package com.gamerdex

import android.graphics.*
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gamerdex.core.domain.*
import com.gamerdex.ui.AppPreferences
import com.gamerdex.ui.CollectionView
import com.gamerdex.ui.theme.VisualTheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class CollectionV3FlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val dependencies get() = EntryPointAccessors.fromApplication(context, CollectionV3Dependencies::class.java)

    @Test fun coverFlowNavigatesFiltersAndRemembersMode() = runBlocking {
        val repository = dependencies.collection()
        val prefs = dependencies.preferences()
        prefs.select(VisualTheme.CYBERPUNK); prefs.setCollectionView(CollectionView.LIST)
        val ids = mutableListOf<Long>()
        val files = mutableListOf<File>()
        try {
            listOf("Aurora Drift", "Pixel Frontier", "Neon Circuit").forEachIndexed { index, title ->
                val bitmap = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.shader = LinearGradient(0f, 0f, 480f, 640f, intArrayOf(Color.rgb(15 + index * 30, 30, 70), Color.rgb(30, 140 - index * 25, 160)), null, Shader.TileMode.CLAMP)
                canvas.drawPaint(paint); paint.shader = null
                paint.color = Color.rgb(220, 250, 140); paint.alpha = 150
                canvas.drawCircle(300f, 220f, 120f, paint); paint.alpha = 255
                for (i in 0..10) { paint.color = Color.rgb(10, 25 + i * 4, 45); canvas.drawRect(i * 55f, 380f - (i % 3) * 50, i * 55f + 42, 640f, paint) }
                paint.color = Color.WHITE; paint.textSize = 42f; paint.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                title.split(' ').forEachIndexed { row, word -> canvas.drawText(word.uppercase(), 35f, 500f + row * 50, paint) }
                val file = File(context.cacheDir, "v3-cover-$index.png").also { files += it }
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                val id = System.currentTimeMillis() + index
                ids += id
                repository.save(Game(id = id, title = "$title [V3]", platform = if (index == 2) "Nintendo Switch" else "PlayStation 5", company = "Preview Studio", coverUrl = Uri.fromFile(file).toString(), genres = listOf("Aventura")))
            }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Cover Flow").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Buscar en tu colección").performScrollTo().performTextInput("[V3]")
            compose.onNodeWithText("Cover Flow").performScrollTo().performClick()
            compose.onNodeWithTag("cover-flow").performScrollTo().assertExists()
            compose.onNodeWithContentDescription("Juego anterior").assertIsNotEnabled()
            compose.onNodeWithContentDescription("Juego siguiente").performScrollTo().performClick()
            compose.onNodeWithText("2 / 3").assertExists()
            compose.onNodeWithText("Abrir juego").performScrollTo().performClick()
            compose.onNodeWithText("Editar").assertExists()
            compose.onNodeWithText("Volver").performClick()
            compose.activityRule.scenario.recreate()
            assertEquals("COVER_FLOW", context.getSharedPreferences("gamerdex_preferences", 0).getString("collection_view", null))
            compose.onNodeWithTag("cover-flow").performScrollTo().assertExists()
            val directory = File(context.getExternalFilesDir(null), "render-tests").apply { mkdirs() }
            instrumentation.uiAutomation.takeScreenshot().let { bitmap -> File(directory, "cover-flow.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
            compose.onNode(hasText("Nintendo Switch") and hasClickAction()).performScrollTo().performClick()
            compose.onNodeWithText("1 / 1").assertExists()
            compose.onNodeWithContentDescription("Juego siguiente").assertIsNotEnabled()
            compose.onNodeWithText("Buscar en tu colección").performScrollTo().performTextInput("sin coincidencias v3")
            compose.onNodeWithText("Sin coincidencias.").performScrollTo().assertExists()
            compose.onNodeWithText("Buscar en tu colección").performTextClearance()
            compose.onNodeWithTag("cover-flow").performScrollTo().assertExists()
            compose.onNodeWithText("Lista").performScrollTo().performClick()
            compose.onNodeWithTag("cover-flow").assertDoesNotExist()
        } finally {
            ids.forEach { repository.delete(it) }; files.forEach { it.delete() }
            prefs.setCollectionView(CollectionView.LIST); prefs.select(VisualTheme.CYBERPUNK)
        }
    }

    @Test fun platformPickerShowsOfflineLogosAndAllowsSelection() {
        compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
        compose.onNodeWithText("Elegir").performClick()
        compose.onNodeWithText("Buscar consola o sistema").performTextInput("Switch")
        compose.onNodeWithContentDescription("Logotipo de Nintendo Switch").assertExists()
        val directory = File(context.getExternalFilesDir(null), "render-tests").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { bitmap -> File(directory, "platform-picker.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        compose.onNodeWithText("Nintendo Switch", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Plataforma *").assert(hasText("Nintendo Switch"))
        compose.onNodeWithText("Volver").performClick()
    }
}
