package com.gamerdex.branding

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import com.gamerdex.R
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.gamerdex.ui.theme.*
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.PathParser

/** Two-layer vector identity shared by the interface and exported player cards. */
@Composable
fun GamerDexLogo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val theme = LocalVisualTheme.current
    val primary = MaterialTheme.colorScheme.primary.toArgb(); val secondary = MaterialTheme.colorScheme.secondary.toArgb()
    ComposeCanvas(modifier.aspectRatio(5.2f).semantics { contentDescription = "Logotipo GamerDex" }) {
        drawIntoCanvas { GamerDexBrand.drawLogo(context, it.nativeCanvas, Rect(0, 0, size.width.toInt(), size.height.toInt()), primary, secondary, theme) }
    }
}

object GamerDexBrand {
    fun drawLogo(context: Context, canvas: Canvas, bounds: Rect, primary: Int, secondary: Int, theme: VisualTheme = VisualTheme.CYBERPUNK) {
        if (theme != VisualTheme.COMPETITIVE) {
            drawWordmark(canvas, bounds, primary, secondary, theme)
            return
        }
        listOf(R.drawable.brand_logo_primary to primary, R.drawable.brand_logo_secondary to secondary).forEach { (resource, color) ->
            requireNotNull(ContextCompat.getDrawable(context, resource)).mutate().apply { setTint(color); this.bounds = bounds; draw(canvas) }
        }
    }
    private val g = requireNotNull(PathParser.createPathFromPathData("M20,18L52,18L40,30L28,30L22,36L22,64L28,70L40,70L40,58L32,58L32,46L52,46L52,82L20,82L8,70L8,30Z"))
    private val d = requireNotNull(PathParser.createPathFromPathData("M58,18H74L92,36V64L74,82H58Z M70,30V70H71L80,61V39L71,30Z")).apply { fillType = android.graphics.Path.FillType.EVEN_ODD }
    private val pixels = mapOf('G' to listOf("01111","10000","10000","10111","10001","10001","01111"),
        'A' to listOf("01110","10001","10001","11111","10001","10001","10001"),
        'M' to listOf("10001","11011","10101","10101","10001","10001","10001"),
        'E' to listOf("11111","10000","10000","11110","10000","10000","11111"),
        'R' to listOf("11110","10001","10001","11110","10100","10010","10001"),
        'D' to listOf("11110","10001","10001","10001","10001","10001","11110"),
        'X' to listOf("10001","10001","01010","00100","01010","10001","10001"))
    private fun drawWordmark(canvas: Canvas, bounds: Rect, primary: Int, secondary: Int, theme: VisualTheme) {
        canvas.save(); canvas.translate(bounds.left.toFloat(), bounds.top.toFloat()); canvas.scale(bounds.width() / 520f, bounds.height() / 100f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = primary; canvas.drawPath(g, paint); paint.color = secondary; canvas.drawPath(d, paint)
        if (theme == VisualTheme.ARCADE || theme == VisualTheme.VOXEL) {
            "GAMERDEX".forEachIndexed { i, char -> pixels.getValue(char).forEachIndexed { row, cells -> cells.forEachIndexed { col, cell -> if (cell == '1') {
                val x = 124f + i * 48 + col * 8; val y = 22f + row * 8
                if (theme == VisualTheme.VOXEL) { paint.color = secondary; paint.alpha = 100; canvas.drawRect(x + 3, y + 4, x + 10, y + 11, paint) }
                paint.color = if (i < 5) primary else secondary; paint.alpha = 255
                canvas.drawRect(x, y, x + 7, y + 7, paint)
            } } } }
        } else if(theme==VisualTheme.SEGA) {
            paint.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);paint.textSize=70f
            paint.textScaleX=368f/paint.measureText("GAMERDEX")
            val baseline=50f-(paint.ascent()+paint.descent())/2
            paint.style=Paint.Style.STROKE;paint.strokeJoin=Paint.Join.ROUND;paint.strokeWidth=2.4f;paint.color=primary
            canvas.drawText("GAMERDEX",124f,baseline,paint)
            paint.style=Paint.Style.FILL;paint.color=secondary
            canvas.drawRect(125f,87f,478f,89f,paint)
        } else if (theme == VisualTheme.POP) {
            paint.typeface = Typeface.create("sans-serif-black", Typeface.BOLD); paint.textSize = 65f
            "GAMERDEX".forEachIndexed { i, char ->
                canvas.save(); val x = 124f + i * 48; canvas.translate(x, if (i % 2 == 0) 73f else 77f)
                canvas.rotate(if (i % 2 == 0) -4f else 3f)
                paint.textScaleX = 42f / paint.measureText(char.toString())
                paint.style = Paint.Style.STROKE; paint.strokeWidth = 4f; paint.color = theme.palette().text.toArgb(); canvas.drawText(char.toString(), 2f, 2f, paint)
                paint.style = Paint.Style.FILL; paint.color = when (i % 3) { 0 -> primary; 1 -> secondary; else -> theme.palette().tertiary.toArgb() }
                canvas.drawText(char.toString(), 0f, 0f, paint); canvas.restore(); paint.textScaleX = 1f
            }
        } else {
            paint.typeface = Typeface.create(when (theme) { VisualTheme.TACTICAL -> "monospace"; VisualTheme.KAWAII -> "sans-serif-rounded"; else -> "sans-serif-condensed" }, Typeface.BOLD)
            paint.textSize = if (theme == VisualTheme.TACTICAL) 60f else 72f
            paint.textSkewX = if (theme == VisualTheme.CYBERPUNK) -.23f else if (theme == VisualTheme.RACING || theme == VisualTheme.ROYALE) -.12f else 0f
            paint.textScaleX = 368f / paint.measureText("GAMERDEX")
            val baseline = 50f - (paint.ascent() + paint.descent()) / 2
            canvas.save()
            if (theme == VisualTheme.CYBERPUNK || theme == VisualTheme.TACTICAL) {
                canvas.clipOutRect(120f, 44f, 515f, 46f); canvas.clipOutRect(120f, 66f, 515f, 68f)
            }
            paint.color = primary; canvas.drawText("GAMER", 124f, baseline, paint)
            val split = 124f + paint.measureText("GAMER")
            paint.color = secondary; canvas.drawText("DEX", split, baseline, paint)
            canvas.restore()
            if (theme == VisualTheme.CYBERPUNK || theme == VisualTheme.RACING) {
                paint.color = primary; canvas.drawRect(134f, 86f, 255f, 89f, paint); canvas.drawRect(262f, 86f, 280f, 89f, paint)
            }
        }
        canvas.restore()
    }
}
