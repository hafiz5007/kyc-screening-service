package com.hafiz5007.kyc.ingest

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Dev-only endpoint to kick off an ingest job manually.
 * Not exposed in prod — production ingestion runs as a scheduled job.
 */
@RestController
@RequestMapping("/api/v1/dev/ingest")
@Profile("dev")
class IngestController(
    private val ofac: OfacIngestJob,
    private val hmt: HmtIngestJob,
    private val pep: OpenSanctionsPepIngestJob
) {

    @PostMapping("/ofac")
    fun runOfac(@RequestParam source: String) = ofac.run(source)

    @PostMapping("/hmt")
    fun runHmt(@RequestParam source: String) = hmt.run(source)

    @PostMapping("/pep")
    fun runPep(@RequestParam source: String) = pep.run(source)
}
