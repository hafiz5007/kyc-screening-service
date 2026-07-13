package com.hafiz5007.kyc.domain.service

import java.text.Normalizer
import java.util.Locale

/**
 * Turns any input name into a stable, comparable form.
 *
 *  - Unicode NFD normalisation
 *  - Diacritic stripping (é -> e, ø -> o)
 *  - Lower-case with root locale so Turkish "i/İ" behaves consistently
 *  - Punctuation to whitespace
 *  - Collapses whitespace runs
 *  - Removes honorifics that don't help match ("Mr", "Mrs", "Dr", "Sir", ...)
 *
 * The output is what gets stored on the persistence-side entity's
 * `normalisedName` column and used as the coarse Mongo filter before the
 * scorer runs.
 *
 * Pure Kotlin — no framework annotations. The application module exposes
 * this as a Spring bean via <c>DomainConfig</c>.
 */
class NameNormalizer {

    fun normalise(raw: String?): String {
        if (raw.isNullOrBlank()) return ""

        val decomposed = Normalizer.normalize(raw, Normalizer.Form.NFD)
        val stripped = decomposed.replace(DIACRITICS, "")
        val lowered = stripped.lowercase(Locale.ROOT)
        val cleaned = lowered.replace(NON_ALNUM, " ")
        val tokens = cleaned.split(WHITESPACE).filter { it.isNotBlank() && it !in HONORIFICS }
        return tokens.joinToString(" ")
    }

    /**
     * Split a normalised name into its constituent tokens so downstream matchers
     * can do token-set / token-sort comparisons.
     */
    fun tokens(normalised: String): List<String> =
        if (normalised.isBlank()) emptyList()
        else normalised.split(WHITESPACE).filter { it.isNotBlank() }

    companion object {
        private val DIACRITICS = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        private val NON_ALNUM  = "[^\\p{L}\\p{Nd}]+".toRegex()
        private val WHITESPACE = "\\s+".toRegex()
        private val HONORIFICS = setOf(
            "mr", "mrs", "ms", "miss", "mx", "dr", "prof", "sir", "dame",
            "lord", "lady", "hon", "rev", "fr", "sr", "jr", "esq"
        )
    }
}
