package com.gamerdex

import android.graphics.*
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.core.domain.*
import com.gamerdex.gamertag.*
import com.gamerdex.ui.theme.VisualTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.Assert.*
import java.io.*

class HardwareGamertagTest {
    @Test fun componentsAreSelectableCappedAndPrivateFieldsStayOut() {
        val pc=Hardware(1,"Mi PC","PC",HardwareKind.COMPUTER,serialNumber="PRIVATE-SERIAL",pricePaidCents=123456,purchasedOn="2020-01-02",
            controllers=(0..11).map{Controller("Component $it",if(it==0)2 else 1,serialNumber="PRIVATE-COMPONENT",notes="Optional note")})
        val input=hardwareGamertag("PLAYER",pc,VisualTheme.DESKTOP,listOf(1,0,0,100))
        assertEquals(listOf("1 × Component 1","2 × Component 0"),input.components)
        assertTrue(input.hardwareCard);assertNull(input.cover)
        assertFalse(input.toString().contains("PRIVATE"));assertFalse(input.toString().contains("123456"));assertFalse(input.toString().contains("2020-01-02"));assertFalse(input.toString().contains("Optional note"))
        assertEquals(10,hardwareGamertag("P",pc,VisualTheme.XBOX,(0..11).toList()).components.size)
        assertTrue(hardwareGamertag("P",pc,VisualTheme.DESKTOP,listOf(0),true).components.single().contains("Optional note"))
        assertTrue(hardwareGamertag("P",pc,VisualTheme.DESKTOP,emptyList()).components.isEmpty())
    }
    @Test fun pcCardsWithPhotoTenRowsAndSixLogosSurviveSerializedExport():Unit=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val photo=File(context.cacheDir,"pc-test-photo.png");val target=File(context.cacheDir,"pc-test-card.png")
        val renderer=GamertagRenderer(context)
        try {
            val art=Bitmap.createBitmap(400,600,Bitmap.Config.ARGB_8888)
            Canvas(art).apply{
                drawColor(Color.rgb(18,28,48));val p=Paint(Paint.ANTI_ALIAS_FLAG)
                p.color=Color.rgb(50,65,94);drawRoundRect(45f,20f,355f,580f,20f,20f,p)
                p.color=Color.MAGENTA;drawRect(100f,100f,300f,460f,p)
                p.color=Color.CYAN;for(i in 0..2)drawCircle(200f,160f+i*110f,40f,p)
            }
            photo.outputStream().use{art.compress(Bitmap.CompressFormat.PNG,100,it)}
            val pc=Hardware(1,"AURORA / ESTACIÓN DE JUEGO","PC",HardwareKind.COMPUTER,controllers=listOf(
                Controller("CPU · Ryzen 7 7800X3D"),Controller("GPU · GeForce RTX 4070 SUPER"),Controller("RAM · DDR5 16 GB",2),Controller("SSD · NVMe 2 TB"),Controller("Placa base · B650"),Controller("Fuente · 850 W Gold"),Controller("Refrigeración · torre doble"),Controller("Monitor · 27 pulgadas / 165 Hz"),Controller("Teclado · mecánico"),Controller("Ratón · inalámbrico")),localPhotos=listOf(Uri.fromFile(photo).toString()))
            val dir=File(context.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
            for(theme in listOf(VisualTheme.DESKTOP,VisualTheme.XBOX)) {
                val input=hardwareGamertag("NEKRO PLAYER",pc,theme,pc.controllers.indices.toList()).copy(totalGames=128,totalSystems=7,logos=List(6){GamertagLogo("PC ${it+1}",kind=GamertagLogoKind.PLATFORM)})
                val bytes=ByteArrayOutputStream().also{stream->ObjectOutputStream(stream).use{it.writeObject(input)}}.toByteArray()
                val restored=ObjectInputStream(ByteArrayInputStream(bytes)).use{it.readObject() as GamertagInput};assertEquals(input,restored)
                val vm=GamertagViewModel(renderer,SavedStateHandle(mapOf("exportInput" to restored)),GamertagSettings(context))
                vm.export(Uri.fromFile(target));withTimeout(20_000){vm.state.first{it.message?.startsWith("Gamertag guardado")==true}}
                val bitmap=BitmapFactory.decodeFile(target.path);assertEquals(1600,bitmap.width);assertEquals(900,bitmap.height)
                var magenta=0;for(y in 80 until 820 step 2)for(x in 1030 until 1500 step 2){val pixel=bitmap.getPixel(x,y);if(Color.red(pixel)>80 && Color.blue(pixel)>80 && Color.green(pixel)<60)magenta++}
                assertTrue("PC photo must appear in the actual exported PNG",magenta>100)
                val empty=renderer.render(input.copy(components=emptyList()));var changes=0
                for(y in 530 until 736 step 2)for(x in 80 until 950 step 2)if(bitmap.getPixel(x,y)!=empty.getPixel(x,y))changes++
                assertTrue("Component text must change the component panel",changes>100)
                val share=GamertagSharing.intent(context,bitmap);assertEquals("image/png",share.type)
                target.copyTo(File(dir,"gamertag-pc-${theme.name}.png"),overwrite=true)
            }
        }finally{photo.delete();target.delete()}
    }
}
