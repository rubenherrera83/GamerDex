package com.gamerdex.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamerdex.core.domain.*
import com.gamerdex.ui.RawgMediaViewModel
import com.gamerdex.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DetailScreen(game: Game, back: () -> Unit, edit: () -> Unit, delete: () -> Unit,
    gamertag: () -> Unit, favorite: () -> Unit = {}, hardware: List<Hardware> = emptyList()) {
    var confirm by rememberSaveable(game.id) { mutableStateOf(false) }
    var zoomPhoto by rememberSaveable(game.id) { mutableStateOf<String?>(null) }
    val uriHandler=LocalUriHandler.current
    val rawgViewModel:RawgMediaViewModel=hiltViewModel()
    val rawgState by rawgViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(game.rawgId){game.rawgId?.let(rawgViewModel::load) ?: rawgViewModel.clear()}
    val retryRawg:()->Unit={game.rawgId?.let(rawgViewModel::load)}
    val anchors=remember(game.id){listOf("progress","copy","purchase","notes","media").associateWith{DetailAnchor()}}
    val scroll=rememberScrollState()
    Scaffold(containerColor=androidx.compose.ui.graphics.Color.Transparent,topBar={
        Surface {Column {
            TopAppBar(title={Text("Ficha del juego")},navigationIcon={TextButton(back){Text("Volver")}},actions={TextButton(edit){Text("Editar")}})
            DetailJumpBar("game",listOf("progress" to "Partida","copy" to "Tu copia","purchase" to "Compra","notes" to "Notas","media" to "Fotos").map{(id,title)->DetailJump(id,title,anchors.getValue(id))},scroll)
        }}
    }){padding->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).verticalScroll(scroll).testTag("game-detail"),horizontalAlignment=Alignment.CenterHorizontally){
            Column(Modifier.widthIn(max=840.dp).fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
                BoxWithConstraints(Modifier.fillMaxWidth()){
                    val coverWidth=if(maxWidth<340.dp || LocalDensity.current.fontScale>1.3f)88.dp else 120.dp
                    Row(horizontalArrangement=Arrangement.spacedBy(16.dp),verticalAlignment=Alignment.Top){
                        val cover=game.coverUrl ?: game.localCustomPhotos.firstOrNull()
                        Cover(cover,game.title,Modifier.width(coverWidth).height(coverWidth*1.4f).then(if(cover!=null)Modifier.clickable(onClickLabel="Ampliar carátula"){zoomPhoto=cover}else Modifier),contentScale=ContentScale.Fit)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){
                            Text(game.title,style=MaterialTheme.typography.headlineSmall)
                            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                                BrandLogo(game.platform,game.platformLogoUrl,Modifier.size(28.dp),platform=true)
                                Text(game.platform,style=MaterialTheme.typography.bodyMedium)
                            }
                            if(game.company!=UNSPECIFIED_COMPANY)Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                                BrandLogo(game.company,game.companyLogoUrl,Modifier.size(24.dp))
                                Text(game.company,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FilterChip(game.isFavorite,favorite,label={Text(if(game.isFavorite)"★ Favorito" else "☆ Marcar favorito")},modifier=Modifier.testTag("game-detail-favorite"))
                        }
                    }
                }
                OutlinedButton(gamertag,Modifier.fillMaxWidth()){Text("Crear gamertag de este juego")}
                DetailSection("progress","Tu partida",anchors.getValue("progress")){
                    GameRatings(game.rating,rawgState.communityRating,game.rawgId!=null,rawgState.ratingLoading,rawgState.ratingError,retryRawg)
                    Text("${if(game.isFinished)"Finalizado" else "Sin finalizar"} · ${game.completionPercent} % completado",style=MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress={game.completionPercent/100f},modifier=Modifier.fillMaxWidth(),color=MaterialTheme.colorScheme.primary)
                    if(game.hasPlatinum)Text("🏆 Trofeo platino conseguido",color=MaterialTheme.colorScheme.secondary)
                }
                DetailSection("copy","Tu copia",anchors.getValue("copy")){
                    FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                        DetailBadge(game.effectiveEdition.label())
                        DetailBadge(game.contentsLabel,if(game.condition==GameCondition.DIGITAL)null else game.isComplete)
                    }
                    game.specialEditionDetails?.takeIf{it.isNotBlank()}?.let{Text(it)}
                    if(game.condition!=GameCondition.DIGITAL){
                        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                            DetailBadge(if(game.hasBox)"✓ Caja" else "Sin caja",game.hasBox)
                            DetailBadge(if(game.hasManual)"✓ Instrucciones" else "Sin instrucciones",game.hasManual)
                            DetailBadge(if(game.hasMedia)"✓ Juego" else "Sin juego",game.hasMedia)
                        }
                        DetailFact("Estado físico",game.physicalCondition.label())
                    }else DetailFact("Formato","Digital · sin componentes físicos")
                }
                DetailSection("purchase","Compra",anchors.getValue("purchase")){
                    DetailFact("Precio pagado",game.pricePaidCents?.let{formatPrice(it,game.priceCurrency)} ?: "Desconocido")
                    DetailFact("Fecha de compra",game.purchasedOn ?: "Desconocida")
                    game.ownedSystemId?.let{id->DetailFact("Equipo asociado",hardware.firstOrNull{it.id==id}?.let{"${it.name} · ${it.platform}"} ?: "No disponible en el inventario (#$id)")}
                }
                OutlinedCard(Modifier.fillMaxWidth()){
                    Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                        Text("Datos del juego",style=MaterialTheme.typography.titleLarge)
                        if(game.company==UNSPECIFIED_COMPANY)DetailFact("Compañía","No indicada")
                        DetailFact("Año de lanzamiento",game.releaseYear?.toString() ?: "Desconocido")
                        DetailFact("Géneros",game.genres.takeIf{it.isNotEmpty()}?.joinToString(" / ") ?: "Sin indicar")
                        game.catalogUrl?.let{url->TextButton(onClick={uriHandler.openUri(url)}){Text("Catálogo oficial Evercade ↗")}}
                        if(game.coverUrl?.contains("rawg.io")==true)TextButton(onClick={uriHandler.openUri("https://rawg.io")}){Text("Imágenes y metadatos: RAWG ↗")}
                    }
                }
                DetailSection("notes","Notas",anchors.getValue("notes")){
                    Text(game.notes?.takeIf{it.isNotBlank()} ?: "Sin notas sobre este juego.")
                    if(game.notes.isNullOrBlank())TextButton(edit){Text("Añadir notas")}
                }
                DetailSection("media","Fotos y multimedia",anchors.getValue("media")){
                    RawgMediaSection(game.rawgId,{zoomPhoto=it},edit,rawgState,retryRawg)
                    HorizontalDivider()
                    Text("Fotos de tu copia",style=MaterialTheme.typography.titleMedium)
                    if(game.localCustomPhotos.isEmpty()){
                        Text("Aún no hay fotos de tu ejemplar.",style=MaterialTheme.typography.bodyMedium)
                        TextButton(edit){Text("Añadir fotos")}
                    }else DetailPhotoGallery(game.localCustomPhotos,game.title,{zoomPhoto=it})
                }
                HorizontalDivider()
                TextButton(onClick={confirm=true},modifier=Modifier.fillMaxWidth()){Text("Eliminar de la colección",color=MaterialTheme.colorScheme.error)}
            }
        }
    }
    DetailPhotoViewer(zoomPhoto,game.title){zoomPhoto=null}
    if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text("¿Eliminar este juego?")},text={Text("Se eliminará ${game.title} de tu colección. Tus fotos originales y su compra en el historial de gasto se conservarán. Las compras archivadas se pueden quitar desde Estadísticas.")},
        confirmButton={TextButton(onClick={confirm=false;delete()}){Text("Eliminar")}},dismissButton={TextButton(onClick={confirm=false}){Text("Cancelar")}})
}
