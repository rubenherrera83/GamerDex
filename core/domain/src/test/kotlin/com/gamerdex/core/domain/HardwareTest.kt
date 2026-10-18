package com.gamerdex.core.domain

import org.junit.Test
import org.junit.Assert.*

class HardwareTest {
    @Test fun acquisitionAndControllersAreValidatedIndependently() {
        val item=Hardware(name="Master System II",platform="Master System",hasBox=true,hasManual=false,purchasedOn="1994-02-28",pricePaidCents=12345,controllers=listOf(Controller("Arcade stick",2,serialNumber="ABC")))
        assertTrue(item.hasBox); assertFalse(item.hasManual); assertEquals(2,item.controllers.single().quantity)
        listOf("2025-02-29","2020-13-01","01/02/1994","2020-1-01").forEach { assertThrows(IllegalArgumentException::class.java){item.copy(purchasedOn=it)} }
        assertTrue(validPurchaseDate("2024-02-29"))
        assertThrows(IllegalArgumentException::class.java){item.copy(pricePaidCents=-1)}
        assertThrows(IllegalArgumentException::class.java){Controller("Pad",0)}
        assertThrows(IllegalArgumentException::class.java){item.copy(imageUrl="http://example.com/image.png")}
    }
    @Test fun eanAndUpcPreserveLeadingZerosAndValidateChecksum() {
        listOf("0711719577966","711719577966","96385074").forEach { assertTrue(it,validBarcode(it)) }
        listOf("0711719577965","123","abcdefghijklm"," 0711719577966").forEach{assertFalse(validBarcode(it))}
    }
}
