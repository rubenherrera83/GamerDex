package com.gamerdex

import android.graphics.*
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.gamertag.*
import com.gamerdex.ui.theme.VisualTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.Assert.*
import java.io.*
import java.util.UUID

class GamertagCustomizationTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun totalsSixLogosAndCustomImageSurviveExportRecreation():Unit=runBlocking {
        val source=File(context.cacheDir,"test-logo-${UUID.randomUUID()}.png")
        val target=File(context.cacheDir,"test-card-${UUID.randomUUID()}.png")
        try {
            val logo=Bitmap.createBitmap(180,60,Bitmap.Config.ARGB_8888)
            Canvas(logo).drawRect(15f,10f,165f,50f,Paint().apply{color=Color.MAGENTA})
            source.outputStream().use{logo.compress(Bitmap.CompressFormat.PNG,100,it)}
            val input=GamertagInput("PLAYER ONE","MI COLECCIÓN","TODAS MIS AVENTURAS","42 COMPLETOS",null,VisualTheme.CYBERPUNK,1287,12,listOf(
                GamertagLogo("PlayStation 5",kind=GamertagLogoKind.PLATFORM),GamertagLogo("Nintendo Switch",kind=GamertagLogoKind.PLATFORM),
                GamertagLogo("SEGA",kind=GamertagLogoKind.COMPANY),GamertagLogo("Neon Quest",Uri.fromFile(source).toString(),GamertagLogoKind.GAME),
                GamertagLogo("Mi estudio",Uri.fromFile(source).toString()),GamertagLogo("Imagen ausente","file:///missing-gamerdex-test-logo.png")))
            val bytes=ByteArrayOutputStream().also{stream->ObjectOutputStream(stream).use{it.writeObject(input)}}.toByteArray()
            val recovered=ObjectInputStream(ByteArrayInputStream(bytes)).use{it.readObject() as GamertagInput}
            assertEquals(input,recovered)
            val renderer=GamertagRenderer(context)
            val state=SavedStateHandle(mapOf("exportInput" to recovered))
            val vm=GamertagViewModel(renderer,state,GamertagSettings(context))
            vm.export(Uri.fromFile(target))
            withTimeout(20_000){vm.state.first{it.message?.startsWith("Gamertag guardado")==true}}
            val bitmap=BitmapFactory.decodeFile(target.path)
            assertEquals(1600,bitmap.width);assertEquals(900,bitmap.height)
            var magenta=0
            for(y in 748 until 842) for(x in 80 until 1525) if(bitmap.getPixel(x,y)==Color.MAGENTA)magenta++
            assertTrue("Custom artwork must be present in exported PNG",magenta>100)
            val zero=renderer.render(input.copy(totalGames=0,totalSystems=0,logos=emptyList()))
            var changed=0
            for(y in 307 until 350) for(x in 80 until 950) if(bitmap.getPixel(x,y)!=zero.getPixel(x,y))changed++
            assertTrue("Totals must change the pixels in their dedicated band",changed>100)
            val directory=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            target.copyTo(File(directory,"gamertag-customized.png"),overwrite=true)
        } finally { source.delete();target.delete() }
    }
    @Test fun selectionPersistsAndInvalidConfigurationDoesNotBreakRendering() {
        val settings=GamertagSettings(context);val previous=settings.logos.value
        try {
            val logos=listOf(GamertagLogo("Sega Saturn",kind=GamertagLogoKind.PLATFORM),GamertagLogo("Mi juego","content://test/image"))
            settings.setLogos(logos)
            assertEquals(logos,GamertagSettings(context).logos.value)
            assertTrue(GamertagSettings.decode("invalid json").isEmpty())
            assertTrue(GamertagSettings.decode("""[{"name":"Bad","kind":"FUTURE_KIND"}]""").isEmpty())
            assertThrows(IllegalArgumentException::class.java){settings.setLogos((1..7).map{GamertagLogo("Logo $it")})}
            assertEquals(logos,settings.logos.value)
        } finally { settings.setLogos(previous) }
    }
}
