package com.hafiz5007.kyc.infrastructure.repositories

import com.hafiz5007.kyc.infrastructure.entities.SanctionRecord
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface SanctionRepository : MongoRepository<SanctionRecord, String> {

    /**
     * Coarse first-pass filter: any sanction record whose normalised name
     * contains at least one of the candidate tokens. The scorer refines from here.
     */
    @Query("{ 'normalisedName': { \$regex: ?0, \$options: 'i' } }")
    fun findByNormalisedNameMatching(regex: String): List<SanctionRecord>

    fun countBySource(source: String): Long
}
