package com.gamerdex.core.backup

import com.gamerdex.core.domain.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import java.io.*
import java.util.zip.*

class ArchiveZipCodecTest {
    @Test fun hardwarePhotosControllersAndV3ArchivesRoundTrip() {
        val hardware=Hardware(42,"Master System II","Master System",hasBox=true,hasManual=false,serialNumber="MS2-ABC",purchasedOn="1995-06-20",pricePaidCents=1999,
            controllers=listOf(Controller("Arcade stick",2,notes="Edición especial")),localPhotos=listOf("content://hardware/photo"))
        val data=snapshot().copy(hardware=listOf(hardware))
        val output=ByteArrayOutputStream(); codec.write(data,output){ByteArrayInputStream(byteArrayOf(9,8,7))}
        val decoded=codec.read(ByteArrayInputStream(output.toByteArray()),temporary.newFolder())
        val item=decoded.snapshot.hardware.single()
        assertEquals(hardware.copy(localPhotos=item.localPhotos),item)
        assertArrayEquals(byteArrayOf(9,8,7),decoded.assets.getValue(item.localPhotos.single()).readBytes())
        val legacy=snapshot().copy(formatVersion=3,games=snapshot().games.map{it.copy(rating=3f)})
        val restored=codec.read(ByteArrayInputStream(zip("manifest.json",Json.encodeToString(legacy).toByteArray())),temporary.newFolder()).snapshot
        assertEquals(4,restored.formatVersion); assertTrue(restored.hardware.isEmpty()); assertEquals(3f,restored.games.single().rating)
    }
    @Test fun oldBackupRatingsConvertOnceAndCurrentLowScoresStayLow() {
        val legacy = listOf(0f, 2.5f, 4.5f, 5f, null)
        listOf(1, 2).forEach { version ->
            val source = snapshot().copy(formatVersion = version, games = legacy.mapIndexed { index, rating -> snapshot().games.single().copy(id = index + 1L, rating = rating) })
            val raw = zip("manifest.json", Json.encodeToString(source).toByteArray())
            val restored = codec.read(ByteArrayInputStream(raw), temporary.newFolder()).snapshot
            assertEquals(4, restored.formatVersion)
            assertEquals(listOf(0f, 50f, 90f, 100f, null), restored.games.map { it.rating })
            val output = ByteArrayOutputStream()
            codec.write(restored, output) { error("No photos") }
            assertEquals(restored, codec.read(ByteArrayInputStream(output.toByteArray()), temporary.newFolder()).snapshot)
        }
        val low = snapshot().copy(games = snapshot().games.map { it.copy(rating = 3f) })
        val output = ByteArrayOutputStream(); codec.write(low, output) { error("No photos") }
        assertEquals(3f, codec.read(ByteArrayInputStream(output.toByteArray()), temporary.newFolder()).snapshot.games.single().rating)
    }
    @Test fun legacyArchivesWithNewScaleValuesAreRejected() {
        val invalid = snapshot().copy(formatVersion = 2, games = snapshot().games.map { it.copy(rating = 90f) })
        assertThrows(IllegalArgumentException::class.java) { codec.read(ByteArrayInputStream(zip("manifest.json", Json.encodeToString(invalid).toByteArray())), temporary.newFolder()) }
    }
    @Test fun currentFormatPreservesProgressAndV1RemainsReadable() {
        val original = snapshot()
        val progressed = original.copy(games = original.games.map { it.copy(isFinished = true, completionPercent = 71, isFavorite = true, rawgId = 123) })
        val output = ByteArrayOutputStream()
        codec.write(progressed, output) { error("No photo") }
        val restored = codec.read(ByteArrayInputStream(output.toByteArray()), temporary.newFolder()).snapshot.games.single()
        assertTrue(restored.isFinished); assertTrue(restored.isFavorite); assertEquals(71, restored.completionPercent); assertEquals(123L, restored.rawgId)
        val oldJson = Json.encodeToString(original.copy(formatVersion = 1))
        val old = codec.read(ByteArrayInputStream(zip("manifest.json", oldJson.toByteArray())), temporary.newFolder()).snapshot.games.single()
        assertEquals(0, old.completionPercent); assertFalse(old.isFavorite)
    }
    @get:Rule val temporary = TemporaryFolder()
    private val codec = ArchiveZipCodec()
    private fun snapshot(photos: List<String> = emptyList()) = CollectionSnapshot(
        games = listOf(Game(id = 7, title = "Cyberpunk 2077", platform = "PC", company = "CD Projekt", localCustomPhotos = photos,
            editionType = EditionType.COLLECTOR, isSpecialEdition = true, hasManual = false, pricePaidCents = 14995, genres = listOf("RPG"), platformId = 1, companyId = 2)),
        platforms = listOf(Platform(1, "PC")), companies = listOf(Company(2, "CD Projekt")),
    )
    @Test fun archiveRoundTripIncludesPhotosAndExactAcquisitionData() {
        val output = ByteArrayOutputStream()
        codec.write(snapshot(listOf("content://original/photo")), output) { ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)) }
        val decoded = codec.read(ByteArrayInputStream(output.toByteArray()), temporary.newFolder())
        val game = decoded.snapshot.games.single()
        assertEquals(14995L, game.pricePaidCents)
        assertEquals(EditionType.COLLECTOR, game.effectiveEdition)
        assertFalse(game.hasManual)
        assertEquals(listOf("RPG"), game.genres)
        assertTrue(game.localCustomPhotos.single().startsWith("photos/"))
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), decoded.assets.getValue(game.localCustomPhotos.single()).readBytes())
    }
    @Test fun zipTraversalIsRejected() {
        val data = zip("../outside.txt", "unsafe".toByteArray())
        assertThrows(IllegalArgumentException::class.java) { codec.read(ByteArrayInputStream(data), temporary.newFolder()) }
        assertFalse(File(temporary.root, "outside.txt").exists())
    }
    @Test fun missingPhotosAndExternalUriReferencesAreRejected() {
        val manifest = Json.encodeToString(snapshot(listOf("file:///data/other-app/private.jpg"))).toByteArray()
        assertThrows(IllegalArgumentException::class.java) { codec.read(ByteArrayInputStream(zip("manifest.json", manifest)), temporary.newFolder()) }
    }
    @Test fun futureBackupVersionIsRejected() {
        val manifest = Json.encodeToString(snapshot().copy(formatVersion = 99)).toByteArray()
        assertThrows(IllegalArgumentException::class.java) { codec.read(ByteArrayInputStream(zip("manifest.json", manifest)), temporary.newFolder()) }
    }
    @Test fun oversizedManifestIsRejectedBeforeParsing() {
        val data = zip("manifest.json", ByteArray(ArchiveZipCodec.MAX_MANIFEST_BYTES.toInt() + 1) { 32 })
        assertThrows(IllegalArgumentException::class.java) { codec.read(ByteArrayInputStream(data), temporary.newFolder()) }
    }
    @Test fun invalidReferencesPreventExport() {
        val invalid = snapshot().copy(companies = emptyList())
        assertThrows(IllegalArgumentException::class.java) { codec.write(invalid, ByteArrayOutputStream()) { error("No photo") } }
    }
    private fun zip(name: String, bytes: ByteArray): ByteArray = ByteArrayOutputStream().also { output ->
        ZipOutputStream(output).use { zip -> zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry() }
    }.toByteArray()
}
