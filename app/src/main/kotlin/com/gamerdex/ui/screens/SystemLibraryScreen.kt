package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import com.gamerdex.ui.CollectionState
import com.gamerdex.ui.CollectionView
import com.gamerdex.ui.components.ScreenHeader

@Composable fun SystemLibraryScreen(state:CollectionState,hardware:List<Hardware>,family:String,title:String,initialEquipment:Boolean,
    mode:CollectionView,setMode:(CollectionView)->Unit,back:()->Unit,addGame:()->Unit,openGame:(Long)->Unit,
    toggleFavorite:(Game)->Unit,addEquipment:()->Unit,openEquipment:(Long)->Unit) {
    var equipment by rememberSaveable { mutableStateOf(initialEquipment) }
    var gamesQuery by rememberSaveable { mutableStateOf("") }
    var equipmentQuery by rememberSaveable { mutableStateOf("") }
    val savedTabs=rememberSaveableStateHolder()
    val focus=LocalFocusManager.current
    val keyboard=LocalSoftwareKeyboardController.current
    fun leaveInput(action:()->Unit){focus.clearFocus();keyboard?.hide();action()}
    val games=remember(state.games,family){gamesForSystem(state.games,family)}
    val units=remember(hardware,family){hardware.filter{SystemKnowledge.family(it.platform)==family}}
    Scaffold(containerColor=Color.Transparent,topBar={Surface(color=MaterialTheme.colorScheme.surface){Column{
        ScreenHeader(title,back={leaveInput(back)})
        Text("${games.size} ${if(games.size==1)"videojuego" else "videojuegos"} en este sistema",Modifier.padding(horizontal=20.dp).testTag("system-games-total"),style=MaterialTheme.typography.bodyMedium)
        Text("${units.size} ${if(units.size==1)"equipo registrado" else "equipos registrados"}",Modifier.padding(horizontal=20.dp,vertical=6.dp).testTag("system-equipment-total"),style=MaterialTheme.typography.bodyMedium)
        TabRow(selectedTabIndex=if(equipment)1 else 0){
            Tab(!equipment,{leaveInput{equipment=false}},text={Text("Juegos")},modifier=Modifier.testTag("system-tab-games"))
            Tab(equipment,{leaveInput{equipment=true}},text={Text("Mis equipos")},modifier=Modifier.testTag("system-tab-equipment"))
        }
    }}}){padding->Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)){
        savedTabs.SaveableStateProvider(if(equipment)"equipment" else "games"){
            if(equipment)HardwareListScreen(units,{}, {leaveInput(addEquipment)},{id->leaveInput{openEquipment(id)}},equipmentQuery,{equipmentQuery=it},embedded=true)
            else VaultScreen(state,{leaveInput(addGame)},{id->leaveInput{openGame(id)}},{},null,null,{}, {},mode,setMode,toggleFavorite=toggleFavorite,
                familyFilter=family,showHeader=false,searchQuery=gamesQuery,setSearchQuery={gamesQuery=it})
        }
    }}
}
