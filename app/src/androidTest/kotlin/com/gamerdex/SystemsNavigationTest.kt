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
    private fun hideKeyboard(){
        compose.activityRule.scenario.onActivity{activity->
            androidx.core.view.WindowCompat.getInsetsController(activity.window,activity.window.decorView).hide(androidx.core.view.WindowInsetsCompat.Type.ime())
        }
        // Insets can report the target state before the IME animation finishes.
        Thread.sleep(400)
        compose.waitForIdle()
    }
    @Test fun platformCardsCountAliasCopiesAndNavigateGamesWithoutDuplicatingOwnedUnits():Unit=runBlocking {
        val repo=deps.collection();val settings=deps.preferences();val oldMode=settings.collectionView.value
        val seed=System.currentTimeMillis();val a="Mega Alpha $seed";val b="Mega Beta $seed";val other="Other $seed";val added="Added $seed"
        val custom="Mi consola / test & retro $seed";val ids=listOf(seed,seed+1,seed+2)
        try {
            settings.setCollectionView(CollectionView.GRID)
            repo.save(Game(seed,a,"Mega Drive","SEGA"));repo.save(Game(seed+1,b,"Genesis","SEGA"));repo.save(Game(seed+2,other,"Nintendo Switch","Nintendo"))
            repo.saveHardware(Hardware(seed,"Mi Mega II","Mega Drive II"));repo.saveHardware(Hardware(seed+1,"Mi Genesis","Genesis"));repo.saveHardware(Hardware(seed+2,"Mi consola vacía",custom))
            val expected=systemLibraries(repo.snapshot().games,repo.snapshot().hardware).single{it.key=="mega drive"}
            compose.onNodeWithTag("systems-home").assertExists()
            compose.onNodeWithText("Buscar sistema o plataforma").performTextInput("mega")
            compose.waitUntil(10_000){compose.onAllNodesWithTag("system-card-mega drive").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("system-card-mega drive").performScrollTo().assert(hasText("${expected.gameCount} videojuegos",substring=true)).assert(hasText("${expected.unitCount} unidades registradas",substring=true))
            val dir=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            hideKeyboard()
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

    @Test fun unifiedTabsRetainSearchAndAddEquipmentWithoutMixingPlatforms():Unit=runBlocking {
        val repo=deps.collection();val seed=System.currentTimeMillis();val platform="Laboratorio / PC & $seed"
        val first="Equipo Alpha $seed";val second="Equipo Beta $seed";val added="Equipo Nuevo $seed";val game="Juego Lab $seed";val serial="LAB-SERIAL-$seed"
        try {
            repo.save(Game(seed,game,platform,"Indie",isFavorite=true))
            repo.saveHardware(Hardware(seed,first,platform,HardwareKind.COMPUTER,controllers=listOf(Controller("CPU especial"))))
            repo.saveHardware(Hardware(seed+1,second,platform,HardwareKind.COMPUTER,serialNumber=serial))
            repo.saveHardware(Hardware(seed+2,"Fuera del laboratorio","Otra plataforma $seed"))
            compose.onNodeWithContentDescription("Abrir menú").performClick()
            compose.onNodeWithText("Mis sistemas").assertDoesNotExist()
            compose.onNode(hasText("Sistemas") and hasClickAction()).performClick()
            compose.onNodeWithText("Buscar sistema o plataforma").performTextInput(serial)
            compose.onNodeWithTag("system-card-${SystemKnowledge.family(platform)}").performScrollTo().performClick()
            compose.onNodeWithTag("system-tab-games").assertIsSelected()
            compose.onNodeWithTag("system-games-total").assertTextEquals("1 videojuego en este sistema")
            compose.onNodeWithText("Buscar en tu colección").performTextInput(game)
            compose.onNodeWithTag("system-tab-equipment").performClick()
            compose.onNodeWithTag("system-equipment-total").assertTextEquals("2 equipos registrados")
            compose.onNodeWithText("Buscar en mis equipos").performTextInput(serial)
            compose.onNodeWithTag("equipment-card-${seed+1}").assertExists()
            compose.onNodeWithTag("equipment-card-$seed").assertDoesNotExist()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithTag("system-tab-equipment").assertIsSelected()
            compose.onNodeWithText("Buscar en mis equipos").assert(hasText(serial))
            compose.onNodeWithTag("system-tab-games").performClick()
            compose.onNodeWithText("Buscar en tu colección").assert(hasText(game))
            compose.onNodeWithTag("system-tab-equipment").performClick()
            compose.onNodeWithText("Buscar en mis equipos").assert(hasText(serial))
            hideKeyboard()
            compose.onNodeWithText("Buscar en mis equipos").performTextClearance()
            compose.onNodeWithText("Buscar en mis equipos").assert(hasText("",substring=false))
            val dir=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            compose.waitForIdle();InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->File(dir,"system-equipment.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
            compose.onNodeWithText("+ AÑADIR EQUIPO").performClick()
            compose.onNodeWithText("Plataforma *").assert(hasText(platform))
            compose.onNodeWithText("Modelo / nombre *").performTextInput(added)
            compose.onNodeWithText("GUARDAR SISTEMA").performScrollTo().performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText("3 equipos registrados").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("system-tab-equipment").assertIsSelected()
            compose.onNodeWithText("Buscar en mis equipos").assert(hasText("",substring=false))
            compose.onNodeWithTag("equipment-list").performScrollToNode(hasText(added))
            compose.onNodeWithText(added).performClick()
            compose.onNodeWithText("Eliminar sistema").performScrollTo().performClick()
            compose.onNodeWithText("Eliminar",useUnmergedTree=true).performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText("2 equipos registrados").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("system-tab-games").performClick()
            compose.onNodeWithTag("system-games-total").assertTextEquals("1 videojuego en este sistema")
            assertTrue(repo.snapshot().games.single{it.id==seed}.isFavorite)
        }finally{
            repo.delete(seed);repo.removeArchivedPurchase("GAME:$seed")
            repo.snapshot().hardware.filter{it.id in listOf(seed,seed+1,seed+2) || it.name==added}.forEach{repo.deleteHardware(it.id);repo.removeArchivedPurchase("HARDWARE:${it.id}")}
        }
    }
}
