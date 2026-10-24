package com.gamerdex

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import com.gamerdex.gamertag.*
import com.gamerdex.ui.*
import com.gamerdex.ui.theme.VisualTheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.ExternalResource
import java.io.File

class UxGamertagBlockTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private var oldStart=StartScreen.SYSTEMS
    private var oldTheme=VisualTheme.CYBERPUNK
    private var oldAlias=""
    private var oldLogos=emptyList<GamertagLogo>()
    @get:Rule(order=0) val prepare=object:ExternalResource(){
        override fun before(){val p=deps.preferences();oldStart=p.startScreen.value;oldTheme=p.theme.value;oldAlias=p.alias.value;oldLogos=deps.gamertagSettings().logos.value;p.setStartScreen(StartScreen.SYSTEMS);p.select(VisualTheme.CYBERPUNK);p.setAlias("PLAYER UX");deps.gamertagSettings().setLogos(emptyList())}
        override fun after(){val p=deps.preferences();p.setStartScreen(oldStart);p.select(oldTheme);p.setAlias(oldAlias);deps.gamertagSettings().setLogos(oldLogos)}
    }
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    private fun capture(name:String){
        compose.activityRule.scenario.onActivity{androidx.core.view.WindowCompat.getInsetsController(it.window,it.window.decorView).hide(androidx.core.view.WindowInsetsCompat.Type.ime())}
        Thread.sleep(400);compose.waitForIdle()
        val file=File(context.getExternalFilesDir(null),"render-tests/$name");file.parentFile!!.mkdirs()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{b->file.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)}}
    }
    private fun open(){compose.onNodeWithContentDescription("Abrir menú").performClick();compose.onNodeWithText("Gamertags").performClick()}
    private fun jump(section:String){compose.onNodeWithTag("gamertag-jump-$section").performScrollTo().performClick()}
    private fun ready(){compose.waitUntil(20_000){compose.onAllNodesWithTag("gamertag-share").fetchSemanticsNodes().any{androidx.compose.ui.semantics.SemanticsProperties.Disabled !in it.config}}}
    private fun cover(id:Long):File {
        val file=File(context.cacheDir,"gamertag-ux-$id.png")
        val bitmap=Bitmap.createBitmap(480,700,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bitmap);canvas.drawColor(android.graphics.Color.rgb(14,30,58));val paint=Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color=android.graphics.Color.CYAN;paint.style=Paint.Style.STROKE;paint.strokeWidth=8f
        (1..6).forEach{canvas.drawCircle(240f,340f,it*45f,paint)}
        paint.style=Paint.Style.FILL;paint.textSize=52f;canvas.drawText("NEBULA",85f,180f,paint);canvas.drawText("CIRCUIT",85f,560f,paint)
        file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};return file
    }
    @Test fun navigationHasNoFadeAndPreviewActionsAndIndependentDesignSurviveRecreation():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis();val file=cover(id)
        try {
            repo.save(Game(id,"Nebula Circuit UX","PC","Estudio",coverUrl=Uri.fromFile(file).toString()))
            compose.onNodeWithTag("systems-home").assertExists()
            compose.mainClock.autoAdvance=false
            try {
                compose.onNodeWithTag("nav-vault").performClick();repeat(6){compose.mainClock.advanceTimeByFrame();compose.waitForIdle()}
                compose.onNodeWithTag("collection-grid").assertExists();compose.onNodeWithTag("systems-home").assertDoesNotExist()
                compose.onNodeWithTag("nav-statistics").performClick();repeat(6){compose.mainClock.advanceTimeByFrame();compose.waitForIdle()}
                compose.onNodeWithTag("collection-grid").assertDoesNotExist();compose.onNodeWithTag("statistics-list").assertExists()
            }finally{compose.mainClock.autoAdvance=true}
            open();ready()
            compose.onNodeWithTag("gamertag-share").assertIsDisplayed().assertIsEnabled();compose.onNodeWithTag("gamertag-save").assertIsDisplayed().assertIsEnabled()
            val preview=compose.onNodeWithContentDescription("Vista previa del gamertag").fetchSemanticsNode().boundsInRoot
            val alias=compose.onNodeWithText("Tu gamertag / alias").fetchSemanticsNode().boundsInRoot
            assertTrue(preview.top<alias.top);capture("gamertag-overview-v17.png")
            jump("content");compose.onNodeWithText("Tu gamertag / alias").performTextReplacement("NOVA UX")
            jump("design");compose.onNodeWithTag("gamertag-style-picker").performClick();compose.onNodeWithTag("gamertag-design-KAWAII").performScrollTo().performClick()
            compose.onNodeWithTag("gamertag-current-design").assertTextEquals("Diseño: Kawaii");assertEquals(VisualTheme.CYBERPUNK,deps.preferences().theme.value)
            compose.activityRule.scenario.recreate();compose.onNodeWithTag("gamertag-current-design").assertTextEquals("Diseño: Kawaii")
            compose.onNodeWithTag("gamertag-share").assertIsDisplayed();compose.onNodeWithTag("gamertag-save").assertIsDisplayed()
            jump("content");compose.onNodeWithText("Tu gamertag / alias").assert(hasText("NOVA UX"))
            jump("preview");ready();capture("gamertag-custom-design-v17.png")
            compose.onNodeWithText("Ampliar").performClick();compose.activityRule.scenario.recreate();compose.onNodeWithTag("gamertag-full-preview").assertIsDisplayed()
            compose.onNodeWithText("Cerrar vista previa").performClick();compose.onNodeWithTag("gamertag-full-preview").assertDoesNotExist()
            jump("design");compose.onNodeWithTag("gamertag-follow-theme").performClick().assertIsSelected();compose.onNodeWithTag("gamertag-current-design").assertTextEquals("Diseño: Cyberpunk")
        }finally{repo.delete(id);repo.removeArchivedPurchase("GAME:$id");file.delete()}
    }
    @Test fun targetSearchAndLogoPickerKeepContextWhileActionsStayAvailable():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis();val title="Torre Aurora UX"
        try {
            repo.saveHardware(Hardware(id,title,"PC",HardwareKind.COMPUTER,serialNumber="UX-SER-A",controllers=listOf(Controller("CPU de muestra"),Controller("GPU de muestra",notes="8 GB"))))
            repo.saveHardware(Hardware(id+1,title,"PC",HardwareKind.COMPUTER,serialNumber="UX-SER-B"))
            open();jump("content");compose.onNodeWithTag("gamertag-kind-Sistema / PC").performClick()
            compose.onNodeWithTag("gamertag-target-picker").performScrollTo().performClick();compose.onNodeWithText("Buscar sistema / pc").performTextInput("UX-SER-B")
            compose.activityRule.scenario.recreate();compose.onNodeWithText("Buscar sistema / pc").assert(hasText("UX-SER-B"));compose.onNodeWithTag("gamertag-system-$id").assertDoesNotExist()
            compose.onNodeWithTag("gamertag-system-${id+1}").performClick();jump("components")
            compose.onNodeWithText("Puedes añadir CPU",substring=true).assertExists()
            compose.onNodeWithTag("gamertag-share").assertIsDisplayed();jump("content")
            compose.onNodeWithTag("gamertag-target-picker").performScrollTo().performClick();compose.onNodeWithText("Buscar sistema / pc").performTextInput("UX-SER-A");compose.onNodeWithTag("gamertag-system-$id").performClick()
            jump("components");compose.onNodeWithText("Incluir notas de componentes").performScrollTo().performClick()
            jump("logos");compose.onNodeWithText("Personalizar logotipos (0/6)").performScrollTo().performClick()
            compose.onNodeWithText("Buscar logotipo").performTextInput("no-existe-ux-logo")
            compose.activityRule.scenario.recreate();compose.onNodeWithText("No hay coincidencias.").assertExists();compose.onNodeWithTag("gamertag-logos-done").assertIsDisplayed()
            compose.onNodeWithText("Compañías").performClick();compose.onNodeWithText("Buscar logotipo").performTextInput("Nintendo")
            compose.onNodeWithText("Añadir").performScrollTo().performClick();capture("gamertag-logos-picker-v17.png")
            compose.onNodeWithTag("gamertag-logos-done").performClick();assertEquals("Nintendo",deps.gamertagSettings().logos.value.single().name)
            jump("preview");ready();capture("gamertag-hardware-v17.png")
            compose.onNodeWithTag("gamertag-share").assertIsDisplayed().assertIsEnabled();compose.onNodeWithTag("gamertag-save").assertIsDisplayed().assertIsEnabled()
        }finally{listOf(id,id+1).forEach{repo.deleteHardware(it);repo.removeArchivedPurchase("HARDWARE:$it")}}
    }
}
