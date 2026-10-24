package com.gamerdex

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.Game
import com.gamerdex.ui.CollectionView
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class CollectionV5FlowTest {
    @org.junit.Before fun openGamesSection(){openGlobalCollection(compose)}
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun compactGridHasThreeColumnsAndFourInLandscape() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val dependencies = EntryPointAccessors.fromApplication(context, CollectionV3Dependencies::class.java)
        val repository = dependencies.collection(); val preferences = dependencies.preferences()
        val ids = mutableListOf<Long>()
        try {
            preferences.setCollectionView(CollectionView.LIST)
            repeat(5) { index ->
                val id = System.currentTimeMillis() + index; ids += id
                repository.save(Game(id = id, title = "Grid Sample ${index + 1} [V5]", platform = "PlayStation 5", company = "Studio", createdAt = id))
            }
            compose.onNodeWithText("Buscar en tu colección").performTextInput("[V5]")
            compose.onNodeWithText("Cuadrícula").performScrollTo().performClick()
            compose.activityRule.scenario.recreate()
            assertEquals(CollectionView.GRID, preferences.collectionView.value)
            compose.onNodeWithTag("compact-cover-${ids.first()}").performScrollTo()
            val top = ids.take(4).map { compose.onNodeWithTag("compact-cover-$it").fetchSemanticsNode().boundsInRoot.top }
            assertTrue(abs(top[0] - top[1]) < 2f && abs(top[1] - top[2]) < 2f)
            assertTrue(top[3] > top[0] + 20f)
            val directory = File(context.getExternalFilesDir(null), "render-tests").apply { mkdirs() }
            instrumentation.uiAutomation.takeScreenshot().let { image -> File(directory, "compact-grid.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) } }
            compose.onNodeWithTag("compact-cover-${ids.first()}").performClick()
            compose.onNodeWithText("Editar").assertExists(); compose.onNodeWithText("Volver").performClick()
            compose.runOnUiThread { compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            compose.onNodeWithTag("collection-grid").performScrollToKey(ids.first())
            val landscape = ids.take(4).map { compose.onNodeWithTag("compact-cover-$it").fetchSemanticsNode().boundsInRoot.top }
            assertTrue(landscape.all { abs(it - landscape.first()) < 2f })
            compose.onNodeWithText("Lista").performScrollTo().performClick()
            compose.waitUntil(5_000) { preferences.collectionView.value == CollectionView.LIST && compose.onAllNodesWithTag("compact-cover-${ids.first()}").fetchSemanticsNodes().isEmpty() }
            compose.onNodeWithTag("compact-cover-${ids.first()}").assertDoesNotExist()
        } finally {
            compose.runOnUiThread { compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            preferences.setCollectionView(CollectionView.LIST)
            ids.forEach { repository.delete(it) }
        }
    }
}
