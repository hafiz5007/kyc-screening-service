package com.hafiz5007.kyc.infrastructure.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

/**
 * MongoDB persistence view of a sanctions-list row. Lives in the infrastructure
 * layer — the domain never sees this type. Adapters map it to
 * <c>SanctionCandidate</c> before handing rows to the screening pipeline.
 *
 * Source may be OFAC SDN, UK HMT consolidated list, EU consolidated list, etc.
 */
@Document(collection = "sanctions")
data class SanctionRecord(
    @Id
    val id: String? = null,

    /** Stable per-source identifier from the upstream feed. */
    @Indexed
    val sourceId: String,

    /** OFAC | HMT | EU | UN | ... */
    @Indexed
    val source: String,

    val primaryName: String,
    val aliases: List<String> = emptyList(),
    val dateOfBirth: LocalDate? = null,
    val countries: List<String> = emptyList(),      // ISO-3166 alpha-2
    val programs: List<String> = emptyList(),       // sanction programs

    /** Normalised (lower-case, transliterated) primary name for indexed matching. */
    @Indexed
    val normalisedName: String,

    val ingestedAtUtc: Instant = Instant.now()
)
