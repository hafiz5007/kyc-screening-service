package com.hafiz5007.kyc.domain

import com.hafiz5007.kyc.domain.model.Person
import com.hafiz5007.kyc.domain.model.SanctionCandidate
import com.hafiz5007.kyc.domain.model.ScreeningRequest
import com.hafiz5007.kyc.domain.model.ScreeningResult
import com.hafiz5007.kyc.domain.ports.PepSource
import com.hafiz5007.kyc.domain.ports.SanctionSource
import com.hafiz5007.kyc.domain.service.MatchScorer
import com.hafiz5007.kyc.domain.service.NameNormalizer
import com.hafiz5007.kyc.domain.service.ScreeningService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Zero Spring, zero MongoDB, zero Jakarta Validation. This test compiles + runs
 * with just Kotlin + JUnit + MockK on the classpath, because the code under
 * test has zero framework dependencies.
 */
class ScreeningServiceTest {

    private val normalizer = NameNormalizer()
    private val scorer = MatchScorer(normalizer)
    private val sanctions = mockk<SanctionSource>()
    private val peps = mockk<PepSource>()
    private val service = ScreeningService(sanctions, peps, normalizer, scorer)

    @Test fun `clear when no candidates`() {
        every { sanctions.findCandidatesByNameRegex(any()) } returns emptyList()
        every { peps.findCandidatesByNameRegex(any()) } returns emptyList()

        val result = service.screen(
            ScreeningRequest(Person(fullName = "Jane Doe"))
        )

        assertEquals(ScreeningResult.Decision.CLEAR, result.decision)
        assertEquals(0, result.sanctionsMatches.size)
        assertEquals(0, result.pepMatches.size)
    }

    @Test fun `hit when exact-name sanction candidate returned by source`() {
        every { sanctions.findCandidatesByNameRegex(any()) } returns listOf(
            SanctionCandidate(
                id = "abc-1",
                sourceId = "OFAC-1",
                source = "OFAC",
                primaryName = "Vladimir Petrov",
                normalisedName = "vladimir petrov"
            )
        )
        every { peps.findCandidatesByNameRegex(any()) } returns emptyList()

        val result = service.screen(
            ScreeningRequest(Person(fullName = "Vladimir Petrov"))
        )

        assertEquals(ScreeningResult.Decision.HIT, result.decision)
        assertEquals(1, result.sanctionsMatches.size)
        assertEquals("OFAC", result.sanctionsMatches[0].source)
    }
}
