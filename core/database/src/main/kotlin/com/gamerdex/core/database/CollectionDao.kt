package com.gamerdex.core.database

import androidx.room.*
import com.gamerdex.core.database.entity.*
import kotlinx.coroutines.flow.Flow

data class GameRecord(
    @Embedded val game: GameEntity,
    @Relation(parentColumn = "platformId", entityColumn = "id") val platform: PlatformEntity,
    @Relation(parentColumn = "companyId", entityColumn = "id") val company: CompanyEntity,
)

@Dao
interface CollectionDao {
    @Transaction @Query("SELECT * FROM games ORDER BY createdAt DESC, id DESC") suspend fun allGames(): List<GameRecord>
    @Query("SELECT * FROM platforms ORDER BY id") suspend fun allPlatforms(): List<PlatformEntity>
    @Query("SELECT * FROM companies ORDER BY id") suspend fun allCompanies(): List<CompanyEntity>
    @Query("DELETE FROM games") suspend fun clearGames()
    @Query("DELETE FROM platforms") suspend fun clearPlatforms()
    @Query("DELETE FROM companies") suspend fun clearCompanies()
    @Insert suspend fun restorePlatforms(values: List<PlatformEntity>)
    @Insert suspend fun restoreCompanies(values: List<CompanyEntity>)
    @Transaction @Query("SELECT * FROM games ORDER BY createdAt DESC, id DESC")
    fun observeGames(): Flow<List<GameRecord>>
    @Upsert suspend fun save(game: GameEntity)
    @Query("DELETE FROM games WHERE id = :id") suspend fun delete(id: Long)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(platform: PlatformEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(company: CompanyEntity): Long
    @Query("SELECT id FROM platforms WHERE name = :name") suspend fun platformId(name: String): Long?
    @Query("SELECT id FROM companies WHERE name = :name") suspend fun companyId(name: String): Long?
    @Query("UPDATE platforms SET logoUrl = :url WHERE name = :name") suspend fun updatePlatformLogo(name: String, url: String)
    @Query("UPDATE companies SET logoUrl = :url WHERE name = :name") suspend fun updateCompanyLogo(name: String, url: String)
}
