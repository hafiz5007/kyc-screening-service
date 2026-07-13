package com.hafiz5007.kyc.domain.model

import java.time.LocalDate

/**
 * Framework-free view of a Politically Exposed Person row. See
 * <c>SanctionCandidate</c> for the same rationale.
 */
data class PepCandidate(
    val id: String,
    val sourceId: String,
    val fullName: String,
    val aliases: List<String> = emptyList(),
    val dateOfBirth: LocalDate? = null,
    val country: String? = null,
    val position: String? = null,
    val normalisedName: String
)
