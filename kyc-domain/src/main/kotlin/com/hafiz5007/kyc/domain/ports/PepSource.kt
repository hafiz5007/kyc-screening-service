package com.hafiz5007.kyc.domain.ports

import com.hafiz5007.kyc.domain.model.PepCandidate

/**
 * The read side of a PEP (Politically Exposed Person) dataset.
 * See <c>SanctionSource</c> for the same rationale.
 */
interface PepSource {
    fun findCandidatesByNameRegex(nameRegex: String): List<PepCandidate>
}
