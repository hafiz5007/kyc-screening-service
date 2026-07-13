package com.hafiz5007.kyc.api

import com.hafiz5007.kyc.infrastructure.repositories.PepRepository
import com.hafiz5007.kyc.infrastructure.repositories.SanctionRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lists")
@Tag(name = "Lists", description = "Health of the loaded reference datasets")
class ListsController(
    private val sanctions: SanctionRepository,
    private val peps: PepRepository
) {

    @GetMapping("/status")
    @Operation(summary = "Loaded dataset counts by source")
    fun status(): Map<String, Any> = mapOf(
        "sanctions" to mapOf(
            "total" to sanctions.count(),
            "ofac" to sanctions.countBySource("OFAC"),
            "hmt" to sanctions.countBySource("HMT"),
            "eu" to sanctions.countBySource("EU"),
            "un" to sanctions.countBySource("UN")
        ),
        "pep" to mapOf("total" to peps.count())
    )
}
