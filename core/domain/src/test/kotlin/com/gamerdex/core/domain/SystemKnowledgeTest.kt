package com.gamerdex.core.domain

import org.junit.Test
import org.junit.Assert.*

class SystemKnowledgeTest {
    @Test fun aliasesShareFamilyButDistinctGenerationsNeverBorrowRankings() {
        assertEquals(167,SystemKnowledge.rawgId("Genesis"));assertEquals(167,SystemKnowledge.rawgId("Mega Drive II"))
        assertEquals(74,SystemKnowledge.rawgId("Master System II"));assertEquals(79,SystemKnowledge.rawgId("Super Famicon"))
        assertNull(SystemKnowledge.rawgId("Nintendo Switch 2"));assertNull(SystemKnowledge.rawgId("Evercade EXP"))
        assertNotEquals(SystemKnowledge.rawgId("Game Boy"),SystemKnowledge.rawgId("Game Boy Color"))
        assertEquals(SystemKnowledge.history("Genesis"),SystemKnowledge.history("Mega Drive"))
        assertNull(SystemKnowledge.history("Consola inventada"))
        assertTrue(SystemKnowledge.history("PS Vita")!!.sales==null)
        assertTrue(SystemKnowledge.history("Sega Saturn")!!.facts.all{it.source.startsWith("https://")})
    }
    @Test fun spendingSelectionIntersectsYearAndPlatformAndKeepsUnknownDatesSeparate() {
        val items=listOf(Purchase(PurchaseKind.GAME,1,"A","PC",1000,"EUR","2020-01-01"),
            Purchase(PurchaseKind.HARDWARE,2,"B","PC",2000,"EUR","2021-01-01"),
            Purchase(PurchaseKind.GAME,3,"C","PS4",300000,"JPY","2021-01-02"),
            Purchase(PurchaseKind.GAME,4,"D","PC",null,"EUR"))
        assertEquals(4,filterPurchases(items).size)
        assertEquals(2,filterPurchases(items,"2021").size)
        assertEquals(2000L,spendingTotals(filterPurchases(items,"2021","PC")).combined["EUR"])
        assertEquals(1,filterPurchases(items,"Año desconocido","PC").size)
        assertTrue(filterPurchases(items,"2020","PS4").isEmpty())
    }
}
