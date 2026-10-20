package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import com.gamerdex.ui.theme.VisualTheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import android.graphics.Bitmap
import java.io.File

class SystemsAndSegaFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun segaThemePersistsAndCounterOpensOwnedUnitsWithFactsAndLiveRanking():Unit=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val deps=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
        val repo=deps.collection();val prefs=deps.preferences();val previous=prefs.theme.value
        val id=System.currentTimeMillis();val name="Mega Drive [v8 test]"
        val base=repo.snapshot().hardware.size
        try {
            repo.saveHardware(Hardware(id,name,"Mega Drive"));repo.saveHardware(Hardware(id+1,"Genesis [v8 test]","Genesis"))
            compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNodeWithText("Preferencias").performClick()
            compose.onNodeWithTag("preferences-list").performScrollToNode(hasText("Azul 16-bit"));compose.onNodeWithText("Azul 16-bit").performClick()
            compose.activityRule.scenario.recreate();assertEquals(VisualTheme.SEGA,prefs.theme.value)
            compose.onNodeWithText("☰").performClick();compose.onNodeWithText("Juegos").performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithTag("owned-systems-counter").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("owned-systems-counter").assert(hasText("%02d".format(base+2),substring=true))
            val dir=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            compose.waitForIdle();InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->File(dir,"vault-SEGA.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
            compose.onNodeWithTag("owned-systems-counter").performClick();compose.onNodeWithText("Buscar en mis sistemas").performTextInput("[v8 test]")
            compose.onNodeWithText(name).performScrollTo().performClick()
            compose.onNodeWithText("¿Sabías que…?").performScrollTo().assertExists()
            compose.onNodeWithText("En Norteamérica se llamó Genesis;",substring=true).performScrollTo().assertExists()
            compose.onNodeWithText("Top 5 de la comunidad RAWG").performScrollTo()
            if(BuildConfig.RAWG_API_KEY.isNotBlank()) {
                compose.waitUntil(90_000){compose.onAllNodesWithText("votos",substring=true).fetchSemanticsNodes().any{it.config.toString().contains("/ 100")}}
                compose.onAllNodesWithText("/ 100",substring=true).onFirst().performScrollTo().assertExists()
                compose.waitForIdle();InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->File(dir,"system-insights.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
            }
        }finally{
            prefs.select(previous)
            for(unit in listOf(id,id+1)){repo.deleteHardware(unit);repo.removeArchivedPurchase("HARDWARE:$unit")}
        }
    }
}
