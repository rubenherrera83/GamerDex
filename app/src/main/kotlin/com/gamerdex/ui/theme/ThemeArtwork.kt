package com.gamerdex.ui.theme

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

/** Original geometric motifs, shared by Compose panels and exported gamertags. */
object ThemeArtwork {
    fun draw(canvas: Canvas, theme: VisualTheme, bounds: RectF, primary: Int, secondary: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.save()
        canvas.clipRect(bounds)
        canvas.translate(bounds.left, bounds.top)
        canvas.scale(bounds.width() / 400f, bounds.height() / 220f)
        fun line(x: Float, y: Float, xx: Float, yy: Float, color: Int, alpha: Int = 45) {
            paint.color = color; paint.alpha = alpha; paint.strokeWidth = 1.5f
            canvas.drawLine(x, y, xx, yy, paint)
        }
        fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, alpha: Int = 35) {
            paint.color = color; paint.alpha = alpha; paint.style = Paint.Style.FILL
            canvas.drawRect(x, y, x + w, y + h, paint)
        }
        when (theme) {
            VisualTheme.SEGA -> {
                for(i in 0..7)line(0f,155f+i*7,400f,155f+i*7,primary,40)
                paint.color=secondary;paint.alpha=95;paint.style=Paint.Style.STROKE;paint.strokeWidth=3f
                for(i in 0..3)canvas.drawCircle(260f+i*38,75f-i*8,14f,paint)
                paint.color=primary;paint.alpha=60;paint.strokeWidth=2f
                for(i in 0..4)canvas.drawArc(220f+i*5,5f+i*5,410f-i*5,185f-i*5,195f,135f,false,paint)
                paint.style=Paint.Style.FILL
                for(i in 0..9)rect(i*40f,195f,20f,12f,secondary,30)
            }
            VisualTheme.POP -> {
                // Floating platforms, clouds and round tokens; no character artwork.
                for (i in 0..3) {
                    val x = 255f + i * 48
                    rect(x, 175f - i * 22, 38f, 11f, primary, 60)
                    paint.color = secondary; paint.alpha = 38
                    canvas.drawCircle(x + 19f, 147f - i * 22, 8f, paint)
                }
                paint.color = secondary; paint.alpha = 25
                canvas.drawRoundRect(260f, 37f, 375f, 62f, 14f, 14f, paint)
                canvas.drawCircle(294f, 37f, 20f, paint); canvas.drawCircle(329f, 31f, 26f, paint)
                rect(0f, 213f, 400f, 7f, primary, 45)
                // Original bonus block with a diamond insignia and corner rivets.
                rect(304f, 91f, 36f, 36f, android.graphics.Color.rgb(232, 177, 76), 160)
                for (x in listOf(307f, 335f)) for (y in listOf(94f, 122f)) rect(x, y, 2f, 2f, primary, 160)
                val gem = Path().apply { moveTo(322f, 97f); lineTo(330f, 109f); lineTo(322f, 121f); lineTo(314f, 109f); close() }
                paint.color = primary; paint.alpha = 170; canvas.drawPath(gem, paint)
            }
            VisualTheme.ARCADE -> {
                for (i in 0..40) {
                    val x = ((i * 83 + 17) % 400).toFloat(); val y = ((i * 59 + 9) % 220).toFloat()
                    rect(x, y, if (i % 3 == 0) 3f else 1.5f, 2f, secondary, 60)
                }
                for (row in 0..2) for (col in 0..2) {
                    val x = 274f + col * 44; val y = 32f + row * 46
                    // Angular signal satellites, an original five-cell motif.
                    rect(x + 8, y, 8f, 8f, primary, 65); rect(x, y + 8, 24f, 8f, primary, 65)
                    rect(x + 8, y + 16, 8f, 8f, secondary, 65)
                    rect(x - 4, y + 20, 4f, 8f, secondary, 65); rect(x + 24, y + 20, 4f, 8f, secondary, 65)
                }
                for (y in 0..220 step 5) line(0f, y.toFloat(), 400f, y.toFloat(), secondary, 12)
            }
            VisualTheme.VOXEL -> {
                for (col in 0..6) {
                    val x = 240f + col * 26
                    val top = 120f - (col % 3) * 26
                    for (row in 0..6) {
                        val y = top + row * 26
                        rect(x, y, 24f, 24f, if (row == 0) primary else secondary, if (row == 0) 65 else 24 + (col + row) % 3 * 10)
                        rect(x + 4, y + 4, 6f, 6f, primary, 24)
                    }
                }
                for (x in 0..400 step 26) line(x.toFloat(), 0f, x.toFloat(), 220f, primary, 12)
            }
            VisualTheme.ROYALE -> {
                val path = Path().apply { moveTo(325f, -20f); lineTo(400f, 15f); lineTo(360f, 180f); lineTo(245f, 235f); close() }
                paint.color = primary; paint.alpha = 35; canvas.drawPath(path, paint)
                line(350f, 0f, 285f, 220f, secondary, 80)
                for (i in 0..3) {
                    val x = 287f + i * 23; val y = 55f + i * 26
                    val diamond = Path().apply { moveTo(x, y - 12); lineTo(x + 10, y); lineTo(x, y + 12); lineTo(x - 10, y); close() }
                    paint.color = secondary; paint.alpha = 65; canvas.drawPath(diamond, paint)
                }
            }
            VisualTheme.TACTICAL -> {
                paint.color = primary; paint.alpha = 35; paint.style = Paint.Style.STROKE; paint.strokeWidth = 1.2f
                for (i in 0..6) {
                    val path = Path().apply { moveTo(200f + i * 17, -20f); cubicTo(360f - i * 11, 45f, 210f + i * 24, 100f, 390f + i * 17, 240f) }
                    canvas.drawPath(path, paint)
                }
                paint.style = Paint.Style.FILL
                for (i in 0..5) { line(365f, 30f + i * 24, 380f, 30f + i * 24, secondary, 65) }
                line(280f, 35f, 305f, 35f, primary, 85); line(280f, 35f, 280f, 60f, primary, 85)
                line(355f, 165f, 330f, 165f, primary, 85); line(355f, 165f, 355f, 140f, primary, 85)
            }
            else -> Unit
        }
        canvas.restore()
    }
}
