package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import android.graphics.Bitmap
import java.io.File

class SpendingEvercadeFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun directBarcodeEntryRejectsIncompleteCodeWithoutNetworkCall() {
        compose.onNodeWithText("+ AÑADIR JUEGO").performClick(); expandGameSections(compose)
        compose.onNodeWithText("Introducir EAN / UPC").performScrollTo().performClick()
        compose.onNodeWithText("Código EAN / UPC").performTextInput("502655067591")
        compose.onNodeWithText("Buscar código").assertIsNotEnabled()
        compose.onNodeWithText("Código incompleto o dígito de control incorrecto.",substring=true).assertExists()
        compose.onNodeWithText("Código EAN / UPC").performTextReplacement("0711719577966")
        compose.onNodeWithText("Buscar código").assertIsEnabled()
        compose.onNodeWithText("Cancelar").performClick();compose.onNodeWithText("Volver").performClick()
    }
    @Test fun officialEvercadeImportsYenPurchaseAndShowsSpendingPerOwnedUnit():Unit=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val repository=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java).collection()
        val id=System.currentTimeMillis();val title="Atari Collection 1 [v7 test]";val systemName="Evercade EXP [v7 test]"
        try {
            repository.saveHardware(Hardware(id,systemName,"Evercade",purchasedOn="2022-01-02",pricePaidCents=1200000,currency="JPY"))
            compose.onNodeWithText("+ AÑADIR JUEGO").performClick(); expandGameSections(compose)
            compose.onNodeWithText("Buscar cartucho Evercade").performScrollTo().performClick()
            compose.onNodeWithText("Buscar cartucho o colección Evercade").performTextInput("Atari Collection 1")
            val cartridge=hasText("Atari Collection 1") and hasClickAction() and !hasSetTextAction()
            compose.waitUntil(40_000){compose.onAllNodes(cartridge).fetchSemanticsNodes().isNotEmpty()}
            compose.onNode(cartridge).performClick()
            compose.waitUntil(40_000){compose.onAllNodesWithText("Buscar cartucho o colección Evercade").fetchSemanticsNodes().isEmpty()}
            compose.onNodeWithText("Plataforma *").assert(hasText("Evercade"))
            compose.onNodeWithText("Título *").performTextReplacement(title)
            compose.onNodeWithText("Fecha de compra (AAAA-MM-DD)").performScrollTo().performTextInput("2023-03-04")
            compose.onNode(hasText("Sistema de mi inventario:",substring=true) and hasClickAction()).performScrollTo().performClick()
            compose.onNodeWithText("$systemName · Evercade").performScrollTo().performClick()
            compose.onNodeWithText("JPY").performScrollTo().performClick()
            compose.onNodeWithText("Precio pagado (opcional)").performScrollTo().performTextInput("1500")
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()}
            val game=repository.snapshot().games.single{it.title==title}
            assertNull(game.rawgId);assertTrue(game.catalogUrl!!.contains("atari-collection-1"));assertTrue(game.notes!!.contains("Adventure"))
            assertEquals(150000L,game.pricePaidCents);assertEquals("JPY",game.priceCurrency);assertEquals(id,game.ownedSystemId)
            val expenses=repository.snapshot().purchases.filter{it.systemId==id}
            assertEquals(1350000L,spendingTotals(expenses).combined["JPY"])
            compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNodeWithText("Estadísticas").performClick()
            compose.onNodeWithText("Ver gasto por año, plataforma y sistema").performScrollTo().performClick()
            compose.onNodeWithText("Por sistema").performClick()
            compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex) and hasAnyAncestor(isDialog())).performScrollToNode(hasText(systemName))
            compose.onNodeWithText(systemName).performScrollTo().assertExists()
            compose.onNodeWithText("2023").performScrollTo().assertExists()
            compose.onNodeWithText("2022").performScrollTo().assertExists()
            val dir=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            compose.waitForIdle();InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{image->File(dir,"spending-by-system.png").outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)}}
        }finally{
            repository.snapshot().games.filter{it.title==title}.forEach{repository.delete(it.id);repository.removeArchivedPurchase("GAME:${it.id}")}
            repository.deleteHardware(id);repository.removeArchivedPurchase("HARDWARE:$id")
        }
    }
}
