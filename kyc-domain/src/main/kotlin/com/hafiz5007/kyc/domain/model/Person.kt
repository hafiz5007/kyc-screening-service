package com.hafiz5007.kyc.domain.model

import java.time.LocalDate

/**
 * The subject of a KYC / AML screen. Pure POKO — no Jakarta Validation, no
 * Spring, no MongoDB. Adapter modules can attach their own validation
 * annotations to a wrapping DTO if they want.
 *
 * Kept intentionally minimal; richer identity documents belong in a separate
 * flow, not on the screening subject.
 */
data class Person(
    val fullName: String,
    val dateOfBirth: LocalDate? = null,

    /** ISO 3166-1 alpha-2, e.g. "GB", "US". */
    val countryOfResidence: String? = null,
    val nationality: String? = null,
    val aliases: List<String> = emptyList()
)
