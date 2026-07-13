package com.hafiz5007.kyc.infrastructure.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

/**
 * MongoDB persistence view of a PEP row. Infrastructure layer only — the domain
 * only sees the mapped <c>PepCandidate</c>.
 *
 * Politically Exposed Person — a public individual whose position exposes them
 * to a higher risk of bribery / corruption. Source: OpenSanctions PEP dataset.
 */
@Document(collection = "pep")
data class PepRecord(
    @Id
    val id: String? = null,

    @Indexed
    val sourceId: String,

    val fullName: String,
    val aliases: List<String> = emptyList(),
    val dateOfBirth: LocalDate? = null,
    val country: String? = null,          // ISO-3166 alpha-2
    val position: String? = null,         // "Minister of Finance", etc.
    val positionStart: LocalDate? = null,
    val positionEnd: LocalDate? = null,

    @Indexed
    val normalisedName: String,

    val ingestedAtUtc: Instant = Instant.now()
)
