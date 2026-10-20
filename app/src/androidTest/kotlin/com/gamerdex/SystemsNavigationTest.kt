package com.gamerdex

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import com.gamerdex.ui.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.ExternalResource
import java.io.File

class SystemsNavigationTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private var previous=StartScreen.GAMES
    @get:Rule(order=0) val prepare=object:ExternalResource(){override fun before(){previous=deps.preferences().startScreen.value;deps.preferences().setStartScreen(StartScreen.SYSTEMS)};override fun after(){deps.preferences().setStartScreen(previous)}}
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    @Test fun platformCardsCountAliasCopiesAndNavigateGamesWithoutDuplicatingOwnedUnits():Unit=runBlocking {
        val repo=deps.collection();val settings=deps.preferences();val oldMode=settings.collectionView.value
        val seed=System.currentTimeMillis();val a="Mega Alpha $seed";val b="Mega Beta $seed";val other="Other $seed";val added="Added $seed"
        val custom="Mi consola / test & retro $seed";val ids=listOf(seed,seed+1,seed+2)
        try {
            repo.save(Game(seed,a,"Mega Drive","SEGA"));repo.save(Game(seed+1,b,"Genesis","SEGA"));repo.save(Game(seed+2,other,"Nintendo Switch","Nintendo"))
            repo.saveHardware(Hardware(seed,"Mi Mega II","Mega Drive II"));repo.saveHardware(Hardware(seed+1,"Mi Genesis","Genesis"));repo.saveHardware(Hardware(seed+2,"Mi consola vacía",custom))
            val expected=systemLibraries(repo.snapshot().games,repo.snapshot().hardware).single{it.key=="mega drive"}
            compose.onNodeWithTag("systems-home").assertExists()
            compose.onNodeWithText("Buscar sistema o plataforma").performTextInput("mega")
            compose.waitUntil(10_000){compose.onAllNodesWithTag("system-card-mega drive").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("system-card-mega drive").performScrollTo().assert(hasText("${expected.gameCount} videojuegos",substring=true)).assert(hasText("${expected.unitCount} unidades registradas",substring=true))
            val dir=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            compose.waitForIdle();InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->File(dir,"systems-home.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
            compose.onNodeWithTag("system-card-mega drive").performClick()
            compose.onNodeWithTag("system-games-total").assertTextEquals("${expected.gameCount} videojuegos en este sistema")
            compose.waitForIdle();InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->File(dir,"system-games.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
            compose.onNodeWithText("Buscar en tu colección").performTextInput(a)
            compose.onNode(hasText(a) and hasClickAction() and !hasSetTextAction()).performScrollTo().performClick();compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithText("Buscar en tu colección").assert(hasText(a));compose.onNodeWithTag("system-games-total").assertTextEquals("${expected.gameCount} videojuegos en este sistema")
            compose.onNodeWithText("Buscar en tu colección").performTextClearance()
            CollectionView.entries.forEach { mode->compose.onNodeWithTag("collection-grid").performScrollToNode(hasText(mode.label));compose.onNodeWithText(mode.label).performClick() }
            compose.activityRule.scenario.recreate();compose.onNodeWithTag("collection-grid").performScrollToIndex(0);compose.onNodeWithTag("system-games-total").assertExists()
            compose.onNodeWithTag("collection-grid").performScrollToIndex(0);compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithText("Buscar sistema o plataforma").performTextReplacement(custom)
            compose.onNodeWithTag("system-card-${SystemKnowledge.family(custom)}").performScrollTo().performClick()
            compose.onNodeWithTag("system-games-total").assertTextEquals("0 videojuegos en este sistema")
            compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
            compose.onNodeWithText("Plataforma *").assert(hasText(custom))
            compose.onNodeWithText("Título *").performTextInput(added);compose.onNodeWithText("Compañía *").performTextInput("Indie")
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performScrollTo().performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithTag("system-games-total").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("system-games-total").assertTextEquals("1 videojuego en este sistema")
            compose.onNodeWithTag("collection-grid").performScrollToIndex(0);compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithTag("system-card-${SystemKnowledge.family(custom)}").performScrollTo().assert(hasText("1 videojuego",substring=true))
        }finally{
            settings.setCollectionView(oldMode)
            repo.snapshot().games.filter{it.id in ids || it.title==added}.forEach{repo.delete(it.id);repo.removeArchivedPurchase("GAME:${it.id}")}
            ids.forEach{repo.deleteHardware(it);repo.removeArchivedPurchase("HARDWARE:$it")}
        }
    }
}
