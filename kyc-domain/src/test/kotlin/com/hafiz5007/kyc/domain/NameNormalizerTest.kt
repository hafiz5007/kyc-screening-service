package com.hafiz5007.kyc.domain

import com.hafiz5007.kyc.domain.service.NameNormalizer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NameNormalizerTest {

    private val n = NameNormalizer()

    @Test fun `strips diacritics`() {
        assertEquals("jose garcia", n.normalise("José García"))
    }

    @Test fun `lowercases and collapses whitespace`() {
        assertEquals("john smith", n.normalise("  JOHN    Smith "))
    }

    @Test fun `removes honorifics`() {
        assertEquals("smith", n.normalise("Dr. Smith"))
        assertEquals("john smith", n.normalise("Mr John Smith"))
    }

    @Test fun `handles null and blank`() {
        assertEquals("", n.normalise(null))
        assertEquals("", n.normalise(""))
        assertEquals("", n.normalise("   "))
    }

    @Test fun `tokens splits normalised name`() {
        val tokens = n.tokens(n.normalise("Jean-Claude Van Damme"))
        assertTrue(tokens.containsAll(listOf("jean", "claude", "van", "damme")))
    }
}
