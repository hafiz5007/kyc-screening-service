package com.hafiz5007.kyc.domain

import com.hafiz5007.kyc.domain.service.MatchScorer
import com.hafiz5007.kyc.domain.service.NameNormalizer
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

class MatchScorerTest {

    private val normalizer = NameNormalizer()
    private val scorer = MatchScorer(normalizer)

    @Test fun `identical names score 1_0`() {
        val s = scorer.score("John Smith", null, null, "John Smith")
        assertTrue(s.score == 1.0, "expected 1.0 got ${s.score}")
    }

    @Test fun `token order does not tank the score`() {
        val s = scorer.score("John Smith", null, null, "Smith, John")
        assertTrue(s.score >= 0.95, "expected >= 0.95, got ${s.score}")
    }

    @Test fun `diacritic difference is forgiven`() {
        val s = scorer.score("Jose Garcia", null, null, "José García")
        assertTrue(s.score >= 0.95)
    }

    @Test fun `unrelated names score low`() {
        val s = scorer.score("John Smith", null, null, "Vladimir Petrov")
        assertTrue(s.score < 0.5, "expected < 0.5, got ${s.score}")
    }

    @Test fun `DOB match boosts score`() {
        val d = LocalDate.of(1980, 5, 15)
        val base    = scorer.score("John Smith", null, null, "Jon Smyth")
        val boosted = scorer.score("John Smith", d, null, "Jon Smyth", candidateDob = d)
        assertTrue(boosted.score > base.score, "DOB match should increase score")
    }

    @Test fun `alias match wins over primary`() {
        val s = scorer.score(
            subjectName = "Bob Dylan",
            subjectDob = null,
            subjectCountry = null,
            candidateName = "Robert Zimmerman",
            candidateAliases = listOf("Bob Dylan")
        )
        assertTrue(s.score >= 0.95, "expected alias to win, got ${s.score}")
    }
}
