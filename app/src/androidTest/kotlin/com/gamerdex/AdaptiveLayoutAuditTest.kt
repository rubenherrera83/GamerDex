package com.gamerdex

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
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
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

class AdaptiveLayoutAuditTest {
    private val instrumentation get()=InstrumentationRegistry.getInstrumentation()
    private val context get()=instrumentation.targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private var oldStart=StartScreen.SYSTEMS
    private var oldTheme=VisualTheme.CYBERPUNK
    private var oldMode=CollectionView.GRID
    private var oldFont="1.0"
    private var oldSize:String?=null
    private var oldDensity:String?=null
    private fun shell(command:String):String=instrumentation.uiAutomation.executeShellCommand(command).use{android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText().trim()}
    @get:Rule(order=0) val prepare=object:TestWatcher(){
        override fun starting(description:Description){
            val p=deps.preferences();oldStart=p.startScreen.value;oldTheme=p.theme.value;oldMode=p.collectionView.value;p.setStartScreen(StartScreen.SYSTEMS);p.select(VisualTheme.CYBERPUNK);p.setCollectionView(CollectionView.GRID)
            oldFont=shell("settings get system font_scale").takeIf{it.toFloatOrNull()!=null} ?: "1.0"
            oldSize=Regex("Override size: (\\d+x\\d+)").find(shell("wm size"))?.groupValues?.get(1)
            oldDensity=Regex("Override density: (\\d+)").find(shell("wm density"))?.groupValues?.get(1)
            shell("wm size 960x1920");shell("wm density 480");shell("settings put system font_scale ${if(description.methodName.startsWith("landscape"))"1.0" else "1.6"}")
        }
        override fun finished(description:Description){
            shell("wm size ${oldSize ?: "reset"}");shell("wm density ${oldDensity ?: "reset"}");shell("settings put system font_scale $oldFont")
            val p=deps.preferences();p.setStartScreen(oldStart);p.select(oldTheme);p.setCollectionView(oldMode)
        }
    }
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    private fun capture(name:String){
        compose.waitForIdle();Thread.sleep(400)
        val file=File(context.getExternalFilesDir(null),"render-tests/$name");file.parentFile!!.mkdirs()
        instrumentation.uiAutomation.takeScreenshot().let{b->file.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)}}
    }
    private fun fits(text:String,ancestor:String){
        val layouts=mutableListOf<TextLayoutResult>()
        compose.onNode(hasText(text) and hasAnyAncestor(hasTestTag(ancestor)),useUnmergedTree=true).performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(layouts)}
        assertTrue("No se midió $text",layouts.isNotEmpty());val layout=layouts.first();assertFalse("Texto recortado: $text · tamaño=${layout.size} · líneas=${layout.lineCount} · ancho=${layout.didOverflowWidth} · alto=${layout.didOverflowHeight} · estilo=${layout.layoutInput.style}",layout.hasVisualOverflow)
    }
    private fun menuItem(title:String){compose.onNodeWithTag("drawer-list").performScrollToNode(hasText(title));compose.onNodeWithText(title).performClick()}
    @Test fun largeTextKeepsNavigationDrawerCollectionAndBackupReachable():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis()
        try {
            (1L..4L).forEach{repo.save(Game(id+it,"Aventura de prueba con un título largo $it","Nintendo Switch","Estudio",createdAt=id+it))}
            compose.onNodeWithTag("systems-home").assertExists();capture("audit-large-systems-v18.png")
            fits("Estadísticas","nav-statistics")
            compose.onNodeWithContentDescription("Abrir menú").performClick();capture("audit-large-drawer-v18.png");menuItem("Preferencias")
            compose.onNodeWithTag("preferences-jump-backup").performScrollTo().performClick()
            compose.onNodeWithTag("backup-export").performScrollTo().assertIsDisplayed();compose.onNodeWithTag("backup-import").performScrollTo().assertIsDisplayed()
            deps.preferences().select(VisualTheme.KAWAII)
            compose.onNodeWithTag("nav-vault").performClick()
            compose.onNodeWithTag("collection-grid").performScrollToNode(hasTestTag("collection-view-GRID"))
            compose.onNodeWithTag("collection-view-GRID").assertIsSelected()
            compose.onNodeWithTag("collection-grid").performScrollToKey(id+4)
            val first=compose.onNodeWithTag("compact-cover-${id+4}").fetchSemanticsNode().boundsInRoot
            val second=compose.onNodeWithTag("compact-cover-${id+3}").fetchSemanticsNode().boundsInRoot
            assertEquals(first.top,second.top,2f);assertTrue("Carátulas demasiado estrechas",first.width/context.resources.displayMetrics.density>=120f)
            capture("audit-large-grid-v18.png")
            compose.onNodeWithTag("nav-statistics").performClick();compose.onNodeWithTag("stat-total-ALL").assertExists();capture("audit-large-statistics-v18.png")
        }finally{(1L..4L).forEach{repo.delete(id+it);repo.removeArchivedPurchase("GAME:${id+it}")}}
    }

    @Test fun largeTextEditorCanSaveAfterUnsavedGuard():Unit=runBlocking {
        val repo=deps.collection();val title="Alta accesible ${System.currentTimeMillis()}"
        try {
            compose.onNodeWithText("+ AÑADIR JUEGO").performClick()
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").assertIsDisplayed().assertIsNotEnabled()
            compose.onNodeWithText("Título *").performScrollTo().performTextInput(title)
            compose.onNodeWithText("Plataforma *").performScrollTo().performTextInput("PC")
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").assertIsDisplayed().assertIsEnabled();capture("audit-large-editor-keyboard-v18.png")
            compose.activityRule.scenario.onActivity{androidx.core.view.WindowCompat.getInsetsController(it.window,it.window.decorView).hide(androidx.core.view.WindowInsetsCompat.Type.ime())}
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithText("Seguir editando").performScrollTo().performClick()
            compose.onNodeWithText("Título *").performScrollTo().assert(hasText(title))
            compose.onNodeWithText("GUARDAR EN LA BÓVEDA").performClick()
            compose.waitUntil(10_000){runBlocking{repo.snapshot().games.any{it.title==title}}}
            assertEquals("PC",repo.snapshot().games.single{it.title==title}.platform)
        }finally{repo.snapshot().games.filter{it.title==title}.forEach{repo.delete(it.id);repo.removeArchivedPurchase("GAME:${it.id}")}}
    }
    @Test fun landscapeKeepsSpendingViewportAndMenuUsable():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis()
        try {
            repo.save(Game(id,"Compra para revisión horizontal","PC","Estudio",pricePaidCents=1200,purchasedOn="2024-01-01"))
            compose.runOnUiThread{compose.activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}
            compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE}
            compose.onNodeWithTag("nav-statistics").performClick();compose.onNodeWithTag("statistics-open-spending").performClick();capture("audit-landscape-spending-v18.png")
            val viewport=compose.onNodeWithTag("spending-list").fetchSemanticsNode().boundsInRoot
            assertTrue("El contenido tiene menos de 120 dp de alto",viewport.height/context.resources.displayMetrics.density>=120f)
            compose.onNodeWithTag("spending-list").performScrollToNode(hasTestTag("spend-filter-Año"))
            compose.onNodeWithTag("spend-filter-Año").performClick();compose.onNodeWithTag("spend-option-Año-2024").performScrollTo().performClick()
            compose.onNodeWithTag("spending-list").performScrollToNode(hasTestTag("spending-selected-total-EUR"))
            compose.onNodeWithTag("spending-selected-total-EUR").assertExists()
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithContentDescription("Abrir menú").performClick();menuItem("Gamertags")
            compose.onNodeWithTag("gamertag-share").assertIsDisplayed();compose.onNodeWithTag("gamertag-save").assertIsDisplayed();capture("audit-landscape-gamertag-v18.png")
        }finally{
            compose.runOnUiThread{compose.activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
            repo.delete(id);repo.removeArchivedPurchase("GAME:$id")
        }
    }
}
