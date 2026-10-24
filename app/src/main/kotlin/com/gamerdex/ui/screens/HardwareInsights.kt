package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamerdex.core.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class RankingState(val loading:Boolean=false,val games:List<Metadata> = emptyList(),val error:String?=null)
@HiltViewModel class HardwareInsightsViewModel @Inject constructor(private val metadata:MetadataRepository):ViewModel() {
    private val mutable=MutableStateFlow(RankingState());val state=mutable.asStateFlow()
    private var job:Job?=null;private var loaded:Int?=null
    fun load(platform:Int,force:Boolean=false) {
        if(loaded==platform && !force)return
        job?.cancel();loaded=platform;mutable.value=RankingState(loading=true)
        job=viewModelScope.launch {
            try{mutable.value=RankingState(games=metadata.topForPlatform(platform))}
            catch(e:CancellationException){throw e}
            catch(_:Exception){mutable.value=RankingState(error="No se pudo cargar el top de RAWG. Comprueba la conexión y la configuración de RAWG.")}
        }
    }
}
@Composable fun HardwareInsights(platform:String,viewModel:HardwareInsightsViewModel=hiltViewModel(),framed:Boolean=true) {
    val history=remember(platform){SystemKnowledge.history(platform)}
    val platformId=remember(platform){SystemKnowledge.rawgId(platform)}
    val uri=LocalUriHandler.current
    val ranking by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(platformId){platformId?.let{viewModel.load(it)}}
    val content:@Composable ColumnScope.()->Unit={
        Text("¿Sabías que…?",style=MaterialTheme.typography.titleLarge)
        Text("Datos de la familia de consola, no de esta unidad. Ficha revisada: 18/10/2026.",style=MaterialTheme.typography.bodySmall)
        if(history==null)Text("Todavía no hay una ficha histórica verificada para esta plataforma.")
        else {
            Text(history.family,style=MaterialTheme.typography.titleMedium)
            Text("Unidades: ${history.sales ?: "Sin cifra verificada en esta ficha"}")
            history.salesSource?.let{source->TextButton(onClick={uri.openUri(source)}){Text("Fuente de las cifras ↗")}}
            history.facts.forEach{fact->Text(fact.text);TextButton(onClick={uri.openUri(fact.source)}){Text("Fuente del dato ↗")}}
        }
        HorizontalDivider()
        Text("Top 5 de la comunidad RAWG",style=MaterialTheme.typography.titleMedium)
        Text("Nota global del juego, no exclusiva de esta versión. Mínimo 10 votos; se revisan hasta 200 candidatos ordenados por nota. Puede haber menos de cinco. Los resultados pueden proceder de la caché.",style=MaterialTheme.typography.bodySmall)
        if(platformId==null)Text("Esta plataforma no tiene una equivalencia RAWG verificada para el ranking.")
        else {
            if(ranking.loading)LinearProgressIndicator(Modifier.fillMaxWidth())
            ranking.error?.let{Text(it,color=MaterialTheme.colorScheme.error);TextButton(onClick={viewModel.load(platformId,true)}){Text("Reintentar top RAWG")}}
            if(!ranking.loading && ranking.error==null && ranking.games.isEmpty())Text("Sin juegos con suficientes votos en los candidatos consultados.")
            ranking.games.forEachIndexed { index,game->
                val score=requireNotNull(game.communityRating)
                Text("${index+1}. ${game.title}",style=MaterialTheme.typography.titleSmall)
                Text("${formatPersonalRating(score.score)} / 100 · ${score.votes} votos",style=MaterialTheme.typography.bodySmall)
                TextButton(onClick={uri.openUri("https://rawg.io/games/${game.id}")}){Text("Ver juego en RAWG ↗")}
            }
            TextButton(onClick={uri.openUri("https://rawg.io/games?platforms=$platformId&ordering=-rating")}){Text("Fuente: RAWG ↗")}
        }
    }
    if(framed)OutlinedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)}
    else Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
}
