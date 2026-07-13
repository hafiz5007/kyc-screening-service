package com.hafiz5007.kyc.ingest

import com.hafiz5007.kyc.domain.service.NameNormalizer
import com.hafiz5007.kyc.infrastructure.repositories.SanctionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * OFAC SDN List ingester.
 * Feed: https://www.treasury.gov/ofac/downloads/sdn.xml (public, no key required).
 *
 * TODO: stream XML with StAX or the async-http client, map to SanctionRecord,
 *       and upsertBulk 5,000 rows at a time via MongoTemplate.
 */
@Component
class OfacIngestJob(
    private val sanctions: SanctionRepository,
    private val normalizer: NameNormalizer
) : IngestJob {

    private val log = LoggerFactory.getLogger(javaClass)
    override val source = "OFAC"

    override fun run(source: String): IngestJob.IngestResult {
        log.info("OfacIngestJob is a stub — implement streaming XML load. Path={}", source)
        return IngestJob.IngestResult(0, 0, 0, 0)
    }
}
