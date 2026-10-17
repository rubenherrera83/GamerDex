package com.gamerdex.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideCollectionRepository(repository: RoomCollectionRepository): com.gamerdex.core.domain.CollectionRepository = repository
    private val migrations: Array<Migration> = arrayOf(DatabaseMigrations.FROM_1_TO_2, DatabaseMigrations.FROM_2_TO_3, DatabaseMigrations.FROM_3_TO_4)

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GamerDexDatabase =
        Room.databaseBuilder(context, GamerDexDatabase::class.java, "gamerdex.db")
            .addMigrations(*migrations)
            .build()
}
