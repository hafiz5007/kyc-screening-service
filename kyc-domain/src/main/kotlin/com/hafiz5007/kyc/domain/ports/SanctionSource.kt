package com.hafiz5007.kyc.domain.ports

import com.hafiz5007.kyc.domain.model.SanctionCandidate

/**
 * The read side of a sanctions list, as the domain sees it. Implemented in
 * infrastructure by <c>MongoSanctionSource</c>. The domain never knows which
 * store backs it, which is the point.
 */
interface SanctionSource {

    /**
     * Coarse first-pass filter: any candidate whose normalised name matches
     * <c>nameRegex</c>. The scorer refines from there.
     * Return type is deliberately the framework-free candidate, not any
     * infrastructure entity.
     */
    fun findCandidatesByNameRegex(nameRegex: String): List<SanctionCandidate>
}
