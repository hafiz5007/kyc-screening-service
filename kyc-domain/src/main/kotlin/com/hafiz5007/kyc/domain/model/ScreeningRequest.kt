package com.hafiz5007.kyc.domain.model

/**
 * Everything a screen needs. Deliberately doesn't carry a subjectId — it's
 * the caller's job to correlate results back to their own customer records.
 */
data class ScreeningRequest(
    val person: Person,

    /** Return matches with score >= this. Defaults to 0.75. */
    val minScore: Double = 0.75,

    /** Cap results per source. Defaults to 10. */
    val maxResultsPerSource: Int = 10
)
