package com.hafiz5007.kyc.infrastructure.adapters

import com.hafiz5007.kyc.domain.model.SanctionCandidate
import com.hafiz5007.kyc.domain.ports.SanctionSource
import com.hafiz5007.kyc.infrastructure.entities.SanctionRecord
import com.hafiz5007.kyc.infrastructure.repositories.SanctionRepository
import org.springframework.stereotype.Component

/**
 * Bridges the domain <c>SanctionSource</c> port to the Spring Data MongoDB
 * repository. This is the ONLY place in the codebase where <c>SanctionRecord</c>
 * (Mongo entity) is converted to <c>SanctionCandidate</c> (domain model).
 * If the storage engine ever changes, this is the file that changes.
 */
@Component
class MongoSanctionSource(
    private val repository: SanctionRepository
) : SanctionSource {

    override fun findCandidatesByNameRegex(nameRegex: String): List<SanctionCandidate> =
        repository.findByNormalisedNameMatching(nameRegex).map { it.toDomain() }

    private fun SanctionRecord.toDomain(): SanctionCandidate = SanctionCandidate(
        id = id ?: "",
        source = source,
        sourceId = sourceId,
        primaryName = primaryName,
        aliases = aliases,
        dateOfBirth = dateOfBirth,
        countries = countries,
        programs = programs,
        normalisedName = normalisedName
    )
}
