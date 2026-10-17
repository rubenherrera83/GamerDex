package com.gamerdex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamerdex.core.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionState(val games: List<Game> = emptyList(), val loading: Boolean = true, val error: String? = null)
data class SearchState(val query: String = "", val results: List<Metadata> = emptyList(), val loading: Boolean = false, val error: String? = null)

@HiltViewModel
class CollectionViewModel @Inject constructor(private val collection: CollectionRepository, private val metadata: MetadataRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(CollectionState())
    val state = mutableState.asStateFlow()
    private val mutableSearch = MutableStateFlow(SearchState())
    val search = mutableSearch.asStateFlow()
    private val mutableSaving = MutableStateFlow(false)
    val saving = mutableSaving.asStateFlow()
    private var searchJob: Job? = null
    init { viewModelScope.launch {
        collection.observeGames().catch { mutableState.update { s -> s.copy(loading = false, error = "No se pudo abrir la colección.") } }
            .collect { games -> mutableState.update { it.copy(games = games, loading = false) } }
    } }
    fun clearError() { mutableState.update { it.copy(error = null) } }
    fun search(query: String) {
        searchJob?.cancel()
        mutableSearch.value = SearchState(query)
        if (query.trim().length < 2) return
        searchJob = viewModelScope.launch {
            delay(350)
            mutableSearch.update { it.copy(loading = true) }
            try { mutableSearch.value = SearchState(query, metadata.search(query)) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutableSearch.value = SearchState(query, error = if (e is IllegalStateException) e.message else "No se pudo consultar RAWG. Reintenta o añade el juego manualmente.") }
        }
    }
    fun choose(result: Metadata, selected: (Metadata) -> Unit) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            mutableSearch.update { it.copy(loading = true) }
            try { selected(metadata.details(result.id)); mutableSearch.value = SearchState() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutableSearch.update { it.copy(loading = false, error = "No se pudieron cargar los detalles. Reintenta o usa el formulario manual.") } }
        }
    }
    fun save(game: Game, done: () -> Unit) {
        if (mutableSaving.value) return
        mutableSaving.value = true
        viewModelScope.launch {
            try { collection.save(game); done() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutableState.update { it.copy(error = "No se pudo guardar. Revisa los campos e inténtalo de nuevo.") } }
            finally { mutableSaving.value = false }
        }
    }
    fun delete(id: Long, done: () -> Unit) { viewModelScope.launch {
        try { collection.delete(id); done() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { mutableState.update { it.copy(error = "No se pudo eliminar el juego.") } }
    } }
}
