package com.gamerdex.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Test fun migratesV5PurchasesWithoutInventingGamePurchaseDates() {
        val name="spending-v6-migration"
        helper.createDatabase(name,5).apply {
            execSQL("INSERT INTO platforms (id,name,generation,logoUrl,colorHex) VALUES (1,'PC',NULL,NULL,'#00F0FF')")
            execSQL("INSERT INTO companies (id,name,logoUrl) VALUES (1,'Studio',NULL)")
            execSQL("INSERT INTO games (id,title,platformId,companyId,releaseYear,coverUrl,localCustomPhotos,isSpecialEdition,specialEditionDetails,condition,rating,notes,createdAt,pricePaidCents) VALUES (1,'Old game',1,1,1991,NULL,'[]',0,NULL,'VERY_GOOD',90,'Keep',1234,1999)")
            val hardware=com.gamerdex.core.domain.Hardware(7,"Old Saturn","Sega Saturn",purchasedOn="2019-12-01",pricePaidCents=10000)
            execSQL("INSERT INTO hardware (id,payload) VALUES (?,?)",arrayOf(7,kotlinx.serialization.json.Json.encodeToString(com.gamerdex.core.domain.Hardware.serializer(),hardware)))
            close()
        }
        helper.runMigrationsAndValidate(name,6,true,DatabaseMigrations.FROM_5_TO_6).use {db->
            db.query("SELECT payload FROM purchases ORDER BY `key`").use {row->
                assertEquals(2,row.count);assertTrue(row.moveToFirst())
                val game=kotlinx.serialization.json.Json.decodeFromString(com.gamerdex.core.domain.Purchase.serializer(),row.getString(0));assertNull(game.purchasedOn);assertEquals(1999L,game.amount)
                assertTrue(row.moveToNext());val hardware=kotlinx.serialization.json.Json.decodeFromString(com.gamerdex.core.domain.Purchase.serializer(),row.getString(0));assertEquals("2019-12-01",hardware.purchasedOn)
            }
        }
    }
    @Test fun addsHardwareToV4WithoutChangingGames() {
        val name="hardware-migration-v5"
        helper.createDatabase(name,4).apply {
            execSQL("INSERT INTO platforms (id,name,generation,logoUrl,colorHex) VALUES (1,'PC',NULL,NULL,'#00F0FF')")
            execSQL("INSERT INTO companies (id,name,logoUrl) VALUES (1,'Studio',NULL)")
            execSQL("INSERT INTO games (id,title,platformId,companyId,releaseYear,coverUrl,localCustomPhotos,isSpecialEdition,specialEditionDetails,condition,rating,notes,createdAt) VALUES (1,'Preserved',1,1,2000,NULL,'[]',0,NULL,'VERY_GOOD',90,'Keep',1234)")
            close()
        }
        helper.runMigrationsAndValidate(name,5,true,DatabaseMigrations.FROM_4_TO_5).use { db ->
            db.query("SELECT rating, notes FROM games").use{assertTrue(it.moveToFirst());assertEquals(90f,it.getFloat(0));assertEquals("Keep",it.getString(1))}
            db.query("SELECT COUNT(*) FROM hardware").use{assertTrue(it.moveToFirst());assertEquals(0,it.getInt(0))}
        }
    }
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GamerDexDatabase::class.java)
    @Test fun convertsLegacyRatingsOnceAndPreservesOtherFields() {
        val name = "migration-rating-v4-test"
        helper.createDatabase(name, 3).apply {
            execSQL("INSERT INTO platforms (id,name,generation,logoUrl,colorHex) VALUES (1,'PS5',NULL,NULL,'#00F0FF')")
            execSQL("INSERT INTO companies (id,name,logoUrl) VALUES (1,'Studio',NULL)")
            listOf("0", "2.5", "4.5", "5", "NULL").forEachIndexed { index, rating ->
                execSQL("INSERT INTO games (id,title,platformId,companyId,localCustomPhotos,isSpecialEdition,condition,rating,notes,createdAt,isFavorite,completionPercent) VALUES (${index + 1},'Keep',1,1,'[]',0,'VERY_GOOD',$rating,'Same notes',1234,1,87)")
            }
            close()
        }
        helper.runMigrationsAndValidate(name, 4, true, DatabaseMigrations.FROM_3_TO_4).use { db ->
            db.query("SELECT rating,notes,isFavorite,completionPercent FROM games ORDER BY id").use { row ->
                listOf(0f, 50f, 90f, 100f, null).forEach { expected ->
                    assertTrue(row.moveToNext()); if (expected == null) assertTrue(row.isNull(0)) else assertEquals(expected, row.getFloat(0))
                    assertEquals("Same notes", row.getString(1)); assertEquals(1, row.getInt(2)); assertEquals(87, row.getInt(3))
                }
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repeat(2) {
            val db = androidx.room.Room.databaseBuilder(context, GamerDexDatabase::class.java, name).addMigrations(DatabaseMigrations.FROM_3_TO_4, DatabaseMigrations.FROM_4_TO_5,DatabaseMigrations.FROM_5_TO_6).build()
            try { kotlinx.coroutines.runBlocking {
                val repository = RoomCollectionRepository(db)
                assertEquals(90f, repository.snapshot().games.first { it.id == 3L }.rating)
                repository.save(repository.snapshot().games.first { it.id == 1L }.copy(rating = 3f))
                assertEquals(3f, repository.snapshot().games.first { it.id == 1L }.rating)
            } } finally { db.close() }
        }
    }
    @Test fun migratesV2AndRoundTripsNewProgressWithoutLosingNotes() {
        val name = "migration-v3-test"
        helper.createDatabase(name, 2).apply {
            execSQL("INSERT INTO platforms (id,name,generation,logoUrl,colorHex) VALUES (1,'PlayStation 5',NULL,NULL,'#00F0FF')")
            execSQL("INSERT INTO companies (id,name,logoUrl) VALUES (1,'Studio',NULL)")
            execSQL("INSERT INTO games (id,title,platformId,companyId,releaseYear,coverUrl,localCustomPhotos,isSpecialEdition,specialEditionDetails,condition,rating,notes,createdAt) VALUES (1,'Keep',1,1,2020,NULL,'[]',0,NULL,'VERY_GOOD',4.5,'My notes',1234)")
            close()
        }
        helper.runMigrationsAndValidate(name, 3, true, DatabaseMigrations.FROM_2_TO_3).use { db ->
            db.query("SELECT * FROM games").use { row ->
                assertTrue(row.moveToFirst()); assertEquals("My notes", row.getString(row.getColumnIndexOrThrow("notes")))
                assertEquals(0, row.getInt(row.getColumnIndexOrThrow("isFavorite")))
                assertEquals(0, row.getInt(row.getColumnIndexOrThrow("completionPercent")))
                assertTrue(row.isNull(row.getColumnIndexOrThrow("rawgId")))
            }
        }
        val db = androidx.room.Room.databaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, GamerDexDatabase::class.java, name).addMigrations(DatabaseMigrations.FROM_2_TO_3, DatabaseMigrations.FROM_3_TO_4, DatabaseMigrations.FROM_4_TO_5,DatabaseMigrations.FROM_5_TO_6).build()
        try { kotlinx.coroutines.runBlocking {
            val repository = RoomCollectionRepository(db)
            val original = repository.snapshot().games.single()
            repository.save(original.copy(isFavorite = true, isFinished = true, completionPercent = 83, hasPlatinum = true, rawgId = 4200))
            val saved = repository.snapshot().games.single()
            assertTrue(saved.isFavorite); assertTrue(saved.isFinished); assertTrue(saved.hasPlatinum)
            assertEquals(83, saved.completionPercent); assertEquals(4200L, saved.rawgId); assertEquals(90f, saved.rating)
        } } finally { db.close() }
    }
    @Test fun migratesExistingSpecialAndLooseCopiesWithoutLosingData() {
        val name = "migration-test"
        helper.createDatabase(name, 1).apply {
            execSQL("INSERT INTO platforms (id,name,generation,logoUrl,colorHex) VALUES (1,'PC',NULL,NULL,'#00F0FF')")
            execSQL("INSERT INTO companies (id,name,logoUrl) VALUES (1,'Studio',NULL)")
            execSQL("INSERT INTO games (id,title,platformId,companyId,releaseYear,coverUrl,localCustomPhotos,isSpecialEdition,specialEditionDetails,condition,rating,notes,createdAt) VALUES (1,'Old special',1,1,2000,NULL,'[]',1,'Steelbook','LOOSE',4.5,'Keep me',1234)")
            close()
        }
        helper.runMigrationsAndValidate(name, 2, true, DatabaseMigrations.FROM_1_TO_2).use { db ->
            db.query("SELECT * FROM games WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst()); assertEquals("Keep me", cursor.getString(cursor.getColumnIndexOrThrow("notes")))
                assertEquals("SPECIAL", cursor.getString(cursor.getColumnIndexOrThrow("editionType")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("hasBox")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("hasManual")))
                assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("hasMedia")))
                assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("pricePaidCents")))
                assertEquals("[]", cursor.getString(cursor.getColumnIndexOrThrow("genres")))
            }
        }
    }
}
