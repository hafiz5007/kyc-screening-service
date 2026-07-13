package com.hafiz5007.kyc.ingest

/**
 * Common surface for every reference-data ingest job.
 * Each implementation streams a public feed into a Mongo collection idempotently.
 */
interface IngestJob {
    val source: String

    /** Path or URL. Idempotent — safe to re-run. */
    fun run(source: String): IngestResult

    data class IngestResult(val inserted: Int, val updated: Int, val skipped: Int, val errors: Int)
}
