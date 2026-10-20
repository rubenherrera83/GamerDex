package com.gamerdex

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gamerdex.ui.theme.VisualTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Captures only this app's collection screen for visual review on an emulator. */
@RunWith(AndroidJUnit4::class)
class DesignPreviewTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun captureThemePreviews() {
        if (!android.os.Build.FINGERPRINT.contains("generic") && !android.os.Build.MODEL.contains("sdk")) return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "render-tests").apply { mkdirs() }
        VisualTheme.entries.forEach { theme ->
            compose.onNodeWithContentDescription("Abrir menú").performClick()
            compose.onNodeWithText("Preferencias").performClick()
            compose.onNodeWithTag("preferences-list").performScrollToNode(hasText(theme.title))
            compose.onNodeWithText(theme.title).performClick()
            compose.onNodeWithText("☰").performClick()
            compose.onNodeWithText("Juegos").performClick()
            compose.waitForIdle()
            instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
                File(directory, "vault-${theme.name}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        }
        compose.onNodeWithContentDescription("Abrir menú").performClick()
        compose.onNodeWithText("Preferencias").performClick()
        compose.onNodeWithTag("preferences-list").performScrollToNode(hasText("Cyberpunk"))
        compose.onNodeWithText("Cyberpunk").performClick()
    }
}
