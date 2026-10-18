package com.gamerdex.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "hardware")
data class HardwareEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val payload: String)

@Dao interface HardwareDao {
    @Query("SELECT * FROM hardware ORDER BY id DESC") fun observe(): Flow<List<HardwareEntity>>
    @Query("SELECT * FROM hardware ORDER BY id DESC") suspend fun all(): List<HardwareEntity>
    @Upsert suspend fun save(value: HardwareEntity): Long
    @Query("DELETE FROM hardware WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM hardware") suspend fun clear()
}
