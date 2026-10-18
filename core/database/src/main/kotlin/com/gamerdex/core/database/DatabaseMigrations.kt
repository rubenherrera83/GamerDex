package com.gamerdex.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val FROM_5_TO_6 = object : Migration(5,6) {
        override fun migrate(db:SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE games ADD COLUMN purchasedOn TEXT")
            db.execSQL("ALTER TABLE games ADD COLUMN ownedSystemId INTEGER")
            db.execSQL("ALTER TABLE games ADD COLUMN catalogUrl TEXT")
            db.execSQL("CREATE TABLE IF NOT EXISTS purchases (`key` TEXT NOT NULL, payload TEXT NOT NULL, PRIMARY KEY(`key`))")
            val json=kotlinx.serialization.json.Json { encodeDefaults=true }
            fun insert(value:com.gamerdex.core.domain.Purchase) { db.execSQL("INSERT INTO purchases (`key`,payload) VALUES (?,?)",arrayOf(value.key,json.encodeToString(com.gamerdex.core.domain.Purchase.serializer(),value))) }
            db.query("SELECT g.id,g.title,p.name,g.pricePaidCents,g.priceCurrency FROM games g JOIN platforms p ON p.id=g.platformId").use { row ->
                while(row.moveToNext()) insert(com.gamerdex.core.domain.Purchase(com.gamerdex.core.domain.PurchaseKind.GAME,row.getLong(0),row.getString(1),row.getString(2),if(row.isNull(3))null else row.getLong(3),row.getString(4)))
            }
            db.query("SELECT id,payload FROM hardware").use { row -> while(row.moveToNext()) {
                val item=json.decodeFromString(com.gamerdex.core.domain.Hardware.serializer(),row.getString(1)).copy(id=row.getLong(0))
                insert(com.gamerdex.core.domain.hardwarePurchase(item))
            } }
        }
    }
    val FROM_4_TO_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS hardware (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, payload TEXT NOT NULL)")
        }
    }
    val FROM_3_TO_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("UPDATE games SET rating = rating * 20 WHERE rating IS NOT NULL")
        }
    }
    val FROM_2_TO_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE games ADD COLUMN isFinished INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE games ADD COLUMN completionPercent INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE games ADD COLUMN hasPlatinum INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE games ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE games ADD COLUMN rawgId INTEGER")
        }
    }
    val FROM_1_TO_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE games ADD COLUMN editionType TEXT NOT NULL DEFAULT 'STANDARD'")
            db.execSQL("ALTER TABLE games ADD COLUMN hasBox INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE games ADD COLUMN hasManual INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE games ADD COLUMN hasMedia INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE games ADD COLUMN physicalCondition TEXT NOT NULL DEFAULT 'UNSPECIFIED'")
            db.execSQL("ALTER TABLE games ADD COLUMN pricePaidCents INTEGER")
            db.execSQL("ALTER TABLE games ADD COLUMN priceCurrency TEXT NOT NULL DEFAULT 'EUR'")
            db.execSQL("ALTER TABLE games ADD COLUMN genres TEXT NOT NULL DEFAULT '[]'")
            db.execSQL("UPDATE games SET editionType = 'SPECIAL' WHERE isSpecialEdition = 1")
            db.execSQL("UPDATE games SET hasBox = 0, hasManual = 0 WHERE condition IN ('LOOSE', 'DIGITAL')")
            db.execSQL("UPDATE games SET hasMedia = 0 WHERE condition = 'DIGITAL'")
            db.execSQL("UPDATE games SET physicalCondition = 'MINT' WHERE condition = 'MINT'")
            db.execSQL("UPDATE games SET physicalCondition = 'VERY_GOOD' WHERE condition = 'VERY_GOOD'")
        }
    }
}
