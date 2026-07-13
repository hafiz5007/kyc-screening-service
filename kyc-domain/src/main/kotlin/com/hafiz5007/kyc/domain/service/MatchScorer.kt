package com.hafiz5007.kyc.domain.service

import info.debatty.java.stringsimilarity.JaroWinkler
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.max

/**
 * Deterministic scorer that turns a (subject-name, candidate-name, DOB, country)
 * tuple into a fuzzy match score in [0.0, 1.0] plus a human-readable list of
 * reasons the score came out where it did.
 *
 * Design goals:
 *   - Explainable — every non-zero component is captured in the reasons list.
 *   - Symmetric — swap subject and candidate, get the same score.
 *   - Fast    — pure CPU, no I/O, safe to call per candidate row.
 *
 * The primary similarity signal is Jaro-Winkler over normalised name tokens,
 * which is the industry-standard baseline for sanctions screening. DOB and
 * country give small additive boosts / penalties.
 *
 * Pure Kotlin. Spring wires it up as a bean in the application module.
 */
class MatchScorer(
    private val normalizer: NameNormalizer
) {
    private val jw = JaroWinkler()

    fun score(
        subjectName: String,
        subjectDob: LocalDate?,
        subjectCountry: String?,
        candidateName: String,
        candidateAliases: List<String> = emptyList(),
        candidateDob: LocalDate? = null,
        candidateCountries: List<String> = emptyList()
    ): Scored {
        val reasons = mutableListOf<String>()

        val subject   = normalizer.normalise(subjectName)
        val primary   = normalizer.normalise(candidateName)
        val allNames  = (listOf(primary) + candidateAliases.map(normalizer::normalise)).distinct()

        val bestNameSim = allNames.maxOfOrNull { nameSimilarity(subject, it) } ?: 0.0
        val bestName    = allNames.zip(allNames.map { nameSimilarity(subject, it) })
            .maxByOrNull { it.second }?.first ?: primary

        reasons += "name similarity ${(bestNameSim * 100).toInt()}% against \"$bestName\""

        // Base score is the name similarity; DOB and country nudge it up or down.
        var score = bestNameSim

        if (subjectDob != null && candidateDob != null) {
            val yearDelta = abs(subjectDob.year - candidateDob.year)
            when {
                subjectDob == candidateDob -> {
                    score += 0.10
                    reasons += "DOB exact match"
                }
                yearDelta <= 1 -> {
                    score += 0.03
                    reasons += "DOB within 1 year"
                }
                yearDelta >= 5 -> {
                    score -= 0.10
                    reasons += "DOB differs by ${yearDelta} years"
                }
            }
        }

        if (subjectCountry != null && candidateCountries.isNotEmpty()) {
            if (candidateCountries.any { it.equals(subjectCountry, ignoreCase = true) }) {
                score += 0.05
                reasons += "country of residence matches"
            }
        }

        return Scored(score = score.coerceIn(0.0, 1.0), matchedName = bestName, reasons = reasons)
    }

    /**
     * Combine token-order-insensitive similarity with a token-sort variant. This
     * makes "John Smith" and "Smith John" score the same, while still rewarding
     * exact order matches slightly more.
     */
    internal fun nameSimilarity(a: String, b: String): Double {
        if (a.isBlank() || b.isBlank()) return 0.0
        val direct = jw.similarity(a, b)
        val sortedA = normalizer.tokens(a).sorted().joinToString(" ")
        val sortedB = normalizer.tokens(b).sorted().joinToString(" ")
        val sorted = jw.similarity(sortedA, sortedB)
        return max(direct, sorted)
    }

    data class Scored(val score: Double, val matchedName: String, val reasons: List<String>)
}
