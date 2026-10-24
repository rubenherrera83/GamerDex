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
import org.junit.rules.ExternalResource
import java.io.File

class UxSecondBlockTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private var previous=StartScreen.GAMES
    private var previousMode=CollectionView.GRID
    @get:Rule(order=0) val prepare=object:ExternalResource(){
        override fun before(){previous=deps.preferences().startScreen.value;previousMode=deps.preferences().collectionView.value;deps.preferences().setStartScreen(StartScreen.GAMES);deps.preferences().setCollectionView(CollectionView.GRID)}
        override fun after(){deps.preferences().setStartScreen(previous);deps.preferences().setCollectionView(previousMode)}
    }
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    private fun hideKeyboard(){
        compose.activityRule.scenario.onActivity{androidx.core.view.WindowCompat.getInsetsController(it.window,it.window.decorView).hide(androidx.core.view.WindowInsetsCompat.Type.ime())}
        Thread.sleep(400);compose.waitForIdle()
    }
    private fun capture(name:String){
        hideKeyboard()
        val file=File(context.getExternalFilesDir(null),"render-tests/$name");file.parentFile!!.mkdirs()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
    }
    private fun top(){compose.onNodeWithTag("collection-grid").performScrollToIndex(0)}
    @Test fun combinedFiltersSortingAndNavigationRestoreTheCollection():Unit=runBlocking {
        val repo=deps.collection();val seed=System.currentTimeMillis();val prefix="Archivo UX $seed"
        val ids=(0..35).map{seed+it}
        try{
            ids.forEachIndexed{index,id->repo.save(Game(id,"$prefix ${index.toString().padStart(2,'0')}","PC","Estudio",
                genres=listOf(if(index%2==0)"RPG" else "Acción"),isFavorite=index%3==0,isFinished=index%2==0,
                editionType=if(index%6==0)EditionType.COLLECTOR else EditionType.STANDARD,rating=index.toFloat(),createdAt=id))}
            compose.onNodeWithText("Buscar en tu colección").performTextInput(prefix);hideKeyboard()
            compose.onNodeWithTag("collection-filters").performScrollTo().performClick()
            compose.onNodeWithText("Solo favoritos").performClick();compose.onNodeWithText("Solo finalizados").performClick()
            compose.onNodeWithText("Solo ediciones especiales").performClick()
            compose.onNodeWithTag("filter-platform").performClick();compose.onNodeWithTag("filter-option-Plataforma-PC").performClick()
            compose.onNodeWithTag("filter-genre").performClick();compose.onNodeWithTag("filter-option-Género-RPG").performClick()
            capture("collection-filters.png")
            compose.onNodeWithText("Ver resultados").performClick()
            compose.onNodeWithTag("collection-order").performScrollTo().performClick();compose.onNodeWithText("Tu nota más alta").performClick()
            compose.onNodeWithTag("collection-results").performScrollTo().assertTextEquals("6 de ${repo.snapshot().games.size} juegos")
            top();capture("collection-tools.png")
            compose.onNodeWithTag("nav-statistics").performClick();compose.onNodeWithTag("statistics-list").assertExists()
            compose.onNodeWithTag("nav-vault").performClick()
            top();compose.onNodeWithText("Buscar en tu colección").assert(hasText(prefix))
            compose.onNodeWithTag("collection-order").assert(hasText("Tu nota más alta",substring=true))
            compose.activityRule.scenario.recreate();top()
            compose.onNodeWithText("Favoritos ×").performScrollTo().assertExists()
            compose.onNodeWithTag("collection-grid").performScrollToNode(hasTestTag("compact-cover-${seed+12}"))
            compose.onNodeWithTag("compact-cover-${seed+12}").performClick()
            compose.onNodeWithTag("nav-vault").assertDoesNotExist()
            compose.onNodeWithText("Volver").performClick()
            compose.onNodeWithTag("compact-cover-${seed+12}").assertIsDisplayed()
            top();compose.onNodeWithText("Limpiar filtros").performScrollTo().performClick()
            compose.onNodeWithTag("collection-results").performScrollTo().assertTextEquals("36 de ${repo.snapshot().games.size} juegos")
            top();compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
            compose.onNodeWithTag("nav-systems").assertDoesNotExist()
            compose.onNodeWithText("Título *").performTextInput("Cambios pendientes")
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithText("¿Guardar los cambios?").assertExists()
            compose.onNodeWithText("Descartar cambios").performClick()
            compose.onNodeWithTag("nav-more").performClick();compose.onNodeWithText("Preferencias").assertIsDisplayed()
            compose.onNode(hasText("Juegos") and hasClickAction()).performClick()
            top();compose.onNodeWithText("Buscar en tu colección").assert(hasText("",substring=false))
            compose.onNodeWithTag("collection-filters").assert(hasText("Filtros",substring=false))
            compose.onNodeWithTag("nav-more").performClick();compose.onNode(hasText("Géneros") and hasClickAction()).performClick()
            compose.onNode(hasText("RPG") and hasClickAction()).performScrollTo().performClick()
            top();compose.onNodeWithText("RPG ×").performScrollTo().assertExists()
            compose.onNodeWithText("Favoritos ×").assertDoesNotExist()
            compose.onNodeWithText("RPG ×").performClick();compose.onNodeWithText("RPG ×").assertDoesNotExist()
        }finally{ids.forEach{repo.delete(it);repo.removeArchivedPurchase("GAME:$it")}}
    }
    @Test fun bottomNavigationRestoresSystemSearchAndMoreOpensSecondarySections(){
        compose.onNodeWithTag("nav-systems").performClick()
        capture("navigation-systems.png")
        compose.onNodeWithText("Buscar sistema o plataforma").performTextInput("Sin coincidencia UX");hideKeyboard()
        compose.onNodeWithTag("nav-vault").performClick();compose.onNodeWithTag("collection-grid").assertExists()
        compose.onNodeWithTag("nav-systems").performClick()
        compose.onNodeWithText("Buscar sistema o plataforma").assert(hasText("Sin coincidencia UX"))
        compose.onNodeWithTag("nav-more").performClick();compose.onNodeWithText("Favoritos").performClick()
        compose.onNode(hasText("Favoritos") and !hasClickAction()).assertExists()
        compose.onNodeWithTag("collection-filters").performScrollTo().performClick()
        compose.onNodeWithTag("filter-platform").assertExists();compose.onNodeWithText("Ver resultados").performClick()
    }
}
