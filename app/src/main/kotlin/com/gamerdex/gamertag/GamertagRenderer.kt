package com.gamerdex.gamertag

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.gamerdex.ui.theme.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.random.Random
import com.gamerdex.branding.GamerDexBrand

data class GamertagInput(val alias: String, val title: String, val subtitle: String, val metrics: String, val cover: String?, val theme: VisualTheme) : java.io.Serializable

class GamertagRenderer @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun render(input: GamertagInput): Bitmap = withContext(Dispatchers.Default) {
        val p = input.theme.palette()
        val art = input.cover?.let { url ->
            val result = SingletonImageLoader.get(context).execute(ImageRequest.Builder(context).data(url).size(700, 900).allowHardware(false).build())
            (result as? SuccessResult)?.image?.toBitmap()
        }
        ensureActive()
        val bitmap = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val background = p.background.toArgb(); val primary = p.primary.toArgb(); val secondary = p.secondary.toArgb(); val text = p.text.toArgb()
        paint.shader = LinearGradient(0f, 0f, 1600f, 900f, intArrayOf(background, p.raised.toArgb(), background), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, 1600f, 900f, paint); paint.shader = null
        decorations(canvas, paint, input.theme, primary, secondary, input.title.hashCode())
        val artPath = Path().apply { moveTo(1120f, 70f); lineTo(1525f, 70f); lineTo(1525f, 770f); lineTo(1430f, 840f); lineTo(1010f, 840f); lineTo(1010f, 185f); close() }
        canvas.save(); canvas.clipPath(artPath)
        paint.color = p.surface.toArgb(); canvas.drawRect(1000f, 60f, 1540f, 850f, paint)
        if (art != null) {
            val scale = maxOf(530f / art.width, 780f / art.height)
            val width = art.width * scale; val height = art.height * scale
            canvas.drawBitmap(art, null, RectF(1265f - width / 2, 450f - height / 2, 1265f + width / 2, 450f + height / 2), paint)
            paint.shader = LinearGradient(1000f, 0f, 1525f, 0f, intArrayOf(background, android.graphics.Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
            canvas.drawRect(1000f, 60f, 1540f, 850f, paint); paint.shader = null
        } else {
            paint.color = secondary; paint.alpha = 70
            for (i in 0..6) canvas.drawCircle(1250f, 420f, 65f + 45 * i, paint.apply { style = Paint.Style.STROKE; strokeWidth = 3f })
            paint.style = Paint.Style.FILL; paint.alpha = 255
        }
        canvas.restore()
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f; paint.color = secondary; canvas.drawPath(artPath, paint); paint.style = Paint.Style.FILL
        GamerDexBrand.drawLogo(context, canvas, Rect(80, 54, 418, 119), primary, secondary, input.theme)
        label(canvas, paint, "// PLAYER CARD", 455f, 99f, 24f, secondary)
        paint.color = primary; canvas.drawRect(80f, 125f, 960f, 132f, paint)
        label(canvas, paint, input.alias.ifBlank { "PLAYER ONE" }.uppercase(), 80f, 235f, 65f, text, 900f)
        label(canvas, paint, "${input.theme.title.uppercase()} / COLLECTION ID", 84f, 287f, 23f, secondary)
        drawWrapped(canvas, paint, input.title.uppercase(), 80f, 400f, 870f, primary)
        label(canvas, paint, input.subtitle, 84f, 650f, 30f, text, 860f)
        paint.color = p.surface.toArgb(); canvas.drawRect(80f, 704f, 950f, 790f, paint)
        paint.color = primary; canvas.drawRect(80f, 704f, 86f, 790f, paint)
        label(canvas, paint, input.metrics, 110f, 758f, 26f, text, 810f)
        label(canvas, paint, "OWN THE GAME. KEEP THE STORY.", 80f, 850f, 20f, secondary)
        label(canvas, paint, "GD / 02", 1340f, 862f, 22f, primary)
        bitmap
    }
    suspend fun write(uri: Uri, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        requireNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
    }
    private fun label(canvas: Canvas, paint: Paint, value: String, x: Float, y: Float, size: Float, color: Int, width: Float = 1500f) {
        paint.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD); paint.textSize = size; paint.color = color; paint.shader = null
        val rendered = android.text.TextUtils.ellipsize(value, android.text.TextPaint(paint), width, android.text.TextUtils.TruncateAt.END).toString()
        canvas.drawText(rendered, x, y, paint)
    }
    private fun drawWrapped(canvas: Canvas, paint: Paint, value: String, x: Float, y: Float, width: Float, color: Int) {
        paint.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD); paint.textSize = 88f; paint.color = color
        var remaining = value
        repeat(3) { line ->
            if (remaining.isNotEmpty()) {
                val count = paint.breakText(remaining, true, width, null).coerceAtLeast(1)
                val split = if (count < remaining.length) remaining.substring(0, count).lastIndexOf(' ').takeIf { it > count / 2 } ?: count else count
                val text = if (line == 2) android.text.TextUtils.ellipsize(remaining, android.text.TextPaint(paint), width, android.text.TextUtils.TruncateAt.END).toString() else remaining.take(split)
                canvas.drawText(text, x, y + line * 92, paint)
                remaining = remaining.drop(split).trimStart()
            }
        }
    }
    private fun decorations(canvas: Canvas, paint: Paint, theme: VisualTheme, primary: Int, secondary: Int, seed: Int) {
        paint.color = secondary; paint.alpha = 26; paint.strokeWidth = 1f
        for (x in 0..1600 step 80) canvas.drawLine(x.toFloat(), 0f, x.toFloat(), 900f, paint)
        for (y in 0..900 step 80) canvas.drawLine(0f, y.toFloat(), 1600f, y.toFloat(), paint)
        when (theme) {
            VisualTheme.POP, VisualTheme.ARCADE, VisualTheme.VOXEL, VisualTheme.ROYALE, VisualTheme.TACTICAL ->
                ThemeArtwork.draw(canvas, theme, RectF(0f, 0f, 1600f, 900f), primary, secondary)
            VisualTheme.CYBERPUNK -> {
                val random = Random(seed); paint.color = secondary; paint.alpha = 30
                for (x in 0..1500 step 80) canvas.drawRect(x.toFloat(), (400 + random.nextInt(300)).toFloat(), x + 60f, 900f, paint)
            }
            VisualTheme.RACING -> {
                paint.color = primary; paint.alpha = 45
                for (i in 0..14) canvas.drawLine(i * 140f, 900f, i * 140f + 600f, 0f, paint.apply { strokeWidth = 20f })
                paint.color = secondary; paint.alpha = 100
                for (x in 0..10) for (y in 0..1) if ((x + y) % 2 == 0) canvas.drawRect(80f + x * 28, 810f + y * 12, 108f + x * 28, 822f + y * 12, paint)
            }
            VisualTheme.COMPETITIVE -> {
                paint.style = Paint.Style.STROKE; paint.color = primary; paint.alpha = 45; paint.strokeWidth = 4f
                canvas.drawCircle(1370f, 400f, 300f, paint); canvas.drawLine(1050f, 400f, 1600f, 400f, paint); paint.style = Paint.Style.FILL
            }
            VisualTheme.KAWAII -> {
                paint.color = primary; paint.alpha = 20
                for (i in 0..12) canvas.drawCircle((i * 173 % 1600).toFloat(), (i * 139 % 900).toFloat(), 55f + i * 4, paint)
                paint.color = secondary; paint.alpha = 70
                val heart = Path().apply { moveTo(880f, 190f); cubicTo(780f, 110f, 840f, 70f, 880f, 115f); cubicTo(920f, 70f, 980f, 110f, 880f, 190f); close() }
                canvas.drawPath(heart, paint)
            }
        }
        paint.alpha = 255; paint.strokeWidth = 1f
    }
}
