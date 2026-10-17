package com.gamerdex

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gamerdex.core.backup.PortableBackupService
import com.gamerdex.core.database.*
import com.gamerdex.core.domain.*
import com.gamerdex.gamertag.*
import com.gamerdex.ui.theme.VisualTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.withTimeout

@RunWith(AndroidJUnit4::class)
class BackupAndGamertagTest {
    @Test fun exportCanRecoverItsInputAfterViewModelRecreation(): Unit = runBlocking {
        val renderer = GamertagRenderer(context)
        val input = GamertagInput("PLAYER ONE", "COLLECTION", "ARCHIVO PERSONAL", "4 JUEGOS", null, VisualTheme.CYBERPUNK)
        val state = SavedStateHandle(mapOf("exportInput" to input))
        val viewModel = GamertagViewModel(renderer, state)
        val file = File(context.cacheDir, "recovered-export-${UUID.randomUUID()}.png")
        try {
            viewModel.export(Uri.fromFile(file))
            withTimeout(20_000) { viewModel.state.first { it.message?.startsWith("Gamertag guardado") == true } }
            assertEquals(1600, BitmapFactory.decodeFile(file.path).width)
            assertNull(state.get<GamertagInput>("exportInput"))
        } finally { file.delete() }
    }
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun portableBackupRestoresPhotosAfterOriginalFileDisappears(): Unit = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, GamerDexDatabase::class.java).build()
        val repository = RoomCollectionRepository(db)
        val service = PortableBackupService(context, repository)
        val source = File(context.cacheDir, "source-${UUID.randomUUID()}.png")
        val archive = File(context.cacheDir, "archive-${UUID.randomUUID()}.zip")
        val corrupt = File(context.cacheDir, "corrupt-${UUID.randomUUID()}.zip")
        try {
            val image = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.MAGENTA) }
            source.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            repository.save(Game(title = "Portable", platform = "PC", company = "Studio", editionType = EditionType.COLLECTOR,
                hasManual = false, pricePaidCents = 4995, genres = listOf("RPG"), localCustomPhotos = listOf(Uri.fromFile(source).toString())))
            assertEquals(1, service.export(Uri.fromFile(archive)))
            source.delete()
            repository.save(Game(title = "To replace", platform = "PC", company = "Studio"))
            val prepared = service.prepare(Uri.fromFile(archive)); assertEquals(1, prepared.photos)
            assertEquals(1, service.restore(prepared))
            val restored = repository.observeGames().first().single()
            assertEquals(4995L, restored.pricePaidCents); assertFalse(restored.hasManual)
            assertEquals(EditionType.COLLECTOR, restored.effectiveEdition)
            val photo = requireNotNull(Uri.parse(restored.localCustomPhotos.single()).path)
            assertEquals(40, BitmapFactory.decodeFile(photo).width)
            corrupt.writeText("Not a GamerDex backup")
            try { service.prepare(Uri.fromFile(corrupt)); fail("Expected invalid backup") } catch (_: Exception) { }
            assertEquals(restored, repository.observeGames().first().single())
            File(photo).delete()
        } finally { db.close(); source.delete(); archive.delete(); corrupt.delete() }
    }
    @Test fun allThemesRenderExportableGamertags() = runBlocking {
        val renderer = GamertagRenderer(context)
        val directory = File(context.getExternalFilesDir(null), "render-tests").apply { mkdirs() }
        val colors = mutableSetOf<Int>()
        VisualTheme.entries.forEach { theme ->
            val bitmap = renderer.render(GamertagInput("PLAYER ONE", "CYBERPUNK 2077", "PC / COLECCIONISTA", "24 JUEGOS / 4 PLATAFORMAS", null, theme))
            assertEquals(1600, bitmap.width); assertEquals(900, bitmap.height)
            colors += bitmap.getPixel(2, 2)
            val file = File(directory, "gamertag-${theme.name}.png")
            renderer.write(Uri.fromFile(file), bitmap)
            val decoded = BitmapFactory.decodeFile(file.path)
            assertEquals(1600, decoded.width); assertEquals(900, decoded.height)
        }
        assertEquals(VisualTheme.entries.size, colors.size)
    }
}
