package com.gamerdex.core.domain

import org.junit.Test
import org.junit.Assert.*

class ProgressTest {
    @Test fun storyProgressPhysicalContentsAndPlatinumAreIndependent() {
        val story = Game(title = "Story", platform = "PS5", company = "Studio", isFinished = true, completionPercent = 70, hasManual = false)
        val full = Game(title = "Full", platform = "PC", company = "Studio", completionPercent = 100, genres = listOf("RPG", "RPG", "Acción"))
        val trophy = Game(title = "Trophy", platform = "PlayStation 4", company = "Studio", hasPlatinum = true, isFavorite = true)
        val stats = collectionStats(listOf(story, full, trophy))
        assertEquals(1, stats.totals[CollectionFilter.FINISHED]); assertEquals(1, stats.totals[CollectionFilter.HUNDRED])
        assertEquals(1, stats.totals[CollectionFilter.PLATINUM]); assertEquals(1, stats.totals[CollectionFilter.FAVORITES])
        assertEquals(2, stats.totals[CollectionFilter.PHYSICAL]); assertEquals(1, stats.genres["RPG"])
        assertEquals(2, stats.genres["Sin género"])
        assertFalse(story.isComplete)
        assertTrue(collectionStats(emptyList()).platforms.isEmpty())
    }
    @Test fun validatesPlatinumPlatformsAndPercentageBounds() {
        listOf("PS3", "PS4", "PS5", "PlayStation 3", "playstation 4", "PlayStation5").forEach { assertTrue(supportsPlatinum(it)) }
        listOf("PS2", "PS Vita", "PSP", "Xbox", "PC", "Nintendo Switch").forEach { name ->
            assertFalse(supportsPlatinum(name))
            try { Game(title = "Invalid", platform = name, company = "Studio", hasPlatinum = true); fail("Invalid platinum") } catch (_: IllegalArgumentException) { }
        }
        listOf(-1, 101).forEach { percent ->
            try { Game(title = "Invalid", platform = "PC", company = "Studio", completionPercent = percent); fail("Invalid percent") } catch (_: IllegalArgumentException) { }
        }
    }
}
