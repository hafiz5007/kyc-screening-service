package com.hafiz5007.kyc.config

import com.hafiz5007.kyc.domain.ports.PepSource
import com.hafiz5007.kyc.domain.ports.SanctionSource
import com.hafiz5007.kyc.domain.service.MatchScorer
import com.hafiz5007.kyc.domain.service.NameNormalizer
import com.hafiz5007.kyc.domain.service.ScreeningService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring wiring for the framework-free domain services.
 *
 * The domain classes live in <c>kyc-domain</c> and carry no Spring annotations
 * — they can't, because that module has no Spring dependency. This
 * configuration is where the domain becomes visible to the Spring
 * <c>ApplicationContext</c>. It's also the seam a test can override to
 * inject a fake clock or a fake port implementation.
 */
@Configuration
class DomainConfig {

    @Bean
    fun nameNormalizer(): NameNormalizer = NameNormalizer()

    @Bean
    fun matchScorer(normalizer: NameNormalizer): MatchScorer = MatchScorer(normalizer)

    @Bean
    fun screeningService(
        sanctionSource: SanctionSource,
        pepSource: PepSource,
        normalizer: NameNormalizer,
        scorer: MatchScorer
    ): ScreeningService = ScreeningService(
        sanctionSource = sanctionSource,
        pepSource = pepSource,
        normalizer = normalizer,
        scorer = scorer
    )
}
