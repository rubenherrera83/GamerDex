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

class PcGamertagFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun themesPersistAndPcComponentsCanBeChosenFromDetailAndGlobalEditor():Unit=runBlocking {
        val instrumentation=InstrumentationRegistry.getInstrumentation();val context=instrumentation.targetContext
        val deps=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
        val repo=deps.collection();val prefs=deps.preferences();val previous=prefs.theme.value
        val id=System.currentTimeMillis();val name="Aurora PC [v9 test]";val other="PC sin accesorios [v9 test]"
        try {
            repo.saveHardware(Hardware(id,name,"PC",HardwareKind.COMPUTER,controllers=(0..10).map{Controller(if(it==0)"CPU original" else "Component $it")}))
            repo.saveHardware(Hardware(id+1,other,"PC",HardwareKind.COMPUTER))
            val dir=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            for(theme in listOf(VisualTheme.DESKTOP,VisualTheme.XBOX)) {
                compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNodeWithText("Preferencias").performClick()
                compose.onNodeWithTag("preferences-list").performScrollToNode(hasText(theme.title));compose.onNodeWithText(theme.title).performClick()
                compose.activityRule.scenario.recreate();assertEquals(theme,prefs.theme.value)
                compose.onNodeWithText("☰").performClick();compose.onNodeWithText("Colección").performClick();compose.waitForIdle()
                instrumentation.uiAutomation.takeScreenshot().let{b->File(dir,"vault-${theme.name}.png").outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)}}
            }
            compose.onNodeWithTag("owned-systems-counter").performClick();compose.onNodeWithText("Buscar en mis sistemas").performTextInput("Aurora PC [v9 test]")
            compose.onNode(hasText(name) and hasClickAction() and !hasSetTextAction()).performScrollTo().performClick();compose.onNodeWithText("Editar").performClick()
            compose.onNodeWithText("Componente 1: nombre / modelo").performScrollTo().performTextReplacement("CPU Ryzen 7")
            compose.onNodeWithText("GUARDAR SISTEMA").performScrollTo().performClick()
            compose.onNodeWithText("Crear gamertag de este sistema").performScrollTo().performClick()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("1 × Component 10"))
            compose.onNode(hasText("1 × Component 10") and isToggleable()).assertIsNotEnabled()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("1 × CPU Ryzen 7"))
            compose.onNode(hasText("1 × CPU Ryzen 7") and isToggleable()).performClick()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("1 × Component 10"))
            compose.onNode(hasText("1 × Component 10") and isToggleable()).assertIsEnabled().performClick()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("1 × Component 10"))
            compose.onNode(hasText("1 × Component 10") and isToggleable()).assertIsOn()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("Compartir gamertag"))
            compose.waitUntil(20_000){compose.onAllNodesWithContentDescription("Vista previa del gamertag").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithText("Compartir gamertag").assertIsEnabled()
            compose.onNodeWithText("☰").performClick();compose.onNode(hasText("Gamertags") and hasClickAction()).performClick()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("Sistema / PC"));compose.onNodeWithText("Sistema / PC").performClick()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasTestTag("gamertag-target-picker"))
            compose.onNodeWithTag("gamertag-target-picker").performClick()
            compose.onNodeWithText("Buscar sistema / pc").performTextInput("sin accesorios")
            compose.onNodeWithText("$other · PC · #${id+1}").performClick()
            compose.onNodeWithTag("gamertag-editor").performScrollToNode(hasText("Puedes añadir CPU",substring=true))
            compose.onNodeWithText("Puedes añadir CPU",substring=true).assertExists()
        }finally{
            prefs.select(previous)
            for(unit in listOf(id,id+1)){repo.deleteHardware(unit);repo.removeArchivedPurchase("HARDWARE:$unit")}
        }
    }
}
