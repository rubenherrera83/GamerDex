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

import com.gamerdex.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable fun SystemsHomeScreen(state:CollectionState,hardware:List<Hardware>,menu:()->Unit,open:(SystemLibrary)->Unit,allGames:()->Unit,addGame:()->Unit,addEquipment:()->Unit) {
    var query by rememberSaveable{mutableStateOf("")}
    val systems=remember(state.games,hardware){systemLibraries(state.games,hardware)}
    val visible=remember(systems,hardware,query){
        val matchingUnits=hardware.filter{(it.name+" "+it.platform+" "+it.serialNumber).contains(query,true)}.map{SystemKnowledge.family(it.platform)}.toSet()
        systems.filter{it.title.contains(query,true) || it.platform.contains(query,true) || it.key.contains(SystemKnowledge.family(query),true) || it.key in matchingUnits}
    }
    Scaffold(containerColor=androidx.compose.ui.graphics.Color.Transparent,topBar={ScreenHeader("Sistemas",menu=menu)},floatingActionButton={ExtendedFloatingActionButton(onClick=addGame){Text("+ AÑADIR JUEGO")}}){padding->
        LazyVerticalGrid(GridCells.Adaptive(300.dp),Modifier.fillMaxSize().padding(padding).testTag("systems-home"),contentPadding=PaddingValues(16.dp,8.dp,16.dp,100.dp),verticalArrangement=Arrangement.spacedBy(10.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            item(span={GridItemSpan(maxLineSpan)}){Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
                Text("${systems.size} plataformas · ${state.games.size} videojuegos · ${hardware.size} equipos",style=MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick=allGames){Text("Todos los juegos")};OutlinedButton(onClick=addEquipment){Text("+ AÑADIR EQUIPO")}}
                OutlinedTextField(query,{query=it},label={Text("Buscar sistema o plataforma")},supportingText={Text("También puedes buscar un equipo por nombre o serie.")},singleLine=true,modifier=Modifier.fillMaxWidth())
            }}
            if(state.loading)item(span={GridItemSpan(maxLineSpan)}){Shimmer(Modifier.fillMaxWidth().height(180.dp))}
            else if(visible.isEmpty())item(span={GridItemSpan(maxLineSpan)}){
                Text(if(systems.isEmpty())"Añade tu primer juego o registra una consola o PC con Añadir equipo para empezar." else "No hay sistemas que coincidan con la búsqueda.")
            }
            items(visible,key={it.key}){system->
                OutlinedCard(onClick={open(system)},modifier=Modifier.fillMaxWidth().testTag("system-card-${system.key}")){
                    Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                        val unit=system.representative
                        if(unit!=null && (unit.localPhotos.isNotEmpty() || unit.imageUrl!=null))HardwareImage(unit,Modifier.size(64.dp))
                        else BrandLogo(system.platform,system.logoUrl,Modifier.size(52.dp),platform=true)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){
                            Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically){Text(system.title,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,maxLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis);if(unit!=null && (unit.localPhotos.isNotEmpty() || unit.imageUrl!=null))BrandLogo(system.platform,system.logoUrl,Modifier.size(24.dp),platform=true)}
                            Text("${system.gameCount} ${if(system.gameCount==1)"videojuego" else "videojuegos"}",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary)
                            Text("${system.unitCount} ${if(system.unitCount==1)"unidad registrada" else "unidades registradas"}",style=MaterialTheme.typography.bodySmall)
                        }
                        Text("›",style=MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
