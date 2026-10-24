package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import com.gamerdex.ui.CollectionState
import com.gamerdex.ui.SystemView

import com.gamerdex.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable fun SystemsHomeScreen(state:CollectionState,hardware:List<Hardware>,menu:()->Unit,open:(SystemLibrary)->Unit,allGames:()->Unit,addGame:()->Unit,addEquipment:()->Unit,mode:SystemView=SystemView.CARDS,setMode:(SystemView)->Unit={}) {
    var query by rememberSaveable{mutableStateOf("")}
    val systems=remember(state.games,hardware){systemLibraries(state.games,hardware)}
    val visible=remember(systems,hardware,query){
        val matchingUnits=hardware.filter{(it.name+" "+it.platform+" "+it.serialNumber).contains(query,true)}.map{SystemKnowledge.family(it.platform)}.toSet()
        systems.filter{it.title.contains(query,true) || it.platform.contains(query,true) || it.key.contains(SystemKnowledge.family(query),true) || it.key in matchingUnits}
    }
    Scaffold(containerColor=androidx.compose.ui.graphics.Color.Transparent,topBar={ScreenHeader("Sistemas",menu=menu)},floatingActionButton={ExtendedFloatingActionButton(onClick=addGame){Text("+ AÑADIR JUEGO")}}){padding->
        LazyVerticalGrid(when(mode){SystemView.CARDS->GridCells.Adaptive(300.dp);SystemView.LIST->GridCells.Fixed(1);SystemView.GRID->GridCells.Adaptive(145.dp)},Modifier.fillMaxSize().padding(padding).testTag("systems-home"),contentPadding=PaddingValues(16.dp,8.dp,16.dp,100.dp),verticalArrangement=Arrangement.spacedBy(10.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            item(span={GridItemSpan(maxLineSpan)}){Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
                Text("${systems.size} plataformas · ${state.games.size} videojuegos · ${hardware.size} equipos",style=MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick=allGames){Text("Todos los juegos")};OutlinedButton(onClick=addEquipment){Text("+ AÑADIR EQUIPO")}}
                OutlinedTextField(query,{query=it},label={Text("Buscar sistema o plataforma")},supportingText={Text("También puedes buscar un equipo por nombre o serie.")},singleLine=true,modifier=Modifier.fillMaxWidth())
            }}
            item(span={GridItemSpan(maxLineSpan)}){SystemViewSelector(mode,setMode)}
            if(state.loading)item(span={GridItemSpan(maxLineSpan)}){Shimmer(Modifier.fillMaxWidth().height(180.dp))}
            else if(visible.isEmpty())item(span={GridItemSpan(maxLineSpan)}){
                Text(if(systems.isEmpty())"Añade tu primer juego o registra una consola o PC con Añadir equipo para empezar." else "No hay sistemas que coincidan con la búsqueda.")
            }
            items(visible,key={it.key}){system->
                OutlinedCard(onClick={open(system)},modifier=Modifier.fillMaxWidth().testTag("system-card-${system.key}")){
                    val unit=system.representative
                    @Composable fun image(size:androidx.compose.ui.unit.Dp) {
                        if(unit!=null && (unit.localPhotos.isNotEmpty() || unit.imageUrl!=null))HardwareImage(unit,Modifier.size(size),showCaption=false)
                        else BrandLogo(system.platform,system.logoUrl,Modifier.size(size),platform=true)
                    }
                    val games="${system.gameCount} ${if(system.gameCount==1)"videojuego" else "videojuegos"}"
                    val units="${system.unitCount} ${if(system.unitCount==1)"unidad registrada" else "unidades registradas"}"
                    if(mode==SystemView.GRID)Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        image(44.dp)
                        Text(system.title,style=MaterialTheme.typography.titleSmall,maxLines=2,minLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(games,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)
                        Text(units,style=MaterialTheme.typography.labelSmall)
                    } else Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(if(mode==SystemView.LIST)8.dp else 14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        image(if(mode==SystemView.LIST)40.dp else 64.dp)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(if(mode==SystemView.LIST)2.dp else 5.dp)) {
                            Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically) {
                                Text(system.title,Modifier.weight(1f),style=if(mode==SystemView.LIST)MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,maxLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                if(unit!=null && (unit.localPhotos.isNotEmpty() || unit.imageUrl!=null))BrandLogo(system.platform,system.logoUrl,Modifier.size(24.dp),platform=true)
                            }
                            if(mode==SystemView.LIST)Text("$games · $units",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
                            else {
                                Text(games,style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary)
                                Text(units,style=MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text("›",style=MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
