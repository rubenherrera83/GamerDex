package com.gamerdex.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gamerdex.core.domain.*
import com.gamerdex.ui.components.*
import com.gamerdex.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamerdex.ui.RawgMediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DetailScreen(game: Game, back: () -> Unit, edit: () -> Unit, delete: () -> Unit, gamertag: () -> Unit, favorite: () -> Unit = {}) {
    var confirm by remember { mutableStateOf(false) }
    var zoomPhoto by remember { mutableStateOf<String?>(null) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val rawgViewModel: RawgMediaViewModel = hiltViewModel()
    val rawgState by rawgViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(game.rawgId) { game.rawgId?.let(rawgViewModel::load) ?: rawgViewModel.clear() }
    val retryRawg: () -> Unit = { game.rawgId?.let(rawgViewModel::load) }
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, topBar = { TopAppBar(title = { Text("Archivo / %04d".format(game.id)) }, navigationIcon = { TextButton(onClick = back) { Text("Volver") } }, actions = { TextButton(onClick = edit) { Text("Editar") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            Box(Modifier.fillMaxWidth().height(330.dp)) {
                Cover(game.coverUrl, game.title, Modifier.fillMaxSize().blur(28.dp).graphicsLayer { alpha = .35f })
                Cover(game.coverUrl, game.title, Modifier.align(androidx.compose.ui.Alignment.Center).width(210.dp).height(290.dp).neonBorder(game.isSpecialEdition).padding(4.dp))
            }
            Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (game.effectiveEdition != EditionType.STANDARD) Text("✦ ${game.effectiveEdition.label().uppercase()}", color = AcidGreen, style = MaterialTheme.typography.labelLarge)
                Text(game.title, style = MaterialTheme.typography.headlineLarge)
                FilterChip(game.isFavorite, favorite, label = { Text(if (game.isFavorite) "★ Favorito" else "☆ Marcar favorito") })
                Text(listOfNotNull(game.platform, game.company, game.releaseYear?.toString()).joinToString(" · "), color = CyberCyan)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    BrandLogo(game.platform, game.platformLogoUrl, Modifier.size(36.dp), platform = true)
                    Text(game.platform, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    BrandLogo(game.company, game.companyLogoUrl, Modifier.size(36.dp))
                    Text(game.company, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                }
                game.specialEditionDetails?.let { Text(it, color = NeonPurple) }
                if (game.coverUrl?.contains("rawg.io") == true) TextButton(onClick = { uriHandler.openUri("https://rawg.io") }) { Text("Imágenes y metadatos: RAWG ↗") }
                Text(game.contentsLabel, color = MaterialTheme.colorScheme.primary)
                Text(game.physicalCondition.label())
                GameRatings(game.rating, rawgState.communityRating, game.rawgId != null, rawgState.ratingLoading, rawgState.ratingError, retryRawg)
                Text("${if (game.isFinished) "Finalizado" else "Sin finalizar"} · ${game.completionPercent} % completado", style = MaterialTheme.typography.titleMedium)
                if (game.hasPlatinum) Text("🏆 Trofeo platino conseguido", color = MaterialTheme.colorScheme.secondary)
                game.pricePaidCents?.let { Text("Precio pagado: ${com.gamerdex.core.domain.formatPrice(it, game.priceCurrency)}", style = MaterialTheme.typography.titleMedium) }
                Text("Fecha de compra: ${game.purchasedOn ?: "Desconocida"}")
                game.catalogUrl?.let{url->TextButton(onClick={uriHandler.openUri(url)}){Text("Catálogo oficial Evercade ↗")}}
                if (game.genres.isNotEmpty()) Text(game.genres.joinToString(" / "), color = MaterialTheme.colorScheme.secondary)
                OutlinedButton(onClick = gamertag, modifier = Modifier.fillMaxWidth()) { Text("Crear gamertag de este juego") }
                game.notes?.let { Text("NOTAS", style = MaterialTheme.typography.labelSmall, color = CyberCyan); Text(it) }
                RawgMediaSection(game.rawgId, { zoomPhoto = it }, edit, rawgState, retryRawg)
                if (game.localCustomPhotos.isNotEmpty()) {
                    Text("TU COPIA · Desliza para explorar; toca para ampliar", style = MaterialTheme.typography.labelSmall)
                    val pager = rememberPagerState(pageCount = { game.localCustomPhotos.size })
                    HorizontalPager(pager, key = { game.localCustomPhotos[it] }) { page ->
                        Cover(game.localCustomPhotos[page], "Foto ${page + 1} de ${game.title}", Modifier.fillMaxWidth().height(300.dp)
                            .clickable { zoomPhoto = game.localCustomPhotos[page] }, contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                    }
                    Text("${pager.currentPage + 1} / ${game.localCustomPhotos.size}", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = { confirm = true }) { Text("Eliminar de la colección", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
    zoomPhoto?.let { uri ->
        Dialog(onDismissRequest = { zoomPhoto = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            var scale by remember(uri) { mutableFloatStateOf(1f) }
            var offset by remember(uri) { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            Column(Modifier.fillMaxSize().background(VaultBlack).padding(20.dp)) {
                TextButton(onClick = { zoomPhoto = null }) { Text("Cerrar · Pellizca para ampliar") }
                Box(Modifier.fillMaxWidth().weight(1f).pointerInput(uri) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        val maxX = size.width * (scale - 1f) / 2f
                        val maxY = size.height * (scale - 1f) / 2f
                        offset = androidx.compose.ui.geometry.Offset((offset.x + pan.x).coerceIn(-maxX, maxX), (offset.y + pan.y).coerceIn(-maxY, maxY))
                    }
                }) {
                    Cover(uri, "Foto ampliada de ${game.title}", Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }, contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("¿Eliminar este juego?") }, text = { Text("Se eliminará ${game.title} de tu colección. Tus fotos originales y su compra en el historial de gasto se conservarán. Las compras archivadas se pueden quitar desde Estadísticas.") },
        confirmButton = { TextButton(onClick = { confirm = false; delete() }) { Text("Eliminar") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } })
}
