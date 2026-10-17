package com.gamerdex.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gamerdex.core.domain.Game
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectionDatabaseTest {
    @Test fun replacesCollectionAtomicallyAndRetainsNewFields() = runTest {
        repository.save(Game(title = "Coleccionista", platform = "PC", company = "Studio", editionType = com.gamerdex.core.domain.EditionType.COLLECTOR,
            pricePaidCents = 7999, hasManual = false, genres = listOf("RPG")))
        val snapshot = repository.snapshot()
        repository.save(Game(title = "Otro", platform = "Switch", company = "Nintendo"))
        repository.replace(snapshot)
        val restored = repository.observeGames().first().single()
        assertEquals(7999L, restored.pricePaidCents); assertFalse(restored.hasManual)
        assertEquals(listOf("RPG"), restored.genres)
        assertEquals(snapshot.games.single().id, restored.id)
        try { repository.replace(snapshot.copy(companies = emptyList())); fail("Expected invalid archive") }
        catch (_: IllegalArgumentException) { }
        assertEquals(restored, repository.observeGames().first().single())
    }
    @Test fun logosAreSharedAndSurviveSavingAnotherCopy() = runTest {
        val logo = "https://example.com/pc.svg"
        repository.save(Game(title = "Portal", platform = "PC", company = "Valve", platformLogoUrl = logo))
        repository.save(Game(title = "Portal 2", platform = "PC", company = "Valve"))
        assertTrue(repository.observeGames().first().all { it.platformLogoUrl == logo })
        val game = repository.observeGames().first().first()
        repository.save(game.copy(companyLogoUrl = "https://example.com/valve.svg"))
        assertTrue(repository.observeGames().first().all { it.companyLogoUrl == "https://example.com/valve.svg" })
    }
    private lateinit var db: GamerDexDatabase
    private lateinit var repository: RoomCollectionRepository
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), GamerDexDatabase::class.java).build()
        repository = RoomCollectionRepository(db)
    }
    @After fun close() = db.close()
    @Test fun savesAndUpdatesWithoutDuplicatingReferences() = runTest {
        repository.save(Game(title = "Portal", platform = "PC", company = "Valve", localCustomPhotos = listOf("content://photos/1")))
        val saved = repository.observeGames().first().single()
        repository.save(saved.copy(title = "Portal 2", rating = 90f))
        val updated = repository.observeGames().first().single()
        assertEquals(saved.id, updated.id)
        assertEquals("Portal 2", updated.title)
        assertEquals(listOf("content://photos/1"), updated.localCustomPhotos)
        assertEquals(1L, db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM platforms").use { it.moveToFirst(); it.getLong(0) })
    }
    @Test fun referencedPlatformsCannotBeDeleted() = runTest {
        repository.save(Game(title = "Portal", platform = "PC", company = "Valve"))
        assertThrows(android.database.sqlite.SQLiteConstraintException::class.java) {
            db.openHelper.writableDatabase.execSQL("DELETE FROM platforms")
        }
        assertEquals(1, repository.observeGames().first().size)
    }
    @Test fun deletingGameKeepsPlatformAndCompany() = runTest {
        repository.save(Game(title = "Portal", platform = "PC", company = "Valve"))
        repository.delete(repository.observeGames().first().single().id)
        assertTrue(repository.observeGames().first().isEmpty())
        assertNotNull(db.collectionDao().platformId("PC"))
        assertNotNull(db.collectionDao().companyId("Valve"))
    }
}
