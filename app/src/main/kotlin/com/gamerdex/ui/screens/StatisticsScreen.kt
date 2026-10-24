package com.gamerdex.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import com.gamerdex.ui.components.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun StatisticsScreen(games: List<Game>, menu: () -> Unit, open: (CollectionFilter) -> Unit,
    platform: (String) -> Unit, genre: (String) -> Unit,hardware:List<Hardware> = emptyList(),
    purchases:List<Purchase> = emptyList(),removeArchived:(String)->Unit = {}) {
    var spending by rememberSaveable{mutableStateOf(false)}
    val sections=rememberSaveableStateHolder()
    sections.SaveableStateProvider(if(spending)"spending" else "collection"){
        if(spending)SpendingScreen(games,hardware,purchases,removeArchived,{spending=false})
        else CollectionStatistics(games,hardware,purchases,menu,open,platform,genre,{spending=true})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun CollectionStatistics(games:List<Game>,hardware:List<Hardware>,purchases:List<Purchase>,
    menu:()->Unit,open:(CollectionFilter)->Unit,platform:(String)->Unit,genre:(String)->Unit,spending:()->Unit){
    val stats=remember(games){collectionStats(games)}
    Scaffold(containerColor=androidx.compose.ui.graphics.Color.Transparent,
        topBar={TopAppBar(title={Text("Estadísticas")},navigationIcon={TextButton(menu){Text("☰")}},
            actions={TextButton(spending,modifier=Modifier.testTag("statistics-open-spending")){Text("Gasto")}})}){padding->
        LazyColumn(state=rememberLazyListState(),modifier=Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("statistics-list"),
            contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{
                Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text("Tu colección",style=MaterialTheme.typography.headlineSmall)
                    Text("${games.size} ${if(games.size==1)"ejemplar" else "ejemplares"} · ${hardware.size} ${if(hardware.size==1)"equipo registrado" else "equipos registrados"}",style=MaterialTheme.typography.bodyLarge)
                    Text("Toca un contador para ver sus juegos. Cada copia o edición cuenta como un ejemplar.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            CollectionFilter.entries.chunked(2).forEach{pair->item{
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                    pair.forEach{filter->
                        Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface,
                            modifier=Modifier.weight(1f).testTag("stat-total-${filter.name}").clickable(onClickLabel="Ver ${filter.title}"){open(filter)}){
                            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                                Text("${stats.totals.getValue(filter)}",style=MaterialTheme.typography.headlineMedium,color=MaterialTheme.colorScheme.primary)
                                Text(filter.title,style=MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }}
            item{Text("El 100 % de progreso es independiente de tener caja, instrucciones y juego.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            item{SpendingOverview(purchases,spending)}
            item{SectionLabel("01","Por plataforma")}
            if(stats.platforms.isEmpty())item{Text("Añade juegos para empezar tus estadísticas.")}
            stats.platforms.entries.sortedByDescending{it.value}.forEach{(name,count)->item(key="platform-$name"){
                StatBar(name,count,games.size,{platform(name)},platformLogo={BrandLogo(name,games.firstOrNull{it.platform==name}?.platformLogoUrl,Modifier.size(28.dp),platform=true)})
            }}
            item{SectionLabel("02","Por género")}
            item{Text("Un juego puede contar en varios géneros; los porcentajes no tienen por qué sumar 100 %.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            if(stats.genres.isEmpty())item{Text("Indica los géneros de tus juegos para ver este desglose.")}
            stats.genres.entries.sortedByDescending{it.value}.forEach{(name,count)->item(key="genre-$name"){
                StatBar(name,count,games.size,{genre(name)})
            }}
        }
    }
}

@Composable private fun StatBar(name:String,count:Int,total:Int,click:()->Unit,platformLogo:(@Composable ()->Unit)?=null){
    val fraction=if(total==0)0f else (count.toFloat()/total).coerceIn(0f,1f)
    Surface(color=MaterialTheme.colorScheme.surface,shape=MaterialTheme.shapes.small,modifier=Modifier.fillMaxWidth().clickable(onClick=click)){
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
                platformLogo?.invoke();Text(name,Modifier.weight(1f));Text("$count · ${(fraction*100).roundToInt()} %",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelLarge)
            }
            LinearProgressIndicator(progress={fraction},modifier=Modifier.fillMaxWidth())
        }
    }
}
