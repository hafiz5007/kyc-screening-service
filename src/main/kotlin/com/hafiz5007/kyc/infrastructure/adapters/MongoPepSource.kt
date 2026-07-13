package com.hafiz5007.kyc.infrastructure.adapters

import com.hafiz5007.kyc.domain.model.PepCandidate
import com.hafiz5007.kyc.domain.ports.PepSource
import com.hafiz5007.kyc.infrastructure.entities.PepRecord
import com.hafiz5007.kyc.infrastructure.repositories.PepRepository
import org.springframework.stereotype.Component

/**
 * Bridges the domain <c>PepSource</c> port to the Spring Data MongoDB repository.
 * See <c>MongoSanctionSource</c> for the same rationale.
 */
@Component
class MongoPepSource(
    private val repository: PepRepository
) : PepSource {

    override fun findCandidatesByNameRegex(nameRegex: String): List<PepCandidate> =
        repository.findByNormalisedNameMatching(nameRegex).map { it.toDomain() }

    private fun PepRecord.toDomain(): PepCandidate = PepCandidate(
        id = id ?: "",
        sourceId = sourceId,
        fullName = fullName,
        aliases = aliases,
        dateOfBirth = dateOfBirth,
        country = country,
        position = position,
        normalisedName = normalisedName
    )
}
