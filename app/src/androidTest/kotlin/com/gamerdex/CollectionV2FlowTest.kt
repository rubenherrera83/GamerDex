package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectionV2FlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun collectorContentsPriceGenresAndMenuPersist() {
        val title = "Collector ${System.currentTimeMillis()}"
        compose.onNodeWithText("+ AÑADIR JUEGO").performClick(); expandGameSections(compose)
        compose.onNodeWithText("Título *").performTextInput(title)
        compose.onNodeWithText("Plataforma *").performTextInput("PC")
        compose.onNodeWithText("Compañía (opcional)").performTextInput("Studio")
        compose.onNodeWithText("Coleccionista").performScrollTo().performClick()
        compose.onNodeWithText("Instrucciones").performScrollTo().performClick()
        compose.onNodeWithText("Bueno").performScrollTo().performClick()
        compose.onNodeWithText("Precio pagado (opcional)").performScrollTo().performTextInput("39,95")
        compose.onNodeWithText("Géneros (separados por comas)").performScrollTo().performTextInput("RPG, Acción")
        compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(title).performScrollTo().performClick()
        compose.onNodeWithText("Falta: instrucciones").performScrollTo().assertExists()
        compose.onNodeWithText("Precio pagado:", substring = true).assert(hasText("39", substring = true))
        compose.onNodeWithText("Crear gamertag de este juego").performScrollTo().performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithContentDescription("Vista previa del gamertag").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("Guardar gamertag como imagen"))
        compose.onNodeWithText("Guardar gamertag como imagen").performScrollTo().assertIsEnabled()
        compose.onNodeWithText("☰").performClick()
        compose.onNodeWithText("Géneros").performClick()
        compose.onNodeWithText("RPG").performClick()
        compose.onNodeWithText("Limpiar filtros").performScrollTo().assertExists()
        compose.onNodeWithText(title).performScrollTo().performClick()
        compose.onNodeWithText("Eliminar de la colección").performScrollTo().performClick()
        compose.onNodeWithText("Eliminar", useUnmergedTree = true).performClick()
    }
    @Test fun themePreferenceSurvivesActivityRecreation() {
        compose.onNodeWithContentDescription("Abrir menú").performClick()
        compose.onNodeWithText("Preferencias").performClick()
        compose.onNodeWithTag("preferences-list").performScrollToNode(hasText("Kawaii"))
        compose.onNodeWithText("Kawaii").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Kawaii").performScrollTo().assertExists()
        val preferences = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("gamerdex_preferences", 0)
        assertEquals("KAWAII", preferences.getString("theme", null))
        compose.onNodeWithTag("preferences-list").performScrollToNode(hasText("Cyberpunk"))
        compose.onNodeWithText("Cyberpunk").performClick()
        assertEquals("CYBERPUNK", preferences.getString("theme", null))
    }
}
