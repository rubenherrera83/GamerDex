package com.gamerdex.core.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.gamerdex.core.domain.CollectionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

class PreparedRestore internal constructor(internal val decoded: DecodedArchive, internal val directory: File) {
    val titles get() = decoded.snapshot.games.size
    val systems get() = decoded.snapshot.hardware.size
    val photos get() = decoded.assets.size
    val createdAt get() = decoded.snapshot.createdAt
    internal val consumed = AtomicBoolean(false)
}

@Singleton class PortableBackupService @Inject constructor(
    @ApplicationContext private val context: Context, private val repository: CollectionRepository,
) {
    private val codec = ArchiveZipCodec()
    private val cleanup = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    suspend fun export(uri: Uri): Int = withContext(Dispatchers.IO) {
        val snapshot = repository.snapshot()
        try {
            val output = requireNotNull(context.contentResolver.openOutputStream(uri, "wt")) { "No se puede escribir en ese destino." }
            output.use { codec.write(snapshot, it) { photo -> requireNotNull(context.contentResolver.openInputStream(Uri.parse(photo))) { "No se puede leer una foto. Revisa sus permisos." } } }
            snapshot.games.size
        } catch (error: Exception) {
            runCatching { DocumentsContract.deleteDocument(context.contentResolver, uri) }
            throw error
        }
    }
    suspend fun prepare(uri: Uri): PreparedRestore = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "restore-${UUID.randomUUID()}")
        try {
            val decoded = requireNotNull(context.contentResolver.openInputStream(uri)).use { codec.read(it, directory) }
            ensureActive()
            PreparedRestore(decoded, directory)
        } catch (error: Exception) { directory.deleteRecursively(); throw error }
    }
    suspend fun restore(prepared: PreparedRestore): Int = withContext(Dispatchers.IO + NonCancellable) {
        check(prepared.consumed.compareAndSet(false, true))
        val copied = mutableListOf<File>()
        try {
            val root = File(context.filesDir, "photos").apply { check(isDirectory || mkdirs()) }
            val mapping = prepared.decoded.assets.mapValues { (_, staged) ->
                val target = File(root, "restore-${UUID.randomUUID()}.bin")
                copied += target; staged.copyTo(target)
                Uri.fromFile(target).toString()
            }
            val snapshot = prepared.decoded.snapshot.copy(hardware = prepared.decoded.snapshot.hardware.map { item -> item.copy(localPhotos = item.localPhotos.map { mapping.getValue(it) }) }, games = prepared.decoded.snapshot.games.map { game ->
                game.copy(localCustomPhotos = game.localCustomPhotos.map { mapping.getValue(it) })
            })
            repository.replace(snapshot)
            snapshot.games.size
        } catch (error: Exception) { copied.forEach(File::delete); throw error }
        finally { prepared.directory.deleteRecursively() }
    }
    fun discard(prepared: PreparedRestore?) {
        if (prepared != null && prepared.consumed.compareAndSet(false, true)) cleanup.launch { prepared.directory.deleteRecursively() }
    }
}
