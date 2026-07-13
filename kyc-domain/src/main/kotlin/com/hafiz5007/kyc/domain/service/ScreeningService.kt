package com.hafiz5007.kyc.domain.service

import com.hafiz5007.kyc.domain.model.PepCandidate
import com.hafiz5007.kyc.domain.model.PepMatch
import com.hafiz5007.kyc.domain.model.SanctionCandidate
import com.hafiz5007.kyc.domain.model.SanctionsMatch
import com.hafiz5007.kyc.domain.model.ScreeningRequest
import com.hafiz5007.kyc.domain.model.ScreeningResult
import com.hafiz5007.kyc.domain.ports.PepSource
import com.hafiz5007.kyc.domain.ports.SanctionSource
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Instant
import java.util.UUID

/**
 * Orchestrates a single screen: coarse regex filter → per-candidate score
 * from <c>MatchScorer</c> → threshold → decision.
 *
 * Decision rules:
 *   HIT    – any sanctions match with score >= 0.90
 *   REVIEW – any sanctions match with score >= minScore, or any PEP match
 *   CLEAR  – nothing crosses minScore
 *
 * No Spring annotations. The application module registers this as a bean
 * via <c>DomainConfig</c> and wires in the Mongo-backed <c>SanctionSource</c>
 * and <c>PepSource</c> adapters.
 */
class ScreeningService(
    private val sanctionSource: SanctionSource,
    private val pepSource: PepSource,
    private val normalizer: NameNormalizer,
    private val scorer: MatchScorer,
    private val clock: Clock = Clock.systemUTC(),
    private val idGenerator: () -> String = { UUID.randomUUID().toString() }
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun screen(request: ScreeningRequest): ScreeningResult {
        val subjectNormalised = normalizer.normalise(request.person.fullName)
        val filterRegex = buildCoarseFilter(subjectNormalised)

        val sanctionCandidates: List<SanctionCandidate> = if (filterRegex.isBlank()) emptyList()
        else sanctionSource.findCandidatesByNameRegex(filterRegex)

        val pepCandidates: List<PepCandidate> = if (filterRegex.isBlank()) emptyList()
        else pepSource.findCandidatesByNameRegex(filterRegex)

        val sanctionsHits = sanctionCandidates
            .map { candidate ->
                val scored = scorer.score(
                    subjectName = request.person.fullName,
                    subjectDob = request.person.dateOfBirth,
                    subjectCountry = request.person.countryOfResidence,
                    candidateName = candidate.primaryName,
                    candidateAliases = candidate.aliases,
                    candidateDob = candidate.dateOfBirth,
                    candidateCountries = candidate.countries
                )
                SanctionsMatch(
                    recordId = candidate.id,
                    source = candidate.source,
                    matchedName = scored.matchedName,
                    score = scored.score,
                    reasons = scored.reasons
                )
            }
            .filter { it.score >= request.minScore }
            .sortedByDescending { it.score }
            .groupBy { it.source }
            .flatMap { (_, list) -> list.take(request.maxResultsPerSource) }

        val pepHits = pepCandidates
            .map { candidate ->
                val scored = scorer.score(
                    subjectName = request.person.fullName,
                    subjectDob = request.person.dateOfBirth,
                    subjectCountry = request.person.countryOfResidence,
                    candidateName = candidate.fullName,
                    candidateAliases = candidate.aliases,
                    candidateDob = candidate.dateOfBirth,
                    candidateCountries = listOfNotNull(candidate.country)
                )
                PepMatch(
                    recordId = candidate.id,
                    matchedName = scored.matchedName,
                    position = candidate.position,
                    country = candidate.country,
                    score = scored.score,
                    reasons = scored.reasons
                )
            }
            .filter { it.score >= request.minScore }
            .sortedByDescending { it.score }
            .take(request.maxResultsPerSource)

        val decision = decide(sanctionsHits, pepHits)
        log.info(
            "screening subject=\"{}\" sanction_hits={} pep_hits={} decision={}",
            request.person.fullName, sanctionsHits.size, pepHits.size, decision
        )

        return ScreeningResult(
            screeningId = idGenerator(),
            requestedAtUtc = Instant.now(clock),
            subject = request.person,
            decision = decision,
            sanctionsMatches = sanctionsHits,
            pepMatches = pepHits
        )
    }

    /** Any token of length >= 3 is a candidate; join with regex-OR. */
    private fun buildCoarseFilter(subjectNormalised: String): String =
        normalizer.tokens(subjectNormalised)
            .filter { it.length >= 3 }
            .joinToString("|") { java.util.regex.Pattern.quote(it) }

    private fun decide(sanctions: List<SanctionsMatch>, peps: List<PepMatch>): ScreeningResult.Decision =
        when {
            sanctions.any { it.score >= 0.90 } -> ScreeningResult.Decision.HIT
            sanctions.isNotEmpty() || peps.isNotEmpty() -> ScreeningResult.Decision.REVIEW
            else -> ScreeningResult.Decision.CLEAR
        }
}
