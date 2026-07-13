package com.hafiz5007.kyc.domain.model

import java.time.Instant

data class ScreeningResult(
    val screeningId: String,
    val requestedAtUtc: Instant,
    val subject: Person,
    val decision: Decision,
    val sanctionsMatches: List<SanctionsMatch>,
    val pepMatches: List<PepMatch>
) {
    enum class Decision { CLEAR, REVIEW, HIT }
}

data class SanctionsMatch(
    val recordId: String?,
    val source: String,
    val matchedName: String,
    val score: Double,
    val reasons: List<String>
)

data class PepMatch(
    val recordId: String?,
    val matchedName: String,
    val position: String?,
    val country: String?,
    val score: Double,
    val reasons: List<String>
)
