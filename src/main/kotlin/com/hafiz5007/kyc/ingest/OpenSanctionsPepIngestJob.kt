package com.hafiz5007.kyc.ingest

import com.hafiz5007.kyc.domain.service.NameNormalizer
import com.hafiz5007.kyc.infrastructure.repositories.PepRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * OpenSanctions PEP dataset ingester.
 * Feed: https://data.opensanctions.org/datasets/latest/peps/targets.simple.csv (public).
 *
 * TODO: bulk CSV -> PepRecord upsert. Use OpenSanctions' `entity_id` as sourceId
 *       for cross-run stability.
 */
@Component
class OpenSanctionsPepIngestJob(
    private val peps: PepRepository,
    private val normalizer: NameNormalizer
) : IngestJob {

    private val log = LoggerFactory.getLogger(javaClass)
    override val source = "OPENSANCTIONS_PEP"

    override fun run(source: String): IngestJob.IngestResult {
        log.info("OpenSanctionsPepIngestJob is a stub — implement PEP CSV load. Path={}", source)
        return IngestJob.IngestResult(0, 0, 0, 0)
    }
}
