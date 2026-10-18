package com.gamerdex.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
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
