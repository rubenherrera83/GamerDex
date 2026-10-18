package com.gamerdex.gamertag

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class GamertagState(val bitmap: Bitmap? = null, val message: String? = null,val renderedInput:GamertagInput?=null)
@HiltViewModel class GamertagViewModel @Inject constructor(private val renderer: GamertagRenderer, private val savedState: SavedStateHandle, private val settings: GamertagSettings) : ViewModel() {
    val logos=settings.logos
    fun setLogos(value:List<GamertagLogo>)=settings.setLogos(value)
    private val mutable = MutableStateFlow(GamertagState())
    val state = mutable.asStateFlow()
    private var job: Job? = null
    private var pendingExport: Bitmap? = null
    fun generate(input: GamertagInput) {
        savedState["latestInput"] = input
        job?.cancel(); mutable.value = GamertagState()
        job = viewModelScope.launch {
            delay(300)
            try { mutable.value = GamertagState(renderer.render(input),renderedInput=input) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.value = GamertagState(message = "No se pudo generar la imagen. Inténtalo de nuevo.") }
        }
    }
    fun prepareExport() { pendingExport = mutable.value.bitmap; savedState["exportInput"] = savedState.get<GamertagInput>("latestInput") }
    fun export(uri: Uri) {
        val bitmap = pendingExport
        val input = savedState.get<GamertagInput>("exportInput")
        pendingExport = null
        if (bitmap == null && input == null) { mutable.update { it.copy(message = "Genera el gamertag antes de guardarlo.") }; return }
        viewModelScope.launch {
            try {
                renderer.write(uri, bitmap ?: renderer.render(requireNotNull(input)))
                savedState.remove<GamertagInput>("exportInput")
                mutable.update { it.copy(message = "Gamertag guardado como PNG de 1600 × 900.") }
            }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.update { it.copy(message = "No se pudo guardar la imagen en ese destino.") } }
        }
    }
}
