package com.gamerdex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.Game
import com.gamerdex.core.domain.EditionType
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/** Center-snapping cover carousel with perspective, reflection and accessible controls. */
@Composable
fun CoverFlow(games: List<Game>, open: (Long) -> Unit) {
    if (games.isEmpty()) return
    val pager = rememberPagerState(pageCount = { games.size })
    val scope = rememberCoroutineScope()
    val camera = with(LocalDensity.current) { 18.dp.toPx() }
    val selected = games[pager.currentPage.coerceIn(games.indices)]
    Column(Modifier.fillMaxWidth().testTag("cover-flow"), horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val coverWidth = minOf(maxWidth * .64f, 250.dp)
            HorizontalPager(state = pager, contentPadding = PaddingValues(horizontal = (maxWidth - coverWidth) / 2),
                pageSpacing = (-22).dp, key = { games[it].id }, modifier = Modifier.fillMaxWidth().height(342.dp)) { page ->
                val game = games[page]
                Column(Modifier.fillMaxWidth().graphicsLayer {
                    val offset = ((pager.currentPage - page) + pager.currentPageOffsetFraction).coerceIn(-1f, 1f)
                    rotationY = offset * 34f
                    scaleX = 1f - offset.absoluteValue * .18f; scaleY = scaleX
                    alpha = 1f - offset.absoluteValue * .35f
                    cameraDistance = camera
                }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().height(276.dp).clip(MaterialTheme.shapes.medium)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .65f), MaterialTheme.shapes.medium)
                        .clickable {
                            if (pager.currentPage == page) open(game.id) else scope.launch { pager.animateScrollToPage(page) }
                        }.semantics { contentDescription = "Carátula de ${game.title}" }) {
                        Cover(game.coverUrl ?: game.localCustomPhotos.firstOrNull(), game.title, Modifier.fillMaxSize())
                        if (game.effectiveEdition != EditionType.STANDARD) Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                            Text(if (game.effectiveEdition == EditionType.COLLECTOR) "★ COLECCIONISTA" else "✦ EDICIÓN ESPECIAL", Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(54.dp).clipToBounds().clearAndSetSemantics { }) {
                        Cover(game.coverUrl ?: game.localCustomPhotos.firstOrNull(), game.title, Modifier.fillMaxWidth().height(276.dp).graphicsLayer { scaleY = -1f; alpha = .19f })
                        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background.copy(alpha = .2f), MaterialTheme.colorScheme.background))))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, enabled = pager.currentPage > 0,
                modifier = Modifier.semantics { contentDescription = "Juego anterior" }) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(selected.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrandLogo(selected.platform, selected.platformLogoUrl, Modifier.size(24.dp), platform = true)
                    Text(selected.platform, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
                Text("${pager.currentPage + 1} / ${games.size}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            TextButton(onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }, enabled = pager.currentPage < games.lastIndex,
                modifier = Modifier.semantics { contentDescription = "Juego siguiente" }) { Text("›", style = MaterialTheme.typography.headlineMedium) }
        }
        TextButton(onClick = { open(selected.id) }) { Text("Abrir juego") }
    }
}
