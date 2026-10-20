package com.gamerdex.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamerdex.core.backup.*
import com.gamerdex.ui.theme.VisualTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupState(val busy: Boolean = false, val message: String? = null, val pending: PreparedRestore? = null)
@HiltViewModel class PreferencesViewModel @Inject constructor(private val preferences: AppPreferences, private val backup: PortableBackupService) : ViewModel() {
    val theme = preferences.theme
    val alias = preferences.alias
    val startScreen=preferences.startScreen
    fun setStartScreen(value:StartScreen)=preferences.setStartScreen(value)
    fun setAlias(value: String) = preferences.setAlias(value)
    private val mutableBackup = MutableStateFlow(BackupState())
    val state = mutableBackup.asStateFlow()
    fun select(theme: VisualTheme) = preferences.select(theme)
    fun export(uri: Uri) = operation { "Copia creada: ${backup.export(uri)} juegos, inventario de sistemas y fotos." }
    fun inspect(uri: Uri) = operation {
        val prepared = backup.prepare(uri)
        backup.discard(mutableBackup.value.pending)
        mutableBackup.update { it.copy(pending = prepared) }; "Copia validada. Revisa el resumen antes de restaurar."
    }
    fun cancelRestore() { backup.discard(mutableBackup.value.pending); mutableBackup.update { it.copy(pending = null) } }
    fun restore() {
        val pending = mutableBackup.value.pending ?: return
        operation {
            try { "Colección recuperada: ${backup.restore(pending)} juegos y ${pending.systems} sistemas." }
            finally { mutableBackup.update { it.copy(pending = null) } }
        }
    }
    private fun operation(action: suspend () -> String) {
        if (mutableBackup.value.busy) return
        mutableBackup.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try { val message = action(); mutableBackup.update { it.copy(message = message) } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableBackup.update { it.copy(message = "No se pudo completar la operación. Comprueba el archivo, el espacio libre y los permisos de las fotos. Tu colección no se sustituye si la copia es inválida.") } }
            finally { mutableBackup.update { it.copy(busy = false) } }
        }
    }
    override fun onCleared() { backup.discard(mutableBackup.value.pending) }
}
