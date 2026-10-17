package com.gamerdex.core.domain

import org.junit.Assert.*
import org.junit.Test

class GameTest {
    @Test fun pricesPreserveCentsAndRejectInvalidInput() {
        assertEquals(1999L, parsePriceCents("19,99")); assertEquals(100L, parsePriceCents("1")); assertNull(parsePriceCents(""))
        listOf("-1", "1.999", "NaN", "1e3", "9999999999").forEach { value -> assertThrows(IllegalArgumentException::class.java) { parsePriceCents(value) } }
    }
    @Test fun physicalContentsAreIndependentFromEditionAndCondition() {
        val collector = game().copy(editionType = EditionType.COLLECTOR, physicalCondition = PhysicalCondition.MINT, hasManual = false)
        assertEquals(EditionType.COLLECTOR, collector.effectiveEdition); assertFalse(collector.isComplete)
        assertTrue(collector.contentsLabel.contains("instrucciones")); assertTrue(collector.hasBox)
        assertTrue(collector.copy(condition = GameCondition.DIGITAL).isComplete)
    }
    @Test fun logosRequireHttpsAndAHost() {
        assertTrue(isValidLogoUrl(null))
        assertTrue(isValidLogoUrl("https://example.com/platform.svg"))
        listOf("http://example.com/logo.svg", "file:///tmp/logo.svg", "https:", "not a URL").forEach { value ->
            assertFalse(isValidLogoUrl(value))
            assertThrows(IllegalArgumentException::class.java) { game().copy(platformLogoUrl = value) }
        }
    }
    private fun game() = Game(title = "Metroid Prime", platform = "GameCube", company = "Nintendo")
    @Test fun ratingsMustBeFiniteAndWithinOneHundred() {
        listOf(-1f, 100.1f, Float.NaN, Float.POSITIVE_INFINITY).forEach { value ->
            assertThrows(IllegalArgumentException::class.java) { game().copy(rating = value) }
        }
        assertEquals(100f, game().copy(rating = 100f).rating)
        assertEquals(0f, game().copy(rating = 0f).rating)
        assertEquals("90", formatPersonalRating(90f))
        assertNull(game().rating)
    }
    @Test fun editionDetailsRequireSpecialEdition() {
        assertThrows(IllegalArgumentException::class.java) { game().copy(specialEditionDetails = "Steelbook") }
        assertEquals("Steelbook", game().copy(isSpecialEdition = true, specialEditionDetails = "Steelbook").specialEditionDetails)
    }
    @Test fun collectionRequiresNamedReferences() {
        assertThrows(IllegalArgumentException::class.java) { game().copy(platform = " ") }
        assertThrows(IllegalArgumentException::class.java) { game().copy(company = "") }
    }
}
