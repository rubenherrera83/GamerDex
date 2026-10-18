package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import com.gamerdex.gamertag.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*

class GamertagEditorFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun totalsStayGlobalAndChosenLogosPersistReorderAndRemove():Unit=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val dependencies=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
        val repository=dependencies.collection();val settings=dependencies.gamertagSettings()
        val previous=settings.logos.value
        val gameIds=mutableListOf<Long>();val hardwareIds=mutableListOf<Long>()
        try {
            settings.setLogos(emptyList())
            repeat(3){index->val id=System.currentTimeMillis()+index;gameIds+=id;repository.save(Game(id=id,title="Card fixture $index",platform=if(index<2)"Master System" else "PC",company="SEGA"))}
            repeat(2){index->val id=System.currentTimeMillis()+index;hardwareIds+=id;repository.saveHardware(Hardware(id,"Console $index","Master System"))}
            val snapshot=repository.snapshot()
            val totals="Totales de tu colección: ${snapshot.games.size} videojuegos · ${snapshot.hardware.size} sistemas"
            compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNodeWithText("Gamertags").performClick()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText(totals))
            compose.onNodeWithText(totals).assertIsDisplayed()
            compose.onNodeWithText("Personalizar logotipos (0/6)").performScrollTo().performClick()
            compose.onNodeWithText("Buscar logotipo").performTextInput("PlayStation 5")
            compose.onNodeWithText("Añadir").performScrollTo().performClick()
            compose.onNodeWithText("Compañías").performScrollTo().performClick()
            compose.onNodeWithText("Buscar logotipo").performTextInput("SEGA")
            compose.onNodeWithText("Añadir").performScrollTo().performClick()
            compose.onNodeWithText("Listo").performScrollTo().performClick()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithText("Personalizar logotipos (2/6)").performScrollTo().assertExists()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasContentDescription("Subir SEGA"))
            compose.onNodeWithContentDescription("Subir SEGA").performClick()
            compose.waitUntil(5_000){settings.logos.value.firstOrNull()?.name=="SEGA"}
            assertEquals(settings.logos.value,GamertagSettings(context).logos.value)
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("Juego"))
            compose.onNodeWithText("Juego").performClick()
            compose.onNodeWithText(totals).performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("Plataforma"))
            compose.onNodeWithText("Plataforma").performClick()
            compose.onNodeWithText(totals).performScrollTo().assertIsDisplayed()
            compose.onNodeWithContentDescription("Quitar SEGA").performScrollTo().performClick()
            compose.waitUntil(5_000){settings.logos.value.size==1}
            compose.waitUntil(20_000){compose.onAllNodesWithContentDescription("Vista previa del gamertag").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("Compartir gamertag"))
            compose.onNodeWithText("Compartir gamertag").assertIsEnabled()
        } finally { settings.setLogos(previous);gameIds.forEach{repository.delete(it)};hardwareIds.forEach{repository.deleteHardware(it)} }
    }
}
