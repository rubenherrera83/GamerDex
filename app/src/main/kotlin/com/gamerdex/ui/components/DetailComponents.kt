package com.gamerdex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Stable class DetailAnchor { var offset by mutableIntStateOf(0) }
data class DetailJump(val id: String, val title: String, val anchor: DetailAnchor)

@Composable fun DetailJumpBar(prefix: String, destinations: List<DetailJump>, scroll:ScrollState) {
    val scope = rememberCoroutineScope()
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=16.dp),
        horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        destinations.forEach { destination ->
            AssistChip(onClick={scope.launch{scroll.animateScrollTo(destination.anchor.offset)}},label={Text(destination.title)},
                modifier=Modifier.testTag("$prefix-jump-${destination.id}"))
        }
    }
}

@Composable fun DetailSection(id: String, title: String, anchor: DetailAnchor, subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth().testTag("detail-section-$id").onGloballyPositioned{anchor.offset=it.positionInParent().y.roundToInt()}) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(title,style=MaterialTheme.typography.titleLarge,
                modifier=Modifier.testTag("detail-heading-$id").semantics{heading()})
            subtitle?.let{Text(it,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            content()
        }
    }
}

@Composable fun DetailFact(label: String, value: String) {
    val labelColor=MaterialTheme.colorScheme.onSurfaceVariant
    Text(buildAnnotatedString {
        withStyle(SpanStyle(fontWeight=FontWeight.Medium,color=labelColor)){append("$label: ")}
        append(value)
    },style=MaterialTheme.typography.bodyLarge)
}

@Composable fun DetailBadge(label: String, positive: Boolean? = null) {
    val colors=MaterialTheme.colorScheme
    Surface(shape=MaterialTheme.shapes.small,color=when(positive){true->colors.secondaryContainer;false->colors.errorContainer;null->colors.surfaceVariant},
        contentColor=when(positive){true->colors.onSecondaryContainer;false->colors.onErrorContainer;null->colors.onSurfaceVariant}) {
        Text(label,Modifier.padding(horizontal=10.dp,vertical=8.dp),style=MaterialTheme.typography.labelMedium)
    }
}

@Composable fun DetailPhotoGallery(photos: List<String>, title: String, open: (String) -> Unit) {
    if(photos.isEmpty())return
    val pager=rememberPagerState(pageCount={photos.size})
    HorizontalPager(pager,modifier=Modifier.testTag("detail-photo-gallery")) { page ->
        Cover(photos[page],"Foto ${page+1} de $title",
            Modifier.fillMaxWidth().height(240.dp).clickable(onClickLabel="Ampliar foto"){open(photos[page])},contentScale=ContentScale.Fit)
    }
    Text("${pager.currentPage+1} / ${photos.size} · Desliza o toca para ampliar",style=MaterialTheme.typography.bodySmall,
        color=MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable fun DetailPhotoViewer(uri: String?, title: String, close: () -> Unit) {
    if(uri==null)return
    Dialog(onDismissRequest=close,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        var scale by rememberSaveable(uri){mutableFloatStateOf(1f)}
        var offset by remember(uri){mutableStateOf(Offset.Zero)}
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding().padding(16.dp)) {
            TextButton(onClick=close){Text("Cerrar · Pellizca para ampliar")}
            Box(Modifier.fillMaxWidth().weight(1f).pointerInput(uri){
                detectTransformGestures{_,pan,zoom,_->
                    scale=(scale*zoom).coerceIn(1f,4f)
                    val maxX=size.width*(scale-1f)/2f;val maxY=size.height*(scale-1f)/2f
                    offset=Offset((offset.x+pan.x).coerceIn(-maxX,maxX),(offset.y+pan.y).coerceIn(-maxY,maxY))
                }
            }) {
                Cover(uri,"Foto ampliada de $title",Modifier.fillMaxSize().graphicsLayer{scaleX=scale;scaleY=scale;translationX=offset.x;translationY=offset.y},contentScale=ContentScale.Fit)
            }
        }
    }
}
