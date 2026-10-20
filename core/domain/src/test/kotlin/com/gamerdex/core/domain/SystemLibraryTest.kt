package com.gamerdex.core.domain

import org.junit.Test
import org.junit.Assert.*

class SystemLibraryTest {
    @Test fun copiesAreCountedOnceAcrossRegionalAliasesAndMultipleOwnedUnits() {
        val games=listOf(Game(1,"Sonic","Mega Drive","Sega"),Game(2,"Sonic","Genesis","Sega"),Game(3,"Other","Nintendo Switch 2","Nintendo"))
        val units=listOf(Hardware(10,"My MD2","Mega Drive II"),Hardware(11,"My Genesis","Genesis",localPhotos=listOf("content://photo/11")),Hardware(12,"Empty Saturn","Sega Saturn"))
        val groups=systemLibraries(games,units)
        assertEquals(3,groups.size);assertEquals(3,groups.sumOf{it.gameCount});assertEquals(3,groups.sumOf{it.unitCount})
        val mega=groups.single{it.key=="mega drive"};assertEquals(2,mega.gameCount);assertEquals(2,mega.unitCount);assertEquals(11L,mega.representative!!.id)
        assertEquals(listOf(1L,2L),gamesForSystem(games,mega.key).map{it.id})
        assertEquals(0,groups.single{it.key=="sega saturn"}.gameCount)
        assertEquals(0,groups.single{it.key=="nintendo switch 2"}.unitCount)
    }
    @Test fun emptyLibrariesAndCustomNamesWithSeparatorsAreSupported() {
        assertTrue(systemLibraries(emptyList(),emptyList()).isEmpty())
        val games=listOf(Game(1,"One","Mi sistema / especial & retro","Company"),Game(2,"Two","mi sistema / especial & retro","Company"),Game(3,"Three","Nintendo Switch","Nintendo"))
        val groups=systemLibraries(games,emptyList());assertEquals(2,groups.size)
        assertEquals(2,gamesForSystem(games,"mi sistema / especial & retro").size)
        assertEquals(1,gamesForSystem(games,"nintendo switch").size)
    }
}
