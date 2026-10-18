package com.gamerdex.core.database

import androidx.room.*
import com.gamerdex.core.domain.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName="purchases") data class PurchaseEntity(@PrimaryKey val key:String,val payload:String)
@Dao interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY `key`") fun observe():Flow<List<PurchaseEntity>>
    @Query("SELECT * FROM purchases ORDER BY `key`") suspend fun all():List<PurchaseEntity>
    @Query("SELECT * FROM purchases WHERE `key`=:key") suspend fun get(key:String):PurchaseEntity?
    @Upsert suspend fun save(value:PurchaseEntity)
    @Query("DELETE FROM purchases WHERE `key`=:key") suspend fun delete(key:String)
    @Query("DELETE FROM purchases") suspend fun clear()
}
