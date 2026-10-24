package com.gamerdex.ui

import android.content.Context
import androidx.core.content.edit
import com.gamerdex.ui.theme.VisualTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class AppPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("gamerdex_preferences", Context.MODE_PRIVATE)
    // Earlier versions also wrote GAMES automatically, so legacy values do not identify an explicit choice.
    // Adopt the requested Systems default once; choices made after this migration remain authoritative.
    private val initialScreen=MutableStateFlow(if(preferences.getBoolean("systems_start_default_v2",false))
        runCatching{StartScreen.valueOf(preferences.getString("start_screen",null).orEmpty())}.getOrDefault(StartScreen.SYSTEMS)
        else StartScreen.SYSTEMS)
    val startScreen=initialScreen.asStateFlow()
    init {preferences.edit{putString("start_screen",initialScreen.value.name);putBoolean("systems_start_default_v2",true)}}
    fun setStartScreen(value:StartScreen){preferences.edit{putString("start_screen",value.name)};initialScreen.value=value}
    private val selected = MutableStateFlow(runCatching { VisualTheme.valueOf(preferences.getString("theme", null).orEmpty()) }.getOrDefault(VisualTheme.CYBERPUNK))
    val theme = selected.asStateFlow()
    private val mode = MutableStateFlow(runCatching { CollectionView.valueOf(preferences.getString("collection_view", null).orEmpty()) }
        .getOrDefault(if (preferences.contains("horizontal_collection")) { if (preferences.getBoolean("horizontal_collection", false)) CollectionView.COVER_FLOW else CollectionView.LIST } else CollectionView.GRID))
    val collectionView = mode.asStateFlow()
    private val nickname = MutableStateFlow(preferences.getString("nickname", "").orEmpty())
    val alias = nickname.asStateFlow()
    fun setAlias(value: String) { val name = value.take(32); preferences.edit().putString("nickname", name).apply(); nickname.value = name }
    fun setCollectionView(value: CollectionView) { preferences.edit().putString("collection_view", value.name).apply(); mode.value = value }
    fun select(theme: VisualTheme) { preferences.edit().putString("theme", theme.name).apply(); selected.value = theme }
}
