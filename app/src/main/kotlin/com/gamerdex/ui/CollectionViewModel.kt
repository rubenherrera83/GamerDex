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
data class SearchState(val query: String = "", val results: List<Metadata> = emptyList(), val loading: Boolean = false, val error: String? = null, val barcodeMatch: BarcodeMatch? = null)

@HiltViewModel
class CollectionViewModel @Inject constructor(private val collection: CollectionRepository, private val metadata: MetadataRepository, private val barcodes: BarcodeRepository) : ViewModel() {
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
    val hardware = collection.observeHardware().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun saveHardware(value: Hardware, done: () -> Unit) {
        if (mutableSaving.value) return
        mutableSaving.value = true
        viewModelScope.launch {
            try { collection.saveHardware(value); done() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.update { it.copy(error = "No se pudo guardar el sistema.") } }
            finally { mutableSaving.value = false }
        }
    }
    fun deleteHardware(id: Long, done: () -> Unit) { viewModelScope.launch {
        try { collection.deleteHardware(id); done() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { mutableState.update { it.copy(error = "No se pudo eliminar el sistema.") } }
    } }
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
            try {
                val details=metadata.details(result.id)
                val picked=result.platforms.singleOrNull()
                selected(if(picked!=null && details.platforms.contains(picked)) details.copy(platforms=listOf(picked)) else details)
                mutableSearch.value = SearchState()
            }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutableSearch.update { it.copy(loading = false, error = "No se pudieron cargar los detalles. Reintenta o usa el formulario manual.") } }
        }
    }
    fun lookupBarcode(code:String) {
        searchJob?.cancel(); mutableSearch.value=SearchState(loading=true)
        searchJob=viewModelScope.launch {
            try {
                val match=barcodes.lookup(code)
                if(match==null) mutableSearch.value=SearchState(error="ScanDex no tiene una coincidencia para este código. Busca por título o continúa manualmente.")
                else {
                    mutableSearch.value=SearchState(match.title,loading=true,barcodeMatch=match)
                    try { mutableSearch.value=SearchState(match.title,metadata.search(match.title),barcodeMatch=match) }
                    catch(e:CancellationException){throw e}
                    catch(_:Exception){mutableSearch.value=SearchState(match.title,error="ScanDex identificó el título, pero RAWG no respondió. Puedes reintentar la búsqueda por título.",barcodeMatch=match)}
                }
            } catch(e:CancellationException){throw e}
            catch(e:Exception){mutableSearch.value=SearchState(error=if(e is IllegalArgumentException || e is IllegalStateException) e.message else "No se pudo consultar ScanDex. Reintenta o busca por título.")}
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
