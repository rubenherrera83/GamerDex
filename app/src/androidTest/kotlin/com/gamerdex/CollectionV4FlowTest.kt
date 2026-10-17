package com.gamerdex

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.EntryPointAccessors
import com.gamerdex.gamertag.*
import com.gamerdex.ui.theme.VisualTheme
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class CollectionV4FlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val dependencies get() = EntryPointAccessors.fromApplication(context, CollectionV3Dependencies::class.java)
    private fun capture(name: String) {
        val dir = File(context.getExternalFilesDir(null), "render-tests").apply { mkdirs() }
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { image -> File(dir, name).outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) } }
    }
    @Test fun progressionRatingFavoritesStatisticsAndCountersWorkTogether() {
        val title = "Journey V4 ${System.currentTimeMillis()}"
        val repository = dependencies.collection()
        try {
            compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
            compose.onNodeWithText("Título *").performTextInput(title)
            compose.onNodeWithText("Plataforma *").performTextInput("PlayStation 5")
            compose.onNodeWithText("Compañía *").performTextInput("Studio")
            compose.onNodeWithText("Géneros (separados por comas)").performScrollTo().performTextInput("RPG, Aventura")
            compose.onNodeWithText("★ Favorito").performScrollTo().performClick()
            compose.onNodeWithText("Juego finalizado").performScrollTo().performClick()
            compose.onNodeWithText("Porcentaje completado (0–100)").performScrollTo().performTextReplacement("87")
            compose.onNodeWithText("Trofeo platino conseguido").performScrollTo().performClick()
            compose.onNodeWithContentDescription("Activar valoración personal").performScrollTo().performClick()
            compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)).performSemanticsAction(SemanticsActions.SetProgress) { it(90f) }
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performScrollTo().performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
            val saved = runBlocking { repository.snapshot().games.single { it.title == title } }
            assertTrue(saved.isFinished); assertTrue(saved.hasPlatinum); assertTrue(saved.isFavorite)
            assertEquals(87, saved.completionPercent); assertEquals(90f, saved.rating)
            compose.activityRule.scenario.recreate()
            compose.onNodeWithContentDescription("Abrir menú").performClick()
            compose.onNodeWithText("Favoritos").performClick()
            compose.onNode(hasText("PlayStation 5") and hasClickAction()).performClick()
            compose.onNodeWithText(title).performScrollTo().assertExists()
            capture("favorites.png")
            compose.onNodeWithText("☰").performClick(); compose.onNodeWithText("Estadísticas").performClick()
            compose.onNodeWithText("Trofeos platino").assertExists()
            capture("statistics.png")
            compose.onNodeWithText("Juegos finalizados").performClick()
            compose.onNodeWithText(title).performScrollTo().performClick()
            compose.onNodeWithText("Finalizado · 87 % completado").performScrollTo().assertExists()
            compose.onNodeWithText("Tu nota").assertExists()
            compose.onNodeWithText("90").assertExists()
            compose.onNodeWithText("ALTA").assertExists()
            compose.onNodeWithText("🏆 Trofeo platino conseguido").assertExists()
            compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithText("☰").performClick(); compose.onNodeWithText("Colección").performClick()
            compose.onNodeWithText("SISTEMAS").performClick(); compose.onNode(hasText("Plataformas") and !hasClickAction()).assertExists()
            compose.onNodeWithText("☰").performClick(); compose.onNodeWithText("Colección").performClick()
            compose.onNodeWithText("TÍTULOS").performClick(); compose.onNodeWithText("Todos los títulos").assertExists()
            compose.onNodeWithText("☰").performClick(); compose.onNodeWithText("Colección").performClick()
            compose.onNodeWithText("ESPECIALES").performClick(); compose.onNodeWithText("Ediciones especiales").assertExists()
        } finally { runBlocking { repository.snapshot().games.filter { it.title == title }.forEach { repository.delete(it.id) } } }
    }
    @Test fun nicknamePersistsAndFillsNewGamertags() {
        val prefs = dependencies.preferences()
        val previous = prefs.alias.value
        try {
            compose.onNodeWithContentDescription("Abrir menú").performClick(); compose.onNodeWithText("Preferencias").performClick()
            compose.onNodeWithText("Apodo predeterminado").performTextReplacement("NEKRO PLAYER")
            compose.activityRule.scenario.recreate()
            compose.onNodeWithText("Apodo predeterminado").assert(hasText("NEKRO PLAYER"))
            compose.onNodeWithText("☰").performClick(); compose.onNodeWithText("Gamertags").performClick()
            compose.onNodeWithText("Tu gamertag / alias").assert(hasText("NEKRO PLAYER"))
            compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Vista previa del gamertag").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Compartir gamertag").performScrollTo().assertIsEnabled()
        } finally { prefs.setAlias(previous) }
    }
    @Test fun shareProducesReadablePngAndTemporaryContentGrant() = runBlocking {
        val bitmap = GamertagRenderer(context).render(GamertagInput("PLAYER", "COLLECTION", "ARCHIVO", "0 JUEGOS", null, VisualTheme.POP))
        val intent = GamertagSharing.intent(context, bitmap)
        assertEquals(Intent.ACTION_SEND, intent.action); assertEquals("image/png", intent.type)
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
        assertEquals("content", uri.scheme); assertEquals("${context.packageName}.files", uri.authority)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
        context.contentResolver.openInputStream(uri).use { input ->
            val decoded = BitmapFactory.decodeStream(input)
            assertEquals(1600, decoded.width); assertEquals(900, decoded.height)
        }
        try { androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.files", context.getDatabasePath("gamerdex.db")); fail("Database must not be shareable") }
        catch (_: IllegalArgumentException) { }
    }
}
