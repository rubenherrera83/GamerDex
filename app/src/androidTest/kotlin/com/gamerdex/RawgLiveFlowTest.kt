package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.platform.app.InstrumentationRegistry
import coil3.SingletonImageLoader
import coil3.decode.DataSource
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import com.gamerdex.gamertag.GamertagInput
import com.gamerdex.gamertag.GamertagRenderer
import com.gamerdex.ui.theme.VisualTheme
import java.io.File
import android.net.Uri

/** Opt-in in practice: requires a configured RAWG key and live Internet access. */
@LargeTest @RunWith(AndroidJUnit4::class)
class RawgLiveFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun barcodeLookupFindsRawgCandidatesWithoutAutomaticallyAddingAGame() {
        assumeTrue("RAWG and ScanDex tokens are required",BuildConfig.RAWG_API_KEY.isNotBlank() && BuildConfig.SCANDEX_API_TOKEN.isNotBlank())
        compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
        compose.onNodeWithText("⌕ Autocompletar con RAWG").performClick()
        compose.onNodeWithText("Buscar por código de barras / ScanDex").performClick()
        compose.onNodeWithText("Código EAN / UPC").performTextInput("0711719577966")
        compose.onNodeWithText("Buscar código").performClick()
        compose.waitUntil(30_000){compose.onAllNodesWithText("ScanDex:",substring=true).fetchSemanticsNodes().isNotEmpty()}
        val candidate=hasText("Horizon",substring=true) and hasClickAction() and !hasSetTextAction()
        compose.waitUntil(30_000){compose.onAllNodes(candidate).fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("Continuar con el formulario manual").performClick()
        compose.onNodeWithText("Título *").assert(hasText("",substring=false))
        compose.onNodeWithText("Volver").performClick()
    }
    @Test fun importsMetadataFromRawgAndSavesLocally() {
        assumeTrue("RAWG key is required for the live test", BuildConfig.RAWG_API_KEY.isNotBlank())
        compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
        compose.onNodeWithText("⌕ Autocompletar con RAWG").performClick()
        compose.onNodeWithText("Buscar título en RAWG").performTextInput("Portal")
        val result = hasText("Portal") and hasClickAction() and !hasSetTextAction()
        compose.waitUntil(30_000) { compose.onAllNodes(result).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(result).performClick()
        compose.onNodeWithText("Elige tu versión").assertExists()
        compose.onNodeWithText("PC", useUnmergedTree = true).performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Buscar título en RAWG").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Compañía *").assert(hasText("Valve", substring = true))
        compose.onNodeWithText("Año de lanzamiento").assert(hasText("2007", substring = true))
        val coverUrl = compose.onNodeWithText("URL de portada (opcional)").fetchSemanticsNode().config[SemanticsProperties.EditableText].text
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val loader = SingletonImageLoader.get(context)
        val request = ImageRequest.Builder(context).data(coverUrl).size(300, 400).build()
        val online = runBlocking { withTimeout(30_000) { loader.execute(request) } }
        assertTrue("RAWG artwork must load", online is SuccessResult)
        val offline = runBlocking { withTimeout(30_000) {
            loader.execute(request.newBuilder().memoryCachePolicy(CachePolicy.DISABLED).networkCachePolicy(CachePolicy.DISABLED).build())
        } }
        assertTrue("Artwork must remain available with network and memory cache disabled", offline is SuccessResult)
        assertEquals(DataSource.DISK, (offline as SuccessResult).dataSource)
        val renderer = GamertagRenderer(context)
        val directory = File(context.getExternalFilesDir(null), "render-tests").apply { mkdirs() }
        runBlocking { withTimeout(30_000) {
            val tag = renderer.render(GamertagInput("PLAYER ONE", "PORTAL", "PC / MI COLECCIÓN", "COMPLETO / 2007", coverUrl, VisualTheme.CYBERPUNK))
            renderer.write(Uri.fromFile(File(directory, "gamertag-live-CYBERPUNK.png")), tag)
        } }
        val title = "Portal RAWG ${System.currentTimeMillis()}"
        compose.onNodeWithText("Título *").performTextReplacement(title)
        compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(title).performScrollTo().performClick()
        compose.onNodeWithText("Imágenes y metadatos: RAWG ↗").performScrollTo().assertExists()
        compose.onNodeWithText("Comunidad RAWG").performScrollTo()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("votos", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Media RAWG convertida a escala 0–100.").performScrollTo().assertIsDisplayed()
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { image -> File(directory, "community-rating.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) } }
        compose.onNodeWithText("CAPTURAS Y VÍDEOS").performScrollTo()
        compose.waitUntil(30_000) { compose.onAllNodesWithContentDescription("Captura de RAWG").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Eliminar de la colección").performScrollTo()
        compose.onNodeWithText("Eliminar de la colección").assertIsDisplayed()
        compose.onAllNodesWithContentDescription("Captura de RAWG").onFirst().assertIsDisplayed()
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { image -> File(directory, "rawg-media.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) } }
        compose.onNodeWithText("Eliminar de la colección").performScrollTo().performClick()
        compose.onNodeWithText("Eliminar", useUnmergedTree = true).performClick()
    }
}
