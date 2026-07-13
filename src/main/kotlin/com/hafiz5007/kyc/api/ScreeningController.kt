package com.hafiz5007.kyc.api

import com.hafiz5007.kyc.domain.model.ScreeningRequest
import com.hafiz5007.kyc.domain.model.ScreeningResult
import com.hafiz5007.kyc.domain.service.ScreeningService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/screening")
@Tag(name = "Screening", description = "Run a KYC / AML screen against sanctions and PEP lists")
class ScreeningController(
    private val service: ScreeningService
) {

    @PostMapping("/check")
    @Operation(summary = "Screen a person against sanctions + PEP lists")
    fun check(@Valid @RequestBody request: ScreeningRequest): ResponseEntity<ScreeningResult> {
        val result = service.screen(request)
        return ResponseEntity.ok(result)
    }
}
