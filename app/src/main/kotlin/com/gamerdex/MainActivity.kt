package com.gamerdex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gamerdex.ui.CollectionViewModel
import com.gamerdex.ui.screens.*
import com.gamerdex.ui.theme.GamerDexTheme
import com.gamerdex.ui.theme.VisualTheme
import com.gamerdex.ui.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import com.gamerdex.branding.GamerDexLogo
import com.gamerdex.core.domain.CollectionFilter
import com.gamerdex.ui.CollectionView

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var preferences: AppPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent {
            val theme by preferences.theme.collectAsStateWithLifecycle()
            DisposableEffect(theme) {
                val style = if (theme.dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.BLACK)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            val mode by preferences.collectionView.collectAsStateWithLifecycle()
            val nickname by preferences.alias.collectAsStateWithLifecycle()
            GamerDexTheme(theme) { GamerDexApp(theme, mode, preferences::setCollectionView, nickname) }
        }
    }
}

@Composable private fun GamerDexApp(theme: VisualTheme, mode: CollectionView, setMode: (CollectionView) -> Unit, nickname: String, viewModel: CollectionViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hardware by viewModel.hardware.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val entry by nav.currentBackStackEntryAsState()
    var platform by rememberSaveable { mutableStateOf<String?>(null) }
    var genre by rememberSaveable { mutableStateOf<String?>(null) }
    val menu: () -> Unit = { scope.launch { drawer.open() } }
    val destinations = listOf("vault" to "Colección", "hardware" to "Mis sistemas", "library/FAVORITES" to "Favoritos", "browse/platforms" to "Plataformas", "browse/genres" to "Géneros", "statistics" to "Estadísticas", "gamertag/0" to "Gamertags", "preferences" to "Preferencias")
    LaunchedEffect(state.error) { state.error?.let { snack.showSnackbar(it); viewModel.clearError() } }
    ModalNavigationDrawer(drawerState = drawer, gesturesEnabled = !saving, drawerContent = {
        ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { GamerDexLogo(Modifier.fillMaxWidth()); Text("PLAYER HUB / 02", style = MaterialTheme.typography.labelSmall) }
            destinations.forEachIndexed { index, (route, title) -> NavigationDrawerItem(label = { Text(title) }, icon = { Text("0${index + 1}", color = MaterialTheme.colorScheme.primary) },
                selected = entry?.destination?.route == route || (route == "library/FAVORITES" && entry?.destination?.route == "library/{filter}" && entry?.arguments?.getString("filter") == "FAVORITES") || (route == "gamertag/0" && entry?.destination?.route == "gamertag/{gameId}"), onClick = { scope.launch { drawer.close() }; nav.navigate(route) { launchSingleTop = true; popUpTo("vault") } }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)) }
            Spacer(Modifier.weight(1f)); Text(theme.tagline, Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }) {
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, snackbarHost = { SnackbarHost(snack) }) { outer ->
        NavHost(nav, startDestination = "vault", modifier = Modifier.padding(bottom = outer.calculateBottomPadding())) {
            composable("hardware") { HardwareListScreen(hardware, menu, { nav.navigate("hardware/edit/0") }, { nav.navigate("hardware/detail/$it") }) }
            composable("hardware/edit/{id}",arguments=listOf(navArgument("id"){type=NavType.LongType})) { entry ->
                val id=entry.arguments!!.getLong("id"); val item=hardware.find{it.id==id}
                if(id==0L || item!=null) HardwareEditorScreen(item,saving,{ value -> viewModel.saveHardware(value){nav.popBackStack()} },{if(!saving)nav.popBackStack()})
                else MissingGame(false){nav.popBackStack()}
            }
            composable("hardware/detail/{id}",arguments=listOf(navArgument("id"){type=NavType.LongType})) { entry ->
                val id=entry.arguments!!.getLong("id"); val item=hardware.find{it.id==id}
                if(item!=null) HardwareDetailScreen(item,{nav.popBackStack()},{nav.navigate("hardware/edit/$id")},{viewModel.deleteHardware(id){nav.popBackStack()}})
                else MissingGame(false){nav.popBackStack()}
            }
            composable("vault") { VaultScreen(state, { nav.navigate("edit/0") }, { nav.navigate("detail/$it") }, menu, platform, genre, { platform = it }, { platform = null; genre = null }, mode, setMode,
                { nav.navigate("library/ALL") }, { nav.navigate("browse/platforms") }, { nav.navigate("library/SPECIAL") }, { game -> viewModel.save(game.copy(isFavorite = !game.isFavorite)) {} }) }
            composable("library/{filter}") { listEntry ->
                val filter = runCatching { CollectionFilter.valueOf(listEntry.arguments?.getString("filter").orEmpty()) }.getOrDefault(CollectionFilter.ALL)
                LibraryScreen(state.games, filter, menu, { nav.navigate("detail/$it") }, { game -> viewModel.save(game.copy(isFavorite = !game.isFavorite)) {} })
            }
            composable("statistics") { StatisticsScreen(state.games, menu, { nav.navigate("library/${it.name}") },
                { selected -> platform = selected; genre = null; nav.navigate("vault") { popUpTo("vault"); launchSingleTop = true } },
                { selected -> genre = selected; platform = null; nav.navigate("vault") { popUpTo("vault"); launchSingleTop = true } }) }
            composable("browse/{kind}") { browse ->
                val platforms = browse.arguments?.getString("kind") == "platforms"
                BrowseScreen(state.games, platforms, menu) { selected ->
                    if (platforms) { platform = selected; genre = null } else { genre = selected; platform = null }
                    nav.navigate("vault") { popUpTo("vault"); launchSingleTop = true }
                }
            }
            composable("preferences") { PreferencesScreen(menu) }
            composable("gamertag/{gameId}", arguments = listOf(navArgument("gameId") { type = NavType.LongType })) { tagEntry ->
                GamertagScreen(state.games, theme, tagEntry.arguments?.getLong("gameId") ?: 0, menu, nickname, hardware)
            }
            composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                val id = entry.arguments!!.getLong("id")
                val game = state.games.find { it.id == id }
                if (game != null) DetailScreen(game, { nav.popBackStack() }, { nav.navigate("edit/$id") }, { viewModel.delete(id) { nav.popBackStack() } }, { nav.navigate("gamertag/$id") }, { viewModel.save(game.copy(isFavorite = !game.isFavorite)) {} })
                else MissingGame(state.loading) { nav.popBackStack() }
            }
            composable("edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                val id = entry.arguments!!.getLong("id")
                val game = state.games.find { it.id == id }
                if (id == 0L || game != null) EditorScreen(game, saving, search, viewModel::search, viewModel::choose,
                    { gameToSave, committed -> viewModel.save(gameToSave) { committed(); nav.popBackStack() } }, { if (!saving) nav.popBackStack() }, viewModel::lookupBarcode)
                else MissingGame(state.loading) { nav.popBackStack() }
            }
        }
    }
    }
}

@Composable private fun MissingGame(loading: Boolean, back: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp)) {
        if (loading) CircularProgressIndicator() else { Text("El juego ya no está en tu colección."); TextButton(onClick = back) { Text("Volver") } }
    }
}
