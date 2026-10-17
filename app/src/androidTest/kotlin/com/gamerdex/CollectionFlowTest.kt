package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SdkSuppress

@RunWith(AndroidJUnit4::class)
class CollectionFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test @SdkSuppress(minSdkVersion = 28)
    fun capturesPhotoAndKeepsItAfterRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.grantRuntimePermission(instrumentation.targetContext.packageName, android.Manifest.permission.CAMERA)
        val title = "Camera ${System.currentTimeMillis()}"
        compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
        compose.onNodeWithText("Título *").performTextInput(title)
        compose.onNodeWithText("Plataforma *").performTextInput("PC")
        compose.onNodeWithText("Compañía *").performTextInput("Valve")
        compose.onNodeWithText("Tomar foto").performScrollTo().performClick()
        compose.waitUntil(30_000) { runCatching { compose.onNodeWithText("Capturar foto").assertIsEnabled() }.isSuccess }
        compose.onNodeWithText("Capturar foto").performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithContentDescription("Foto de tu copia").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(title).performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
        // Scroll the outer vertical container, rather than the nested horizontal photo pager.
        compose.onNodeWithText("Eliminar de la colección").performScrollTo()
        compose.onNodeWithContentDescription("Foto 1 de $title").assertIsDisplayed()
        compose.onNodeWithContentDescription("Foto 1 de $title").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Foto ampliada de $title").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Cerrar · Pellizca para ampliar").performClick()
        compose.onNodeWithText("Eliminar de la colección").performScrollTo().performClick()
        compose.onNodeWithText("Eliminar", useUnmergedTree = true).performClick()
    }

    @Test fun createInspectEditAndDeleteManualGame() {
        val title = "Prueba ${System.currentTimeMillis()}"
        compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
        compose.onNodeWithText("Título *").performTextInput(title)
        compose.onNodeWithText("Plataforma *").performTextInput("PC")
        compose.onNodeWithText("Compañía *").performTextInput("Valve")
        compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(title).performScrollTo().performClick()
        compose.onNodeWithText("Editar").performClick()
        compose.onNodeWithText(title).performTextReplacement("$title editado")
        compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("$title editado").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Eliminar de la colección").performScrollTo().performClick()
        compose.onNodeWithText("Eliminar", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("+ AÑADIR JUEGO").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("$title editado").assertDoesNotExist()
    }
}
