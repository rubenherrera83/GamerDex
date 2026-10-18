package com.gamerdex.core.network

import com.gamerdex.core.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class CombinedBarcodeRepositoryTest {
    private val code="5026555067591"
    private fun provider(block:suspend (String)->BarcodeMatch?)=object:BarcodeRepository{override suspend fun lookup(code:String)=block(code)}
    @Test fun scanDexHitNeverCallsFallback()=runTest {
        val match=BarcodeMatch("Civilization VI","Nintendo Switch")
        val repository=CombinedBarcodeRepository(provider{match},provider{error("No debe consultar UPCitemdb")})
        assertEquals(match,repository.lookup(code))
    }
    @Test fun unknownCodeFallsBackWithOriginalDigitsAndSource()=runTest {
        val repository=CombinedBarcodeRepository(provider{null},provider{assertEquals(code,it);BarcodeMatch("Civilization VI","Nintendo Switch","UPCitemdb")})
        assertEquals("UPCitemdb",repository.lookup(code)!!.source)
    }
    @Test fun scanDexFailuresStillAllowFallbackAndRemainVisible()=runTest {
        for(reason in listOf("ScanDex no ha autorizado el token.","ScanDex ha alcanzado el límite.","ScanDex conoce el código sin juego.")) {
            val repository=CombinedBarcodeRepository(provider{error(reason)},provider{BarcodeMatch("Game","","UPCitemdb")})
            val match=repository.lookup(code)!!
            assertEquals("UPCitemdb",match.source);assertTrue(match.notice!!.contains(reason))
        }
    }
    @Test fun absenceIsDistinctFromProviderFailure()=runTest {
        assertNull(CombinedBarcodeRepository(provider{null},provider{null}).lookup(code))
        try {CombinedBarcodeRepository(provider{null},provider{error("UPCitemdb límite")}).lookup(code);fail()}
        catch(e:IllegalStateException){assertTrue(e.message!!.contains("límite"))}
        try {CombinedBarcodeRepository(provider{error("ScanDex token")},provider{null}).lookup(code);fail()}
        catch(e:IllegalStateException){assertTrue(e.message!!.contains("token"));assertTrue(e.message!!.contains("no encontró"))}
    }
    @Test fun invalidInputAndCancellationNeverStartFallback()=runTest {
        val failProvider=provider{error("No debe consultar")}
        try {CombinedBarcodeRepository(failProvider,failProvider).lookup("502655067591");fail()}catch(_:IllegalArgumentException){}
        try {CombinedBarcodeRepository(provider{throw CancellationException()},failProvider).lookup(code);fail()}catch(_:CancellationException){}
        try {CombinedBarcodeRepository(provider{null},provider{throw CancellationException()}).lookup(code);fail()}catch(_:CancellationException){}
    }
}
