package com.gamerdex

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import com.gamerdex.ui.StartScreen
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.ExternalResource
import java.io.File

class UxFirstBlockTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private var previous=StartScreen.GAMES
    @get:Rule(order=0) val prepare=object:ExternalResource(){override fun before(){previous=deps.preferences().startScreen.value;deps.preferences().setStartScreen(StartScreen.GAMES)};override fun after(){deps.preferences().setStartScreen(previous)}}
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    private fun capture(name:String){
        compose.activityRule.scenario.onActivity{androidx.core.view.WindowCompat.getInsetsController(it.window,it.window.decorView).hide(androidx.core.view.WindowInsetsCompat.Type.ime())}
        Thread.sleep(400);compose.waitForIdle()
        val dir=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{b->File(dir,name).outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)}}
    }
    private fun systemBack(){compose.activityRule.scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()}}
    @Test fun quickAddRequiresOnlyTitleAndPlatformAndKeepsUnknownCompanyCompatible():Unit=runBlocking {
        val repo=deps.collection();val title="Alta rápida ${System.currentTimeMillis()}"
        try {
            compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
            compose.onNodeWithText("Juego finalizado").assertDoesNotExist()
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").assertIsDisplayed().assertIsNotEnabled()
            capture("editor-quick.png")
            compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithText("¿Guardar los cambios?").assertDoesNotExist()
            compose.onNodeWithTag("collection-grid").assertExists()
            compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
            compose.onNodeWithText("Título *").performTextInput(title)
            compose.onNodeWithText("Plataforma *").performTextInput("PC")
            compose.onNodeWithTag("editor-section-purchase").performScrollTo().performClick()
            compose.onNodeWithText("Fecha de compra (AAAA-MM-DD)").performTextInput("2024-02-29")
            compose.onNodeWithText("Calendario").performClick()
            compose.onNodeWithText("Elegir fecha").assertIsEnabled().performClick()
            compose.onNodeWithText("Fecha de compra (AAAA-MM-DD)").assert(hasText("2024-02-29"))
            compose.onNodeWithText("Fecha de compra (AAAA-MM-DD)").performTextReplacement("2023-02-29")
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").assertIsNotEnabled()
            compose.onNodeWithText("Fecha de compra (AAAA-MM-DD)").performTextClearance()
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").assertIsEnabled().performClick()
            awaitCollectionGame(compose,title)
            val game=repo.snapshot().games.single{it.title==title}
            assertEquals("Sin especificar",game.company);assertNull(game.pricePaidCents)
            compose.onNodeWithTag("collection-grid").performScrollToNode(hasText(title));compose.onNodeWithText(title).performScrollTo().performClick();compose.onNodeWithText("Editar").performClick()
            compose.onNodeWithText("Compañía (opcional)").assert(hasText("",substring=false))
            compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithText("¿Guardar los cambios?").assertDoesNotExist()
        }finally{repo.snapshot().games.filter{it.title==title}.forEach{repo.delete(it.id);repo.removeArchivedPurchase("GAME:${it.id}")}}
    }
    @Test fun gameGuardSurvivesRecreationAndCannotSaveHiddenInvalidFields():Unit=runBlocking {
        val repo=deps.collection();val title="Protección UX ${System.currentTimeMillis()}"
        try {
            compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
            compose.onNodeWithText("Título *").performTextInput(title)
            compose.activityRule.scenario.recreate();systemBack()
            compose.onNodeWithText("Guardar cambios").assertIsNotEnabled()
            compose.onNodeWithText("Seguir editando").performClick()
            compose.onNodeWithText("Título *").assert(hasText(title))
            compose.onNodeWithText("Plataforma *").performTextInput("PC")
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithText("Guardar cambios").performClick()
            awaitCollectionGame(compose,title)
            compose.onNodeWithTag("collection-grid").performScrollToNode(hasText(title));compose.onNodeWithText(title).performScrollTo().performClick();compose.onNodeWithText("Editar").performClick()
            compose.onNodeWithText("Año de lanzamiento").performScrollTo().performTextReplacement("1800")
            compose.onNodeWithTag("editor-section-metadata").performScrollTo().performClick()
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").assertIsDisplayed().assertIsNotEnabled()
            compose.onNodeWithText("Revisa el año",substring=true).assertIsDisplayed()
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithText("Guardar cambios").assertIsNotEnabled()
            capture("editor-unsaved.png")
            compose.onNodeWithText("Seguir editando").performClick()
            compose.onNodeWithTag("editor-section-metadata").performScrollTo().performClick()
            compose.onNodeWithText("Año de lanzamiento").assert(hasText("1800"))
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithText("Descartar cambios").performClick()
            assertNull(repo.snapshot().games.single{it.title==title}.releaseYear)
            compose.onNodeWithText("Editar").performClick()
            compose.onNodeWithContentDescription("Activar valoración personal").performScrollTo().performClick()
            compose.onNodeWithText("Tu nota (0–100)").performScrollTo().performTextReplacement("107")
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").assertIsNotEnabled()
            compose.onNodeWithText("Tu nota (0–100)").performTextReplacement("87,5")
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").assertIsEnabled().performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText("Editar").fetchSemanticsNodes().isNotEmpty()}
            assertEquals(87.5f,repo.snapshot().games.single{it.title==title}.rating)
        }finally{repo.snapshot().games.filter{it.title==title}.forEach{repo.delete(it.id);repo.removeArchivedPurchase("GAME:${it.id}")}}
    }
    @Test fun equipmentGuardKeepsComponentsAndDiscardsInvalidPrice():Unit=runBlocking {
        val repo=deps.collection();val name="Equipo UX ${System.currentTimeMillis()}"
        try {
            compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNode(hasText("Sistemas") and hasClickAction()).performClick()
            compose.onNodeWithText("+ AÑADIR EQUIPO").performClick()
            compose.onNodeWithText("Modelo / nombre *").performTextInput(name);compose.onNodeWithText("Plataforma *").performTextInput("PC")
            compose.onNodeWithText("PC / ordenador").performClick()
            compose.onNodeWithText("+ Añadir componente / accesorio").performScrollTo().performClick()
            compose.onNodeWithText("Componente 1: nombre / modelo").performScrollTo().performTextReplacement("CPU de prueba")
            compose.activityRule.scenario.recreate();compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithText("Seguir editando").performClick()
            compose.onNodeWithText("Componente 1: nombre / modelo").assert(hasText("CPU de prueba"))
            systemBack();compose.onNodeWithText("Guardar cambios").performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithTag("equipment-list").fetchSemanticsNodes().isNotEmpty()}
            val unit=repo.snapshot().hardware.single{it.name==name};assertEquals("CPU de prueba",unit.controllers.single().name)
            compose.onNodeWithTag("equipment-list").performScrollToNode(hasTestTag("equipment-card-${unit.id}"))
            compose.onNodeWithTag("equipment-card-${unit.id}").performClick();compose.onNodeWithText("Editar").performClick()
            compose.onNodeWithText("Precio pagado").performScrollTo().performTextInput("importe inválido")
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithText("Guardar cambios").assertIsNotEnabled()
            compose.onNodeWithText("Descartar cambios").performClick()
            assertNull(repo.snapshot().hardware.single{it.id==unit.id}.pricePaidCents)
        }finally{repo.snapshot().hardware.filter{it.name==name}.forEach{repo.deleteHardware(it.id);repo.removeArchivedPurchase("HARDWARE:${it.id}")}}
    }
    @Test fun compactHomeShowsSeveralPlatformsWithTheirCounts():Unit=runBlocking {
        val repo=deps.collection();val seed=System.currentTimeMillis();val ids=(0..5).map{seed+it}
        try {
            listOf("Atari 2600","Mega Drive","Nintendo Switch","PC","PlayStation 5","Sega Saturn").forEachIndexed{i,p->repo.save(Game(ids[i],"Portada UX $i",p,"Test"))}
            compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNode(hasText("Sistemas") and hasClickAction()).performClick()
            compose.waitForIdle()
            systemLibraries(repo.snapshot().games,repo.snapshot().hardware).take(3).forEach{s->compose.onNodeWithTag("system-card-${s.key}").assertIsDisplayed()}
            capture("systems-compact.png")
        }finally{ids.forEach{repo.delete(it);repo.removeArchivedPurchase("GAME:$it")}}
    }
}
