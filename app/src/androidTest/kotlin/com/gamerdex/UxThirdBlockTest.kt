package com.gamerdex

import android.graphics.*
import android.net.Uri
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

class UxThirdBlockTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val deps get()=EntryPointAccessors.fromApplication(context,CollectionV3Dependencies::class.java)
    private var previousStart=StartScreen.GAMES
    private var previousMode=CollectionView.GRID
    private var previousTheme=VisualTheme.CYBERPUNK
    @get:Rule(order=0) val prepare=object:ExternalResource(){
        override fun before(){val prefs=deps.preferences();previousStart=prefs.startScreen.value;previousMode=prefs.collectionView.value;previousTheme=prefs.theme.value;prefs.setStartScreen(StartScreen.GAMES);prefs.setCollectionView(CollectionView.GRID);prefs.select(VisualTheme.CYBERPUNK)}
        override fun after(){deps.preferences().apply{setStartScreen(previousStart);setCollectionView(previousMode);select(previousTheme)}}
    }
    @get:Rule(order=1) val compose=createAndroidComposeRule<MainActivity>()
    private fun capture(name:String){
        compose.activityRule.scenario.onActivity{androidx.core.view.WindowCompat.getInsetsController(it.window,it.window.decorView).hide(androidx.core.view.WindowInsetsCompat.Type.ime())}
        Thread.sleep(400);compose.waitForIdle()
        val file=File(context.getExternalFilesDir(null),"render-tests/$name");file.parentFile!!.mkdirs()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let{bitmap->file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
    }
    private fun photo(name:String):File {
        val file=File(context.cacheDir,"detail-$name.png")
        val bitmap=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);val canvas=Canvas(bitmap);val paint=Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader=LinearGradient(0f,0f,480f,640f,intArrayOf(Color.rgb(7,25,51),Color.rgb(16,111,124)),null,Shader.TileMode.CLAMP);canvas.drawPaint(paint);paint.shader=null
        paint.color=Color.rgb(250,237,66);paint.style=Paint.Style.STROKE;paint.strokeWidth=14f;canvas.drawCircle(240f,240f,145f,paint)
        paint.style=Paint.Style.FILL;paint.color=Color.WHITE;paint.textSize=48f;paint.typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD)
        canvas.drawText("NEON ARCHIVE",40f,470f,paint);paint.textSize=28f;canvas.drawText("EDICIÓN DE PRUEBA",40f,525f,paint)
        file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle();return file
    }
    private fun jump(prefix:String,id:String){compose.onNodeWithTag("$prefix-jump-$id").performScrollTo().performClick();compose.onNodeWithTag("detail-heading-$id").assertIsDisplayed()}

    @Test fun gameSectionsKeepCopyPurchaseProgressAndPhotosThroughEditingAndRecreation():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis();val title="Neon Archive · Collector $id";val unit="PS5 de pruebas $id"
        val image=photo("game-$id");val uri=Uri.fromFile(image).toString()
        try{
            repo.saveHardware(Hardware(id,unit,"PlayStation 5"))
            repo.save(Game(id,title,"PlayStation 5","Studio Neon",releaseYear=2025,coverUrl=uri,localCustomPhotos=listOf(uri),
                editionType=EditionType.COLLECTOR,specialEditionDetails="Steelbook y banda sonora",hasManual=false,
                rating=95f,isFinished=true,completionPercent=100,hasPlatinum=true,pricePaidCents=150000,priceCurrency="JPY",
                purchasedOn="2024-07-12",ownedSystemId=id,notes="Comprado en Tokio. Conservar la funda exterior.",genres=listOf("Aventura")))
            compose.onNodeWithText("Buscar en tu colección").performTextInput(title)
            compose.onNodeWithTag("collection-grid").performScrollToNode(hasTestTag("compact-cover-$id"));compose.onNodeWithTag("compact-cover-$id").performClick()
            compose.onNodeWithTag("nav-vault").assertDoesNotExist();capture("game-detail-overview.png")
            compose.onNodeWithTag("game-detail-favorite").performClick()
            compose.waitUntil(5_000){compose.onAllNodesWithText("★ Favorito").fetchSemanticsNodes().isNotEmpty()}
            jump("game","purchase")
            compose.onNodeWithText("Precio pagado: ${formatPrice(150000,"JPY")}").assertIsDisplayed()
            compose.onNodeWithText("Equipo asociado: $unit · PlayStation 5").assertExists()
            jump("game","copy");compose.onNodeWithText("Sin instrucciones").assertExists();compose.onNodeWithText("Steelbook y banda sonora").assertExists()
            jump("game","progress");compose.onNodeWithText("95").assertExists();compose.onNodeWithText("🏆 Trofeo platino conseguido").assertExists()
            compose.activityRule.scenario.recreate();compose.onNodeWithTag("detail-heading-progress").assertIsDisplayed()
            jump("game","notes");compose.onNodeWithText("Comprado en Tokio. Conservar la funda exterior.").assertIsDisplayed()
            compose.onNodeWithText("Editar").performClick();compose.onNodeWithText("Notas y recuerdos").performScrollTo().assert(hasText("Comprado en Tokio. Conservar la funda exterior."))
            compose.onNodeWithText("Volver").performClick();compose.onNodeWithTag("detail-heading-notes").assertIsDisplayed()
            jump("game","media");compose.onNodeWithContentDescription("Foto 1 de $title").performScrollTo();capture("game-detail-media.png")
            compose.onNodeWithContentDescription("Foto 1 de $title").performClick()
            compose.activityRule.scenario.recreate();compose.onNodeWithContentDescription("Foto ampliada de $title").assertIsDisplayed()
            compose.onNodeWithText("Cerrar · Pellizca para ampliar").performClick()
            compose.onNodeWithContentDescription("Foto ampliada de $title").assertDoesNotExist()
            compose.onNodeWithText("Eliminar de la colección").performScrollTo().performClick();compose.activityRule.scenario.recreate()
            compose.onNodeWithText("¿Eliminar este juego?").assertExists();compose.onNodeWithText("Cancelar").performClick()
            val game=repo.snapshot().games.single{it.id==id};assertTrue(game.isFavorite);assertEquals(150000L,game.pricePaidCents);assertEquals(95f,game.rating);assertEquals(100,game.completionPercent);assertFalse(game.hasManual)
            assertTrue(image.exists())
        }finally{repo.delete(id);repo.removeArchivedPurchase("GAME:$id");repo.deleteHardware(id);repo.removeArchivedPurchase("HARDWARE:$id");image.delete()}
    }

    @Test fun hardwareSectionsPreserveComponentsAndAllowGalleryZoomInALightTheme():Unit=runBlocking {
        val repo=deps.collection();val id=System.currentTimeMillis();val name="Aurora PC · Edición personal $id";val platform="Laboratorio PC $id"
        val image=photo("hardware-$id");val uri=Uri.fromFile(image).toString()
        try{
            deps.preferences().select(VisualTheme.KAWAII)
            repo.saveHardware(Hardware(id,name,platform,HardwareKind.COMPUTER,region="Japón",hasBox=true,hasCables=false,working=false,
                serialNumber="AURORA-$id",pricePaidCents=0,currency="JPY",localPhotos=listOf(uri,uri),notes="Revisar la fuente de alimentación.",
                controllers=listOf(Controller("RAM 16 GB",quantity=2,notes="Dos módulos iguales"),Controller("GPU especial",serialNumber="GPU-01"))))
            compose.onNodeWithTag("nav-systems").performClick();compose.onNodeWithText("Buscar sistema o plataforma").performTextInput(name)
            compose.onNodeWithTag("system-card-${SystemKnowledge.family(platform)}").performScrollTo().performClick();compose.onNodeWithTag("system-tab-equipment").performClick()
            compose.onNodeWithTag("equipment-card-$id").performScrollTo().performClick();capture("hardware-detail-overview-v14.png")
            jump("hardware","purchase");compose.onNodeWithText("Precio pagado: ${formatPrice(0,"JPY")}").assertIsDisplayed();compose.onNodeWithText("Fecha de compra: Desconocida").assertExists()
            jump("hardware","status");compose.onNodeWithText("Funcionamiento: No funciona").assertIsDisplayed();compose.onNodeWithText("Sin cables").assertExists()
            jump("hardware","components");compose.onNodeWithText("2 × RAM 16 GB").assertIsDisplayed();compose.onNodeWithText("Serie: GPU-01").assertExists()
            compose.activityRule.scenario.recreate();compose.onNodeWithTag("detail-heading-components").assertIsDisplayed()
            jump("hardware","photos");compose.onNodeWithTag("detail-photo-gallery").performTouchInput{swipeLeft()}
            compose.onNodeWithText("2 / 2 · Desliza o toca para ampliar").assertExists()
            compose.onNodeWithContentDescription("Foto 2 de $name").performScrollTo().performClick();compose.onNodeWithContentDescription("Foto ampliada de $name").assertIsDisplayed()
            compose.onNodeWithText("Cerrar · Pellizca para ampliar").performClick()
            jump("hardware","history");compose.onNodeWithText("Todavía no hay una ficha histórica verificada para esta plataforma.").assertExists()
            compose.onNodeWithText("Eliminar sistema").performScrollTo().performClick();compose.activityRule.scenario.recreate();compose.onNodeWithText("¿Eliminar este sistema?").assertExists();compose.onNodeWithText("Cancelar").performClick()
            assertEquals(2,repo.snapshot().hardware.single{it.id==id}.controllers.first().quantity);assertEquals(0L,repo.snapshot().hardware.single{it.id==id}.pricePaidCents)
        }finally{repo.deleteHardware(id);repo.removeArchivedPurchase("HARDWARE:$id");image.delete()}
    }
}
