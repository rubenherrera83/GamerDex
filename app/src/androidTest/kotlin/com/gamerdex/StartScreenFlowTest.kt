package com.gamerdex

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.ui.StartScreen
import dagger.hilt.android.EntryPointAccessors
import org.junit.*
import org.junit.Assert.*

class StartScreenFlowTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun preferenceAppliesToFreshSessionsWithoutDisruptingCurrentNavigation() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val settings=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java).preferences()
        val previous=settings.startScreen.value
        fun launch()=ActivityScenario.launch<MainActivity>(Intent(context,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        try {
            settings.setStartScreen(StartScreen.GAMES)
            launch().use { scenario->
                compose.onNodeWithTag("collection-grid").assertExists()
                compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNodeWithText("Preferencias").performClick()
                compose.onNodeWithTag("preferences-list").performScrollToNode(hasTestTag("start-screen-SYSTEMS"))
                compose.onNodeWithTag("start-screen-SYSTEMS").performClick();assertEquals(StartScreen.SYSTEMS,settings.startScreen.value)
                compose.onNode(hasText("Preferencias") and !hasClickAction()).assertExists()
                scenario.recreate();compose.onNodeWithTag("start-screen-SYSTEMS").assertIsSelected()
            }
            launch().use {
                compose.onNodeWithTag("systems-home").assertExists()
                compose.onNodeWithText("Todos los juegos").performClick()
                compose.onNodeWithText("Buscar en tu colección").performTextInput("nonexistent-filter")
                compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNode(hasText("Juegos") and hasClickAction()).performClick()
                compose.onNodeWithText("Buscar en tu colección").assert(hasText("",substring=false))
            }
        }finally{settings.setStartScreen(previous)}
    }
}
