package com.gamerdex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamerdex.core.domain.GameMedia
import com.gamerdex.core.domain.CommunityRating
import com.gamerdex.core.domain.MetadataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class RawgMediaState(val loading: Boolean = false, val media: GameMedia? = null, val error: String? = null,
    val ratingLoading: Boolean = false, val communityRating: CommunityRating? = null, val ratingError: Boolean = false)
@HiltViewModel class RawgMediaViewModel @Inject constructor(private val repository: MetadataRepository) : ViewModel() {
    private val mutable = MutableStateFlow(RawgMediaState())
    val state = mutable.asStateFlow()
    private var request: Job? = null
    fun clear() { request?.cancel(); mutable.value = RawgMediaState() }
    fun load(id: Long) {
        request?.cancel(); mutable.value = RawgMediaState(loading = true, ratingLoading = true)
        request = viewModelScope.launch {
            launch {
                try { val media = repository.media(id); mutable.update { it.copy(loading = false, media = media) } }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { mutable.update { it.copy(loading = false, error = "No se pudo consultar la multimedia de RAWG. Revisa la conexión e inténtalo de nuevo.") } }
            }
            launch {
                try { val score = repository.details(id).communityRating; mutable.update { it.copy(ratingLoading = false, communityRating = score) } }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { mutable.update { it.copy(ratingLoading = false, ratingError = true) } }
            }
        }
    }
}
