package com.gamerdex

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.File

class HardwareFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun createsSystemWithControllerRecreatesEditsAndDeletes()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val repository=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java).collection()
        val name="Master System II [hardware test]"
        try {
            compose.onNodeWithContentDescription("Abrir menú").performClick()
            compose.onNode(hasText("Sistemas") and hasClickAction()).performClick()
            compose.onNodeWithText("+ AÑADIR EQUIPO").performClick()
            compose.onNodeWithText("Modelo / nombre *").performTextInput(name)
            compose.onNodeWithText("Plataforma *").performTextInput("Master System II")
            compose.onNodeWithText("Con caja").performScrollTo()
            compose.onAllNodes(isToggleable()).onFirst().performClick()
            compose.onNodeWithText("Número de serie").performScrollTo().performTextInput("MS2-TEST-123")
            compose.onNodeWithText("Fecha de compra (AAAA-MM-DD)").performScrollTo().performTextInput("1995-06-20")
            compose.onNodeWithText("Precio pagado").performScrollTo().performTextInput("49,95")
            compose.onNodeWithText("+ Añadir mando / accesorio").performScrollTo().performClick()
            compose.onNodeWithText("Mando 1: modelo / edición").performScrollTo().performTextReplacement("Arcade stick especial")
            compose.onNodeWithText("+").performScrollTo().performClick()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithText("GUARDAR SISTEMA").performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()}
            val saved=repository.snapshot().hardware.single{it.name==name}
            assertEquals(4995L,saved.pricePaidCents); assertEquals("1995-06-20",saved.purchasedOn);assertTrue(saved.hasBox)
            assertEquals(2,saved.controllers.single().quantity);assertEquals("MS2-TEST-123",saved.serialNumber)
            compose.onNodeWithText(name).performScrollTo().performClick()
            compose.onNodeWithText("2 × Arcade stick especial").performScrollTo().assertIsDisplayed()
            val directory=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            compose.waitForIdle()
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bmp->File(directory,"hardware-detail.png").outputStream().use{bmp.compress(Bitmap.CompressFormat.PNG,100,it)}}
            compose.onNodeWithText("Editar").performClick()
            compose.onNodeWithText("Modelo / nombre *").performTextReplacement("$name PAL")
            compose.onNodeWithText("GUARDAR SISTEMA").performClick()
            compose.onNodeWithText("$name PAL").assertExists()
            compose.onNodeWithText("Eliminar sistema").performScrollTo().performClick()
            compose.onNodeWithText("Eliminar",useUnmergedTree=true).performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText("+ AÑADIR EQUIPO").fetchSemanticsNodes().isNotEmpty()}
            assertFalse(repository.snapshot().hardware.any{it.name.startsWith(name)})
        } finally { repository.snapshot().hardware.filter{it.name.startsWith(name)}.forEach{repository.deleteHardware(it.id)} }
    }
}
