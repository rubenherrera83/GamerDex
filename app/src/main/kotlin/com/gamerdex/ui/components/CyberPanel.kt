package com.gamerdex.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.gamerdex.ui.theme.LocalVisualTheme
import com.gamerdex.ui.theme.VisualTheme
import com.gamerdex.ui.theme.ThemeArtwork
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

@Composable fun CyberBackdrop(modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val theme = LocalVisualTheme.current
    Canvas(modifier) {
        if (theme in setOf(VisualTheme.POP, VisualTheme.ARCADE, VisualTheme.VOXEL, VisualTheme.ROYALE, VisualTheme.TACTICAL,VisualTheme.SEGA,VisualTheme.DESKTOP,VisualTheme.XBOX)) {
            drawIntoCanvas {
                val height = size.width * .55f
                var top = 0f
                while (top < size.height) {
                    ThemeArtwork.draw(it.nativeCanvas, theme, android.graphics.RectF(0f, top, size.width, top + height), accent.toArgb(), secondary.toArgb())
                    top += height
                }
            }
            return@Canvas
        }
        val step = 36.dp.toPx()
        var x = -size.height
        while (x < size.width) { drawLine(secondary.copy(alpha = .09f), Offset(x, size.height), Offset(x + size.height, 0f), 1f); x += step }
        drawLine(accent.copy(alpha = .7f), Offset(size.width * .76f, 0f), Offset(size.width, size.height * .55f), 3.dp.toPx())
        drawLine(accent.copy(alpha = .35f), Offset(size.width * .81f, 0f), Offset(size.width, size.height * .42f), 1.dp.toPx())
        for (i in 0..5) drawRect(accent.copy(alpha = .55f), Offset(size.width - 24.dp.toPx() - i * 12.dp.toPx(), size.height - 20.dp.toPx()), androidx.compose.ui.geometry.Size(7.dp.toPx(), 3.dp.toPx()))
        when (theme) {
            VisualTheme.RACING -> for (col in 0..5) for (row in 0..3) if ((col + row) % 2 == 0) {
                val unit = 15.dp.toPx(); drawRect(accent.copy(alpha = .15f), Offset(size.width - (col + 1) * unit, row * unit), androidx.compose.ui.geometry.Size(unit, unit))
            }
            VisualTheme.COMPETITIVE -> {
                val center = Offset(size.width * .9f, size.height * .4f)
                drawCircle(accent.copy(alpha = .13f), 60.dp.toPx(), center, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                drawLine(accent.copy(alpha = .13f), center - Offset(80.dp.toPx(), 0f), center + Offset(80.dp.toPx(), 0f), 1.dp.toPx())
                drawLine(accent.copy(alpha = .13f), center - Offset(0f, 80.dp.toPx()), center + Offset(0f, 80.dp.toPx()), 1.dp.toPx())
            }
            VisualTheme.KAWAII -> {
                drawCircle(secondary.copy(alpha = .08f), 100.dp.toPx(), Offset(size.width, size.height))
                val x = size.width * .86f; val y = size.height * .23f; val u = 25.dp.toPx()
                val heart = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x, y + u); cubicTo(x - 2 * u, y - u, x - u, y - 2 * u, x, y - u / 2)
                    cubicTo(x + u, y - 2 * u, x + 2 * u, y - u, x, y + u); close()
                }
                drawPath(heart, accent.copy(alpha = .14f))
            }
            else -> Unit
        }
    }
}

@Composable fun SectionLabel(number: String, title: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(number, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        Text(title.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable fun Metric(value: String, label: String, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    Column(modifier.clip(shape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .5f), shape).padding(12.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
