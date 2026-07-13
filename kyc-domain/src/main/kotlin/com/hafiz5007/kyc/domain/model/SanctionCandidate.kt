package com.hafiz5007.kyc.domain.model

import java.time.LocalDate

/**
 * Framework-free view of a sanctions-list row, as the screening pipeline
 * sees it. Infrastructure adapters translate MongoDB / SQL / whatever
 * documents into this shape before handing them to <c>ScreeningService</c>.
 *
 * The scorer + service only ever see this type — never the persisted entity.
 */
data class SanctionCandidate(
    val id: String,
    val source: String,               // OFAC | HMT | EU | UN | ...
    val sourceId: String,             // stable id from the upstream feed
    val primaryName: String,
    val aliases: List<String> = emptyList(),
    val dateOfBirth: LocalDate? = null,
    val countries: List<String> = emptyList(),
    val programs: List<String> = emptyList(),
    val normalisedName: String
)
