package com.gamerdex

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import com.gamerdex.ui.*
import com.gamerdex.ui.theme.VisualTheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.ExternalResource
import java.io.File

class UxHeadersAndPreferencesTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private var oldStart=StartScreen.GAMES
    private var oldTheme=VisualTheme.CYBERPUNK
    private var oldMode=CollectionView.GRID
    private var oldAlias=""
    @get:Rule(order=0) val prepare=object:ExternalResource(){
        override fun before(){val p=deps.preferences();oldStart=p.startScreen.value;oldTheme=p.theme.value;oldMode=p.collectionView.value;oldAlias=p.alias.value;p.setStartScreen(StartScreen.SYSTEMS);p.select(VisualTheme.SEGA);p.setCollectionView(CollectionView.GRID)}
        override fun after(){val p=deps.preferences();p.setStartScreen(oldStart);p.select(oldTheme);p.setCollectionView(oldMode);p.setAlias(oldAlias)}
    }
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    private fun capture(name:String){
        compose.activityRule.scenario.onActivity{androidx.core.view.WindowCompat.getInsetsController(it.window,it.window.decorView).hide(androidx.core.view.WindowInsetsCompat.Type.ime())}
        Thread.sleep(400);compose.waitForIdle()
        val file=File(context.getExternalFilesDir(null),"render-tests/$name");file.parentFile!!.mkdirs()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
    }
    @Test fun systemsStartAndMainHeadersStayConsistentWhileGridIsFirst():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis()
        try {
            repo.saveHardware(Hardware(id,"Mega Drive de muestra","Mega Drive"))
            (1L..6L).forEach{repo.save(Game(id+it,"Juego de muestra $it","Mega Drive","Estudio"))}
            compose.onNodeWithTag("systems-home").assertExists()
            compose.onNodeWithTag("screen-header-title").assertTextEquals("Sistemas")
            val bounds=compose.onNodeWithTag("screen-header").fetchSemanticsNode().boundsInRoot
            capture("header-systems-v16.png")
            compose.onNodeWithTag("nav-vault").performClick()
            compose.onNodeWithTag("screen-header-title").assertTextEquals("Juegos")
            assertEquals(bounds,compose.onNodeWithTag("screen-header").fetchSemanticsNode().boundsInRoot)
            val grid=compose.onNodeWithTag("collection-view-GRID").assertIsSelected().fetchSemanticsNode().boundsInRoot
            val list=compose.onNodeWithTag("collection-view-LIST").fetchSemanticsNode().boundsInRoot
            val flow=compose.onNodeWithTag("collection-view-COVER_FLOW").fetchSemanticsNode().boundsInRoot
            assertTrue(grid.left<list.left && (flow.top>list.top || list.left<flow.left))
            capture("header-games-v16.png")
            compose.onNodeWithTag("collection-grid").performScrollToIndex(5)
            compose.onNodeWithTag("screen-header-title").assertIsDisplayed()
            compose.onNodeWithContentDescription("Abrir menú").assertIsDisplayed()
            compose.onNodeWithTag("nav-statistics").performClick()
            compose.onNodeWithTag("screen-header-title").assertTextEquals("Estadísticas")
            assertEquals(bounds,compose.onNodeWithTag("screen-header").fetchSemanticsNode().boundsInRoot)
            capture("header-statistics-v16.png")
            compose.onNodeWithTag("statistics-open-spending").performClick()
            compose.onNodeWithTag("screen-header-title").assertTextEquals("Gasto histórico")
            compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithTag("screen-header-title").assertTextEquals("Estadísticas")
        }finally {
            (1L..6L).forEach{repo.delete(id+it);repo.removeArchivedPurchase("GAME:${id+it}")}
            repo.deleteHardware(id);repo.removeArchivedPurchase("HARDWARE:$id")
        }
    }
    @Test fun preferencesShortcutsReachBackupsWithoutPassingThemesAndChangesPersist() {
        compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNodeWithText("Preferencias").performClick()
        compose.onNodeWithText("Apodo predeterminado").performTextReplacement("PLAYER UX")
        compose.onNodeWithTag("preferences-jump-appearance").performClick()
        compose.onNodeWithTag("preferences-list").performScrollToNode(hasTestTag("theme-KAWAII"))
        compose.onNodeWithTag("theme-KAWAII").performClick().assertIsSelected()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("theme-KAWAII").assertIsSelected()
        compose.onNodeWithTag("preferences-jump-backup").performClick()
        compose.onNodeWithTag("backup-export").assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithTag("backup-import").assertIsDisplayed().assertIsEnabled()
        capture("preferences-backup-v16.png")
        compose.onNodeWithTag("preferences-jump-identity").performClick()
        compose.onNodeWithText("Apodo predeterminado").assert(hasText("PLAYER UX"))
        compose.onNodeWithTag("start-screen-GAMES").performClick()
        assertEquals(StartScreen.GAMES,deps.preferences().startScreen.value)
        compose.onNodeWithTag("screen-header-title").assertTextEquals("Preferencias")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("start-screen-GAMES").assertIsSelected()
        assertEquals("PLAYER UX",AppPreferences(context).alias.value)
        assertEquals(VisualTheme.KAWAII,AppPreferences(context).theme.value)
        assertEquals(StartScreen.GAMES,AppPreferences(context).startScreen.value)
    }
}