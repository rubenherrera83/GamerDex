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

class UxStatisticsBlockTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private var oldStart=StartScreen.GAMES
    private var oldTheme=VisualTheme.CYBERPUNK
    @get:Rule(order=0) val prepare=object:ExternalResource(){
        override fun before(){oldStart=deps.preferences().startScreen.value;oldTheme=deps.preferences().theme.value;deps.preferences().setStartScreen(StartScreen.GAMES);deps.preferences().select(VisualTheme.CYBERPUNK)}
        override fun after(){deps.preferences().setStartScreen(oldStart);deps.preferences().select(oldTheme)}
    }
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    private fun capture(name:String){
        compose.waitForIdle();Thread.sleep(400)
        val file=File(context.getExternalFilesDir(null),"render-tests/$name");file.parentFile!!.mkdirs()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
    }
    private fun openSpending(){compose.onNodeWithTag("nav-statistics").performClick();compose.onNodeWithTag("statistics-open-spending").performClick()}
    private fun choosePlatform(name:String){compose.onNodeWithTag("spend-filter-Plataforma").performClick();compose.onNodeWithTag("spend-option-Plataforma-$name").performScrollTo().performClick()}
    private fun top(){compose.onNodeWithTag("spending-list").performScrollToIndex(0)}
    private fun amount(currency:String,cents:Long){compose.onNodeWithTag("spending-selected-total-$currency").assertTextEquals("${formatPrice(cents,currency)} $currency")}

    @Test fun fullScreenSpendingKeepsFiltersCurrencyAndUnitBreakdownAcrossNavigation():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis();val platform="RetroLab UX";val unit="Equipo de pruebas UX"
        val gameIds=(2L..5L).map{id+it}
        try{
            repo.saveHardware(Hardware(id,unit,platform,serialNumber="SER-A",pricePaidCents=10000,purchasedOn="2021-01-01"))
            repo.saveHardware(Hardware(id+1,unit,platform,serialNumber="SER-B",pricePaidCents=20000,purchasedOn="2022-01-01"))
            repo.save(Game(id+2,"Juego EUR UX",platform,"Estudio",pricePaidCents=500,purchasedOn="2022-02-01",ownedSystemId=id,isFinished=true,completionPercent=100,isFavorite=true))
            repo.save(Game(id+3,"Juego Japón UX",platform,"Estudio",pricePaidCents=150000,priceCurrency="JPY",purchasedOn="2021-03-01",ownedSystemId=id))
            repo.save(Game(id+4,"Sin fecha UX",platform,"Estudio",pricePaidCents=1000))
            repo.save(Game(id+5,"Sin precio UX",platform,"Estudio",purchasedOn="2021-04-01",ownedSystemId=id))
            compose.onNodeWithTag("nav-statistics").performClick();compose.onNodeWithTag("stat-total-ALL").assertExists();capture("statistics-overview-v15.png")
            compose.onNodeWithTag("statistics-open-spending").performClick();choosePlatform(platform)
            amount("EUR",31500);amount("JPY",150000)
            compose.onNodeWithTag("spending-list").performScrollToNode(hasTestTag("spending-chart"))
            capture("spending-chart-v15.png")
            compose.onNodeWithTag("spending-chart-currency").performScrollTo().performClick();compose.onNodeWithTag("chart-currency-JPY").performClick()
            compose.onNodeWithTag("spending-bar-year-2021").assert(hasAnyDescendant(hasText("${formatPrice(150000,"JPY")} JPY")))
            compose.onNodeWithTag("spend-filter-Año").performClick();compose.onNodeWithTag("spend-option-Año-2021").performClick()
            amount("EUR",10000);amount("JPY",150000);compose.onNodeWithText("1 compra sin precio registrado").assertExists()
            compose.activityRule.scenario.recreate();compose.onNodeWithText("Año: 2021").assertExists();amount("EUR",10000)
            compose.onNodeWithTag("spending-mode-SYSTEM").performScrollTo().performClick()
            compose.onNodeWithTag("spending-list").performScrollToNode(hasTestTag("spending-group-unit-$id"))
            compose.onNodeWithTag("spending-group-unit-$id").assert(hasAnyDescendant(hasText("$unit · SER-A")))
            compose.onNodeWithTag("spending-expand-unit-$id").performScrollTo().performClick();compose.onNodeWithText("2021").assertExists()
            compose.onNodeWithTag("group-unit-$id-2021-total-EUR").assertTextEquals("${formatPrice(10000,"EUR")} EUR")
            compose.onNodeWithTag("group-unit-$id-2021-total-JPY").assertTextEquals("${formatPrice(150000,"JPY")} JPY")
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithTag("statistics-list").assertExists()
            compose.onNodeWithTag("statistics-open-spending").performClick();compose.onNodeWithTag("spending-mode-SYSTEM").assertIsSelected()
            compose.onNodeWithText("Año: 2021").assertExists();compose.onNodeWithTag("spending-expand-unit-$id").assert(hasText("Ocultar desglose anual"))
            compose.onNodeWithTag("nav-vault").performClick();compose.onNodeWithTag("nav-statistics").performClick()
            compose.onNodeWithTag("spending-mode-SYSTEM").assertIsSelected();compose.onNodeWithText("Año: 2021").assertExists()
            compose.activityRule.scenario.onActivity{it.onBackPressedDispatcher.onBackPressed()};compose.onNodeWithTag("statistics-list").assertExists()
        }finally{
            gameIds.forEach{repo.delete(it);repo.removeArchivedPurchase("GAME:$it")}
            listOf(id,id+1).forEach{repo.deleteHardware(it);repo.removeArchivedPurchase("HARDWARE:$it")}
        }
    }

    @Test fun archivedRemovalIsConfirmedRestorableAndLeavesLiveZeroPricePurchaseUntouched():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis();val platform="Archivo UX"
        try{
            deps.preferences().select(VisualTheme.KAWAII)
            repo.save(Game(id,"Compra archivada UX",platform,"Estudio",pricePaidCents=2500,purchasedOn="2024-02-29"));repo.delete(id)
            repo.save(Game(id+1,"Copia de regalo UX",platform,"Estudio",pricePaidCents=0,purchasedOn="2024-03-01"))
            openSpending();choosePlatform(platform);compose.onNodeWithTag("spending-mode-ARCHIVED").performScrollTo().performClick();amount("EUR",2500)
            compose.onNodeWithTag("spending-list").performScrollToNode(hasTestTag("archived-GAME:$id"))
            compose.onNodeWithText("Quitar del historial").performScrollTo().performClick();compose.activityRule.scenario.recreate()
            compose.onNodeWithText("¿Quitar esta compra archivada?").assertExists();capture("spending-archive-confirmation-v15.png")
            compose.onNodeWithText("Cancelar").performClick();assertTrue(repo.snapshot().purchases.any{it.key=="GAME:$id"})
            compose.onNodeWithText("Quitar del historial").performScrollTo().performClick();compose.onNodeWithText("Quitar compra").performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithTag("archived-GAME:$id").fetchSemanticsNodes().isEmpty()}
            assertFalse(repo.snapshot().purchases.any{it.key=="GAME:$id"});assertTrue(repo.snapshot().games.any{it.id==id+1})
            compose.onNodeWithTag("spending-mode-YEAR").performScrollTo().performClick();top();amount("EUR",0)
            compose.onNodeWithTag("spending-list").performScrollToNode(hasTestTag("spending-bar-year-2024"))
            compose.onNodeWithTag("spending-bar-year-2024").assert(hasAnyDescendant(hasText("${formatPrice(0,"EUR")} EUR")))
            assertEquals(0L,repo.snapshot().purchases.single{it.key=="GAME:${id+1}"}.amount)
        }finally{listOf(id,id+1).forEach{repo.delete(it);repo.removeArchivedPurchase("GAME:$it")}}
    }
}
