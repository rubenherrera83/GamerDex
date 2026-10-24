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

class CompactCollectionViewsTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private lateinit var oldStart:StartScreen
    private lateinit var oldMode:CollectionView
    private lateinit var oldSystems:SystemView
    private lateinit var oldTheme:VisualTheme
    @get:Rule(order=0) val prepare=object:ExternalResource(){
        override fun before(){val p=deps.preferences();oldStart=p.startScreen.value;oldMode=p.collectionView.value;oldSystems=p.systemView.value;oldTheme=p.theme.value;p.setStartScreen(StartScreen.SYSTEMS);p.setCollectionView(CollectionView.LIST);p.setSystemView(SystemView.CARDS);p.select(VisualTheme.SEGA)}
        override fun after(){val p=deps.preferences();p.setStartScreen(oldStart);p.setCollectionView(oldMode);p.setSystemView(oldSystems);p.select(oldTheme)}
    }
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    private fun hideKeyboard(){compose.activityRule.scenario.onActivity{androidx.core.view.WindowCompat.getInsetsController(it.window,it.window.decorView).hide(androidx.core.view.WindowInsetsCompat.Type.ime())};compose.waitForIdle()}
    private fun capture(name:String){hideKeyboard();Thread.sleep(400);val file=File(context.getExternalFilesDir(null),"render-tests/$name");file.parentFile!!.mkdirs();InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{b->file.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)}}}
    private fun top(){compose.onNodeWithTag("collection-grid").performScrollToIndex(0)}
    private fun orderIsAlphabetical(){top();compose.onNodeWithTag("collection-order").performScrollTo().assert(hasText("Título A–Z",substring=true))}
    private fun chooseView(mode:CollectionView){compose.onNodeWithTag("collection-grid").performScrollToNode(hasTestTag("collection-view-${mode.name}"));compose.onNodeWithTag("collection-view-${mode.name}").performClick()}
    @Test fun compactGamesKeepAlphabeticalDefaultsAcrossGlobalSystemAndFavorites():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis();val prefix="Listado UX $id";val platform="Consola listado $id"
        try {
            listOf("Zeta","Alfa","Beta").forEachIndexed{index,title->repo.save(Game(id+index,"$prefix $title",platform,"Estudio",isFavorite=true,createdAt=id+index))}
            compose.onNodeWithTag("nav-vault").performClick()
            compose.waitUntil(5_000){compose.onAllNodesWithTag("collection-grid").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("collection-grid").performScrollToNode(hasText("Buscar en tu colección"))
            compose.onNodeWithText("Buscar en tu colección").performTextInput(prefix);hideKeyboard();orderIsAlphabetical()
            compose.onNodeWithTag("collection-grid").performScrollToKey(id+1)
            val alfa=compose.onNodeWithTag("game-row-${id+1}").fetchSemanticsNode().boundsInRoot
            val beta=compose.onNodeWithTag("game-row-${id+2}").fetchSemanticsNode().boundsInRoot
            val zeta=compose.onNodeWithTag("game-row-$id").fetchSemanticsNode().boundsInRoot
            assertTrue(alfa.top<beta.top && beta.top<zeta.top)
            assertTrue("La fila debe ser compacta",alfa.height/context.resources.displayMetrics.density<=80f)
            compose.onNodeWithContentDescription("Quitar $prefix Alfa de favoritos").performClick()
            compose.waitUntil(5_000){runBlocking{!repo.snapshot().games.single{it.id==id+1}.isFavorite}}
            capture("compact-games-v19.png")
            compose.onNodeWithTag("game-row-${id+2}").performClick();compose.onNodeWithTag("game-detail").assertExists();compose.onNodeWithText("Volver").performClick()
            chooseView(CollectionView.GRID);orderIsAlphabetical();chooseView(CollectionView.COVER_FLOW);orderIsAlphabetical();chooseView(CollectionView.LIST)
            top();compose.onNodeWithTag("collection-order").performScrollTo().performClick();compose.onNodeWithText("Alta reciente").performClick()
            compose.activityRule.scenario.recreate();top();compose.onNodeWithTag("collection-order").performScrollTo().assert(hasText("Alta reciente",substring=true))
            compose.onNodeWithTag("nav-systems").performClick();compose.onNodeWithText("Todos los juegos").performClick();orderIsAlphabetical()
            compose.onNodeWithTag("nav-systems").performClick();compose.onNodeWithText("Buscar sistema o plataforma").performTextInput(platform);hideKeyboard()
            compose.onNodeWithTag("system-card-${SystemKnowledge.family(platform)}").performScrollTo().performClick();orderIsAlphabetical()
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithTag("nav-more").performClick();compose.onNodeWithText("Favoritos").performClick();orderIsAlphabetical()
            compose.onNodeWithText("Buscar en tu colección").performTextInput(prefix);hideKeyboard()
            compose.onNodeWithTag("collection-grid").performScrollToKey(id+2)
            compose.onNodeWithTag("game-row-${id+2}").assertIsDisplayed();compose.onNodeWithTag("game-row-${id+1}").assertDoesNotExist()
            assertTrue(compose.onNodeWithTag("game-row-${id+2}").fetchSemanticsNode().boundsInRoot.top<compose.onNodeWithTag("game-row-$id").fetchSemanticsNode().boundsInRoot.top)
        } finally {(0L..2L).forEach{repo.delete(id+it);repo.removeArchivedPurchase("GAME:${id+it}")}}
    }
    @Test fun systemsOfferDenseRowsGridAndCardsAndRememberSelection():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis();val prefix="Consola UX $id"
        fun platform(index:Int)="$prefix ${index.toString().padStart(2,'0')}"
        fun tag(index:Int)="system-card-${SystemKnowledge.family(platform(index))}"
        try {
            (0..13).forEach{repo.saveHardware(Hardware(id+it,platform(it),platform(it)))}
            compose.onNodeWithText("Buscar sistema o plataforma").performTextInput(prefix);hideKeyboard()
            compose.onNodeWithTag("systems-home").performScrollToKey(SystemKnowledge.family(platform(0)))
            compose.onNodeWithTag(tag(0)).performScrollTo();val cardsHeight=compose.onNodeWithTag(tag(0)).fetchSemanticsNode().boundsInRoot.height
            compose.onNodeWithTag("systems-home").performScrollToIndex(0);compose.onNodeWithTag("system-view-LIST").performScrollTo().performClick()
            compose.onNodeWithTag("systems-home").performScrollToKey(SystemKnowledge.family(platform(0)))
            val row=compose.onNodeWithTag(tag(0)).fetchSemanticsNode().boundsInRoot
            assertTrue(row.height<cardsHeight);assertTrue(row.height/context.resources.displayMetrics.density<=80f)
            compose.onNodeWithTag(tag(6)).assertIsDisplayed();capture("compact-systems-v19.png")
            compose.activityRule.scenario.recreate();assertEquals(SystemView.LIST,deps.preferences().systemView.value)
            assertEquals(SystemView.LIST,AppPreferences(context).systemView.value)
            compose.onNodeWithTag(tag(0)).performScrollTo().performClick();compose.onNodeWithTag("system-tab-equipment").performClick()
            compose.onNodeWithTag("system-view-LIST").assertIsSelected()
            compose.onNodeWithTag("equipment-card-$id").performScrollTo()
            assertTrue(compose.onNodeWithTag("equipment-card-$id").fetchSemanticsNode().boundsInRoot.height/context.resources.displayMetrics.density<=80f)
            compose.onNodeWithTag("equipment-card-$id").performClick();compose.onNodeWithTag("hardware-detail").assertExists();compose.onNodeWithText("Volver").performClick();compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithTag("systems-home").performScrollToIndex(0);compose.onNodeWithTag("system-view-GRID").performScrollTo().performClick()
            compose.onNodeWithTag("systems-home").performScrollToKey(SystemKnowledge.family(platform(0)))
            val a=compose.onNodeWithTag(tag(0)).fetchSemanticsNode().boundsInRoot;val b=compose.onNodeWithTag(tag(1)).fetchSemanticsNode().boundsInRoot
            assertEquals(a.top,b.top,2f);assertTrue(a.right<=b.left);capture("grid-systems-v19.png")
            compose.onNodeWithTag("systems-home").performScrollToIndex(0);compose.onNodeWithTag("system-view-CARDS").performScrollTo().performClick();compose.onNodeWithTag(tag(0)).performScrollTo().assertIsDisplayed()
        } finally {(0L..13L).forEach{repo.deleteHardware(id+it);repo.removeArchivedPurchase("HARDWARE:${id+it}")}}
    }
}
