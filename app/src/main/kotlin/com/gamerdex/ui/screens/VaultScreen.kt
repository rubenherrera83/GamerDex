package com.gamerdex.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.gamerdex.ui.CollectionState
import com.gamerdex.ui.CollectionView
import com.gamerdex.ui.components.*
import com.gamerdex.ui.theme.*
import com.gamerdex.branding.GamerDexLogo
import com.gamerdex.core.domain.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable fun VaultScreen(state: CollectionState, add: () -> Unit, open: (Long) -> Unit,
    menu: () -> Unit, platform: String?, genre: String?, setPlatform: (String?) -> Unit, clearFilters: () -> Unit,
    mode: CollectionView = CollectionView.LIST, setMode: (CollectionView) -> Unit = {}, titles: () -> Unit = {}, systems: () -> Unit = {}, specials: () -> Unit = {}, toggleFavorite: (com.gamerdex.core.domain.Game) -> Unit = {}, systemCount:Int=0,familyFilter:String?=null,familyTitle:String?=null,back:(()->Unit)?=null,resetSearchKey:Int=0,showHeader:Boolean=true,searchQuery:String?=null,setSearchQuery:((String)->Unit)?=null,allowAdd:Boolean=true,setGenre:(String?)->Unit={},emptyHint:String?=null) {
    var localQuery by rememberSaveable(resetSearchKey) { mutableStateOf("") }
    val query=searchQuery ?: localQuery
    val focus=LocalFocusManager.current
    val keyboard=LocalSoftwareKeyboardController.current
    fun openGame(id:Long){focus.clearFocus();keyboard?.hide();open(id)}
    val familyGames=remember(state.games,familyFilter){if(familyFilter==null)state.games else com.gamerdex.core.domain.gamesForSystem(state.games,familyFilter)}
    val platforms = remember(familyGames) { familyGames.map { it.platform }.distinct().sorted() }
    val genres = remember(familyGames) { familyGames.flatMap { it.genres.ifEmpty { listOf("Sin género") } }.distinct().sorted() }
    var localPlatform by rememberSaveable(resetSearchKey) { mutableStateOf(platform) }
    var localGenre by rememberSaveable(resetSearchKey) { mutableStateOf(genre) }
    val chosenPlatform=if(showHeader)platform else localPlatform
    val chosenGenre=if(showHeader)genre else localGenre
    fun choosePlatform(value:String?){localPlatform=value;setPlatform(value)}
    fun chooseGenre(value:String?){localGenre=value;setGenre(value)}
    var favorites by rememberSaveable(resetSearchKey) { mutableStateOf(false) }
    var finished by rememberSaveable(resetSearchKey) { mutableStateOf(false) }
    var specialsOnly by rememberSaveable(resetSearchKey) { mutableStateOf(false) }
    var order by rememberSaveable { mutableStateOf(CollectionOrder.NEWEST) }
    var filtersOpen by rememberSaveable { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    var appliedResetKey by rememberSaveable { mutableIntStateOf(resetSearchKey) }
    LaunchedEffect(resetSearchKey) {
        // A restored destination can carry older saved state even when its inputs have changed.
        if(appliedResetKey!=resetSearchKey){
            localQuery="";localPlatform=platform;localGenre=genre;favorites=false;finished=false;specialsOnly=false
            filtersOpen=false;appliedResetKey=resetSearchKey;gridState.scrollToItem(0)
        }
    }
    val visible = remember(familyGames,chosenPlatform,chosenGenre,query,favorites,finished,specialsOnly,order) {
        browseCollection(familyGames,query,chosenPlatform,chosenGenre,favorites,finished,specialsOnly,order)
    }
    val activeCount=listOf(chosenPlatform!=null,chosenGenre!=null,favorites,finished,specialsOnly).count{it}
    fun clear(){choosePlatform(null);chooseGenre(null);favorites=false;finished=false;specialsOnly=false;clearFilters()}
    if(filtersOpen) CollectionFilters(platforms,genres,chosenPlatform,chosenGenre,favorites,finished,specialsOnly,
        ::choosePlatform,::chooseGenre,{favorites=it},{finished=it},{specialsOnly=it},::clear,{filtersOpen=false})
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, floatingActionButton = { if(allowAdd)ExtendedFloatingActionButton(onClick = add, containerColor = CyberCyan, contentColor = MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.medium) { Text("+ AÑADIR JUEGO") } }) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        val columns = when (mode) { CollectionView.GRID -> GridCells.Fixed(if (maxWidth >= 600.dp) 4 else 3); CollectionView.COVER_FLOW -> GridCells.Fixed(1); CollectionView.LIST -> GridCells.Adaptive(155.dp) }
        LazyVerticalGrid(columns = columns, state=gridState, modifier = Modifier.fillMaxSize().testTag("collection-grid"), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if(showHeader)item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if(back!=null)TextButton(onClick=back){Text("Volver")}
                    else IconButton(onClick = menu, modifier = Modifier.then(Modifier.semantics { contentDescription = "Abrir menú" })) { Text("☰", color = CyberCyan, style = MaterialTheme.typography.headlineSmall) }
                    Column(Modifier.weight(1f).padding(start = 8.dp)) {
                        GamerDexLogo(Modifier.widthIn(max = 230.dp).fillMaxWidth())
                        Text("TU COLECCIÓN / PLAYER HUB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("◆", color = NeonPurple)
                }
            }
            if(showHeader && familyFilter!=null)item(span={GridItemSpan(maxLineSpan)}) { Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
                Text(familyTitle.orEmpty(),style=MaterialTheme.typography.headlineMedium)
                Text("${familyGames.size} ${if(familyGames.size==1)"videojuego" else "videojuegos"} en este sistema",style=MaterialTheme.typography.titleMedium,modifier=Modifier.testTag("system-games-total"))
            } }
            if(showHeader && familyFilter==null)item(span = { GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Metric("%02d".format(state.games.size), "EJEMPLARES", Modifier.weight(1f).clickable(onClickLabel = "Ver todos los títulos", onClick = titles))
                    Metric("%02d".format(systemCount), "EQUIPOS", Modifier.weight(1f).testTag("owned-systems-counter").clickable(onClickLabel = "Ver sistemas y equipos", onClick = systems))
                    Metric("%02d".format(state.games.count { it.effectiveEdition != com.gamerdex.core.domain.EditionType.STANDARD }), "ESPECIALES", Modifier.weight(1f).clickable(onClickLabel = "Ver ediciones especiales", onClick = specials))
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(query, {value->if(setSearchQuery!=null)setSearchQuery(value) else localQuery=value}, label = { Text("Buscar en tu colección") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium,
                    trailingIcon={if(query.isNotEmpty())IconButton(onClick={if(setSearchQuery!=null)setSearchQuery("")else localQuery=""},modifier=Modifier.semantics{contentDescription="Borrar búsqueda"}){Text("×")}})
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick={filtersOpen=true},modifier=Modifier.testTag("collection-filters")){Text(if(activeCount==0)"Filtros" else "Filtros · $activeCount")}
                    CollectionOrderMenu(order,{order=it})
                }
            }
            if(activeCount>0)item(span={GridItemSpan(maxLineSpan)}){
                FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                    chosenPlatform?.let{name->InputChip(true,{choosePlatform(null)},label={Text("$name ×")})}
                    chosenGenre?.let{name->InputChip(true,{chooseGenre(null)},label={Text("$name ×")})}
                    if(favorites)InputChip(true,{favorites=false},label={Text("Favoritos ×")})
                    if(finished)InputChip(true,{finished=false},label={Text("Finalizados ×")})
                    if(specialsOnly)InputChip(true,{specialsOnly=false},label={Text("Especiales ×")})
                    TextButton(onClick=::clear){Text("Limpiar filtros")}
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Text("${visible.size} de ${familyGames.size} juegos",style=MaterialTheme.typography.labelLarge,modifier=Modifier.testTag("collection-results"))
                    if(familyFilter==null)Text("${state.games.map{SystemKnowledge.family(it.platform)}.distinct().size} plataformas · Recuento por ejemplares",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CollectionView.entries.forEach { candidate -> FilterChip(mode == candidate, { setMode(candidate) }, label = { Text(candidate.label) }) }
                }
            }
            if (state.loading) item(span = { GridItemSpan(maxLineSpan) }) { Shimmer(Modifier.fillMaxWidth().height(200.dp)) }
            else if (visible.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if(!allowAdd && state.games.isEmpty())"No hay juegos en este listado." else if(familyFilter!=null && familyGames.isEmpty())"Aún no tienes juegos en este sistema." else if (state.games.isEmpty()) "Tu leyenda empieza aquí." else "Sin coincidencias.", style = MaterialTheme.typography.titleLarge)
                    Text(if (familyGames.isEmpty()) emptyHint ?: "Añade tu primer juego. Conserva cada edición, cada hallazgo y cada recuerdo." else "Prueba otro título o limpia los filtros.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else if (mode == CollectionView.COVER_FLOW) item(span = { GridItemSpan(maxLineSpan) }) {
                key(chosenPlatform, chosenGenre, query, favorites, finished, specialsOnly, order) { CoverFlow(visible, ::openGame) }
            }
            else items(visible, key = { it.id }) { game ->
                if (mode == CollectionView.GRID) CompactGameCard(game, { toggleFavorite(game) }) { openGame(game.id) }
                else GameCard(game, { toggleFavorite(game) }) { openGame(game.id) }
            }
        }
    }
}
}
