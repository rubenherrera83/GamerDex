package com.gamerdex.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import kotlinx.coroutines.*
import java.io.File

internal fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

internal object DraftPhotos {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // Only discard camera files owned by this app, never PhotoPicker originals.
    fun discard(context: Context, uris: List<String>) {
        val root = File(context.filesDir, "photos")
        scope.launch { uris.forEach { value -> runCatching {
            val uri = Uri.parse(value)
            if (uri.scheme == "file") {
                val file = File(requireNotNull(uri.path)).canonicalFile
                if (file.parentFile == root.canonicalFile && file.extension == "jpg") file.delete()
            }
        } } }
    }
}
