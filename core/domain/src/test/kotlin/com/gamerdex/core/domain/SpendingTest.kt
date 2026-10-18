package com.gamerdex.core.domain

import org.junit.Test
import org.junit.Assert.*

class SpendingTest {
    @Test fun yenUsesWholeUnitsAndKeepsOtherCurrencyPrecision() {
        assertEquals(150000L,parsePriceCents("1500","JPY"))
        assertThrows(IllegalArgumentException::class.java){parsePriceCents("1500.50","JPY")}
        assertThrows(IllegalArgumentException::class.java){Game(title="A",platform="Evercade",company="Blaze",priceCurrency="JPY",pricePaidCents=150050)}
        assertEquals(1234L,parsePriceCents("12,34","EUR"))
        assertFalse(formatPrice(150000,"JPY").contains(".00"))
        assertFalse(validBarcode("502655067591"))
    }
    @Test fun yearsKindsAndCurrenciesAreIndependentAndUnknownDatesStayUnknown() {
        val h=Hardware(10,"My Saturn","Sega Saturn",pricePaidCents=200000,currency="JPY",purchasedOn="2020-01-02")
        val game=Game(20,"A","Sega Saturn","Studio",pricePaidCents=300000,priceCurrency="JPY",purchasedOn="2020-12-31",ownedSystemId=10)
        val eur=Game(21,"B","PC","Studio",pricePaidCents=1999,priceCurrency="EUR",releaseYear=1991)
        val entries=seedPurchases(listOf(game,eur,eur.copy(id=22,pricePaidCents=null)),listOf(h))
        val totals=spendingTotals(entries)
        assertEquals(500000L,totals.combined["JPY"]);assertEquals(1999L,totals.combined["EUR"]);assertEquals(1,totals.withoutPrice)
        assertEquals(setOf("2020","Año desconocido"),spendingByYear(entries).keys)
        assertEquals(1999L,spendingByYear(entries).getValue("Año desconocido").combined["EUR"])
        assertEquals("My Saturn",entries.first{it.kind==PurchaseKind.GAME && it.sourceId==20L}.systemName)
    }
}
