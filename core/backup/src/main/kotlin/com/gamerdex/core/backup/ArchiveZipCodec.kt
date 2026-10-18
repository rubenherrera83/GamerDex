package com.gamerdex.core.backup

import com.gamerdex.core.domain.CollectionSnapshot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.util.UUID
import java.util.zip.*

data class DecodedArchive(val snapshot: CollectionSnapshot, val assets: Map<String, File>)

/** Streaming ZIP codec. Paths are allowlisted and uncompressed sizes are bounded. */
class ArchiveZipCodec {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }
    private val assetName = Regex("photos/[a-f0-9-]{36}\\.bin")
    companion object {
        const val MAX_TOTAL_BYTES = 512L * 1024 * 1024
        const val MAX_PHOTO_BYTES = 32L * 1024 * 1024
        const val MAX_MANIFEST_BYTES = 8L * 1024 * 1024
        const val MAX_ENTRIES = 5001
    }
    fun write(snapshot: CollectionSnapshot, output: OutputStream, openPhoto: (String) -> InputStream) {
        snapshot.validate()
        require(snapshot.formatVersion == 5) { "La exportación necesita el formato actual de historial." }
        val references = (snapshot.games.flatMap { it.localCustomPhotos } + snapshot.hardware.flatMap { it.localPhotos }).distinct().associateWith { "photos/${UUID.randomUUID()}.bin" }
        require(references.size < MAX_ENTRIES) { "Demasiadas fotos para una sola copia." }
        val portable = snapshot.copy(games = snapshot.games.map { game -> game.copy(localCustomPhotos = game.localCustomPhotos.map { references.getValue(it) }) },
            hardware = snapshot.hardware.map { item -> item.copy(localPhotos = item.localPhotos.map { references.getValue(it) }) })
        val manifest = json.encodeToString(portable).toByteArray(Charsets.UTF_8)
        require(manifest.size <= MAX_MANIFEST_BYTES) { "La colección supera el límite del manifiesto." }
        var total = manifest.size.toLong()
        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json")); zip.write(manifest); zip.closeEntry()
            references.forEach { (uri, entry) ->
                zip.putNextEntry(ZipEntry(entry))
                val count = openPhoto(uri).use { input -> copyBounded(input, zip, minOf(MAX_PHOTO_BYTES, MAX_TOTAL_BYTES - total)) }
                total += count; zip.closeEntry()
            }
        }
    }
    fun read(input: InputStream, directory: File): DecodedArchive {
        require(directory.isDirectory || directory.mkdirs())
        val seen = mutableSetOf<String>(); val assets = mutableMapOf<String, File>()
        var manifest: String? = null; var total = 0L
        ZipInputStream(BufferedInputStream(input)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                require(!entry.isDirectory && (entry.name == "manifest.json" || assetName.matches(entry.name))) { "La copia contiene una ruta no válida." }
                require(seen.add(entry.name) && seen.size <= MAX_ENTRIES) { "Entradas duplicadas o demasiados archivos." }
                if (entry.name == "manifest.json") {
                    val output = ByteArrayOutputStream()
                    total += copyBounded(zip, output, minOf(MAX_MANIFEST_BYTES, MAX_TOTAL_BYTES - total))
                    manifest = output.toString(Charsets.UTF_8.name())
                } else {
                    // The archive cannot choose a destination outside this directory.
                    val file = File(directory, entry.name.substringAfter('/'))
                    total += file.outputStream().use { copyBounded(zip, it, minOf(MAX_PHOTO_BYTES, MAX_TOTAL_BYTES - total)) }
                    assets[entry.name] = file
                }
                zip.closeEntry()
            }
        }
        val stored = json.decodeFromString<CollectionSnapshot>(requireNotNull(manifest) { "No se encontró el manifiesto de GamerDex." })
        stored.validate()
        val normalizedGames=if(stored.formatVersion<3)stored.games.map{it.copy(rating=it.rating?.times(20f))}else stored.games
        val snapshot = stored.copy(formatVersion = 5, games=normalizedGames,purchases=if(stored.formatVersion<5)com.gamerdex.core.domain.seedPurchases(normalizedGames,stored.hardware)else stored.purchases)
        snapshot.validate()
        val references = (snapshot.games.flatMap { it.localCustomPhotos } + snapshot.hardware.flatMap { it.localPhotos }).toSet()
        require(references == assets.keys && references.all { assetName.matches(it) }) { "Faltan fotos o hay referencias no válidas." }
        return DecodedArchive(snapshot, assets)
    }
    private fun copyBounded(input: InputStream, output: OutputStream, limit: Long): Long {
        val buffer = ByteArray(16 * 1024); var count = 0L
        while (true) {
            val read = input.read(buffer); if (read == -1) return count
            count += read
            require(count <= limit) { "La copia excede el límite de tamaño permitido." }
            output.write(buffer, 0, read)
        }
    }
}
