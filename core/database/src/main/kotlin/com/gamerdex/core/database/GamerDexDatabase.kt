package com.gamerdex.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gamerdex.core.database.entity.CompanyEntity
import com.gamerdex.core.database.entity.GameEntity
import com.gamerdex.core.database.entity.PlatformEntity

@Database(entities = [GameEntity::class, PlatformEntity::class, CompanyEntity::class], version = 4, exportSchema = true)
@TypeConverters(DatabaseConverters::class)
abstract class GamerDexDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
}
