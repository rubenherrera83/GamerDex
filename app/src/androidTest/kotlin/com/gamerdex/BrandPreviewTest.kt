package com.gamerdex

import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gamerdex.branding.GamerDexBrand
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Exports actual Android drawables at print and launcher sizes for visual inspection. */
@RunWith(AndroidJUnit4::class)
class BrandPreviewTest {
    @Test fun exportBrandAssets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "branding").apply { mkdirs() }
        fun save(name: String, bitmap: Bitmap) {
            File(directory, name).outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        }
        val primary = Color.rgb(245, 239, 38)
        val secondary = Color.rgb(0, 230, 239)
        val icon = requireNotNull(ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
        assertTrue(icon is AdaptiveIconDrawable)
        val iconBitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        icon.bounds = Rect(0, 0, 1024, 1024); icon.draw(Canvas(iconBitmap))
        save("gamerdex-icon.png", iconBitmap)
        val logo = Bitmap.createBitmap(2080, 400, Bitmap.Config.ARGB_8888)
        GamerDexBrand.drawLogo(context, Canvas(logo), Rect(0, 0, 2080, 400), primary, secondary)
        save("gamerdex-logo.png", logo)
        val board = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(board)
        canvas.drawColor(Color.rgb(8, 13, 19))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = secondary; typeface = Typeface.create("monospace", Typeface.NORMAL); textSize = 22f }
        canvas.drawText("GAMERDEX  /  VISUAL IDENTITY  /  2026", 72f, 75f, paint)
        icon.bounds = Rect(72, 150, 392, 470); icon.draw(canvas)
        GamerDexBrand.drawLogo(context, canvas, Rect(470, 220, 1510, 420), primary, secondary)
        paint.color = Color.rgb(147, 167, 180); paint.textSize = 20f
        canvas.drawText("COLLECT. PLAY. KEEP THE STORY.", 486f, 466f, paint)
        canvas.drawLine(72f, 550f, 1528f, 550f, paint)
        val masks = listOf("CIRCLE", "SQUIRCLE", "THEMED", "48 PX")
        masks.forEachIndexed { index, label ->
            val x = 80 + index * 380
            canvas.save()
            if (index == 0) canvas.clipPath(Path().apply { addCircle(x + 64f, 680f, 64f, Path.Direction.CW) })
            else if (index == 1) canvas.clipPath(Path().apply { addRoundRect(x.toFloat(), 616f, x + 128f, 744f, 30f, 30f, Path.Direction.CW) })
            if (index == 2) {
                paint.color = Color.rgb(39, 53, 64); canvas.drawCircle(x + 64f, 680f, 64f, paint)
                val mono = requireNotNull(ContextCompat.getDrawable(context, R.drawable.ic_gamerdex_monochrome)).mutate()
                mono.setTint(secondary); mono.bounds = Rect(x - 32, 584, x + 160, 776); mono.draw(canvas)
            } else if (index == 3) {
                val small = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
                icon.bounds = Rect(0, 0, 48, 48); icon.draw(Canvas(small))
                canvas.drawBitmap(small, x + 40f, 656f, null)
            } else { icon.bounds = Rect(x, 616, x + 128, 744); icon.draw(canvas) }
            canvas.restore()
            paint.color = Color.rgb(147, 167, 180); paint.textSize = 18f
            canvas.drawText(label, x.toFloat(), 800f, paint)
        }
        save("gamerdex-brand-board.png", board)
    }
}
