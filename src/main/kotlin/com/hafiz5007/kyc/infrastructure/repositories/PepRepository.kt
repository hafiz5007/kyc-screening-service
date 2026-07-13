package com.hafiz5007.kyc.infrastructure.repositories

import com.hafiz5007.kyc.infrastructure.entities.PepRecord
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface PepRepository : MongoRepository<PepRecord, String> {

    @Query("{ 'normalisedName': { \$regex: ?0, \$options: 'i' } }")
    fun findByNormalisedNameMatching(regex: String): List<PepRecord>
}
