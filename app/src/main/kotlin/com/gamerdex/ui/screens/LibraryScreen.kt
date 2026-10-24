package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gamerdex.core.domain.*
import com.gamerdex.ui.CollectionState
import com.gamerdex.ui.CollectionView

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LibraryScreen(games: List<Game>, filter: CollectionFilter, menu: () -> Unit, open: (Long) -> Unit,
    favorite: (Game) -> Unit, mode: CollectionView = CollectionView.GRID, setMode: (CollectionView) -> Unit = {}) {
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { TopAppBar(title = { Text(filter.title) }, navigationIcon = { TextButton(onClick = menu) { Text("☰") } }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            VaultScreen(CollectionState(games.filter(filter::accepts),loading=false), {},open,menu,null,null,{}, {},
                mode,setMode,toggleFavorite=favorite,showHeader=false,allowAdd=false,
                emptyHint=if(filter==CollectionFilter.FAVORITES)"Marca juegos con ★ para encontrarlos aquí." else "Este listado se actualiza con los juegos de tu colección que cumplan su criterio.")
        }
    }
}
