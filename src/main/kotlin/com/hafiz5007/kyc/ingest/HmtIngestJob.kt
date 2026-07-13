package com.hafiz5007.kyc.ingest

import com.hafiz5007.kyc.domain.service.NameNormalizer
import com.hafiz5007.kyc.infrastructure.repositories.SanctionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * UK HM Treasury (OFSI) consolidated sanctions list.
 * Feed: https://ofsistorage.blob.core.windows.net/publishlive/2022format/ConList.csv
 *
 * TODO: parse CSV with Univocity or Kotlin's csv utils, map header rows to
 *       SanctionRecord, upsert by sourceId.
 */
@Component
class HmtIngestJob(
    private val sanctions: SanctionRepository,
    private val normalizer: NameNormalizer
) : IngestJob {

    private val log = LoggerFactory.getLogger(javaClass)
    override val source = "HMT"

    override fun run(source: String): IngestJob.IngestResult {
        log.info("HmtIngestJob is a stub — implement CSV load. Path={}", source)
        return IngestJob.IngestResult(0, 0, 0, 0)
    }
}
