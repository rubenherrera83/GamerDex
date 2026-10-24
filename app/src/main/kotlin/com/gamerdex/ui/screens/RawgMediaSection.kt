package com.gamerdex.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.gamerdex.core.domain.GameTrailer
import com.gamerdex.ui.RawgMediaState
import com.gamerdex.ui.components.Cover
import com.gamerdex.ui.components.SectionLabel

@Composable fun RawgMediaSection(id: Long?, screenshot: (String) -> Unit, linkRawg: () -> Unit, state: RawgMediaState, retry: () -> Unit) {
    SectionLabel("RAWG", "Capturas y vídeos")
    if (id == null) {
        Text("Este juego no está vinculado a RAWG. Edita y usa «Buscar por nombre» para asociar su multimedia.", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = linkRawg) { Text("Vincular con RAWG") }
        return
    }
    var video by remember(id) { mutableStateOf<GameTrailer?>(null) }
    val uri = LocalUriHandler.current
    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    state.error?.let { Text(it); TextButton(onClick = retry) { Text("Reintentar multimedia") } }
    state.media?.let { media ->
        if (media.screenshots.isNotEmpty()) LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(media.screenshots, key = { it }) { image -> Cover(image, "Captura de RAWG", Modifier.width(240.dp).height(140.dp).clickable { screenshot(image) }) }
        }
        media.trailers.forEach { trailer ->
            OutlinedButton(onClick = { video = trailer }, modifier = Modifier.fillMaxWidth()) { Text("▶ ${trailer.title}") }
        }
        if (media.partialFailure) {
            Text("Parte de la multimedia no está disponible. Puede depender de la conexión o del acceso que permita RAWG.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = retry) { Text("Reintentar multimedia") }
        } else if (media.screenshots.isEmpty() && media.trailers.isEmpty()) Text("RAWG no ofrece capturas ni tráileres para este juego.", style = MaterialTheme.typography.bodySmall)
    }
    TextButton(onClick = { uri.openUri("https://rawg.io") }) { Text("Capturas y vídeos proporcionados por RAWG ↗") }
    video?.let { trailer ->
        Dialog(onDismissRequest = { video = null }) {
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(trailer.title, style = MaterialTheme.typography.titleMedium)
                    var player by remember(trailer.url) { mutableStateOf<VideoView?>(null) }
                    var error by remember(trailer.url) { mutableStateOf(false) }
                    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { player?.pause() }
                    if (!error) AndroidView(factory = { context -> VideoView(context).apply {
                        player = this
                        val controller = MediaController(context)
                        controller.setAnchorView(this); setMediaController(controller)
                        setVideoURI(Uri.parse(trailer.url))
                        setOnPreparedListener { controller.show(0) }
                        setOnErrorListener { _, _, _ -> error = true; true }
                    } }, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f), onRelease = { it.stopPlayback(); player = null })
                    else Text("No se pudo reproducir el vídeo. Prueba de nuevo con otra conexión.")
                    TextButton(onClick = { video = null }) { Text("Cerrar vídeo") }
                }
            }
        }
    }
}
