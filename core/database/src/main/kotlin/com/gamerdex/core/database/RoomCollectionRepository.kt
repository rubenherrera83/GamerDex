package com.gamerdex.core.database

import androidx.room.withTransaction
import com.gamerdex.core.database.entity.*
import com.gamerdex.core.domain.*
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomCollectionRepository @Inject constructor(private val db: GamerDexDatabase) : CollectionRepository {
    private val dao get() = db.collectionDao()
    private fun GameRecord.toDomain() = with(game) { Game(
        id, title, platform.name, company.name, releaseYear, coverUrl, localCustomPhotos,
        isSpecialEdition, specialEditionDetails, condition, rating, notes, createdAt, platform.logoUrl, company.logoUrl,
        editionType, hasBox, hasManual, hasMedia, physicalCondition, pricePaidCents, priceCurrency, genres, platformId, companyId,
        isFinished, completionPercent, hasPlatinum, isFavorite, rawgId,
    ) }
    private fun Game.entity(platformId: Long, companyId: Long) = GameEntity(
        id = id, title = title.trim(), platformId = platformId, companyId = companyId, releaseYear = releaseYear,
        coverUrl = coverUrl, localCustomPhotos = localCustomPhotos, isSpecialEdition = effectiveEdition != EditionType.STANDARD,
        specialEditionDetails = specialEditionDetails.takeIf { effectiveEdition != EditionType.STANDARD }, condition = condition,
        rating = rating, notes = notes, createdAt = createdAt, editionType = effectiveEdition, hasBox = hasBox, hasManual = hasManual,
        hasMedia = hasMedia, physicalCondition = physicalCondition, pricePaidCents = pricePaidCents, priceCurrency = priceCurrency,
        genres = genres.map(String::trim).filter(String::isNotBlank).distinct(),
        isFinished = isFinished, completionPercent = completionPercent, hasPlatinum = hasPlatinum, isFavorite = isFavorite, rawgId = rawgId,
    )
    override fun observeGames() = dao.observeGames().map { records -> records.map { it.toDomain() } }
    override suspend fun save(game: Game) { db.withTransaction {
        val platform = game.platform.trim().also { require(it.isNotBlank()) }
        val company = game.company.trim().also { require(it.isNotBlank()) }
        dao.insert(PlatformEntity(name = platform, colorHex = "#00F0FF"))
        dao.insert(CompanyEntity(name = company))
        game.platformLogoUrl?.let { dao.updatePlatformLogo(platform, it) }
        game.companyLogoUrl?.let { dao.updateCompanyLogo(company, it) }
        dao.save(game.entity(requireNotNull(dao.platformId(platform)), requireNotNull(dao.companyId(company))))
    } }
    override suspend fun delete(id: Long) = dao.delete(id)
    override suspend fun snapshot(): CollectionSnapshot = db.withTransaction {
        CollectionSnapshot(games = dao.allGames().map { it.toDomain() },
            platforms = dao.allPlatforms().map { Platform(it.id, it.name, it.generation, it.logoUrl, it.colorHex) },
            companies = dao.allCompanies().map { Company(it.id, it.name, it.logoUrl) })
    }
    override suspend fun replace(snapshot: CollectionSnapshot) {
        snapshot.validate()
        db.withTransaction {
            dao.clearGames(); dao.clearPlatforms(); dao.clearCompanies()
            dao.restorePlatforms(snapshot.platforms.map { PlatformEntity(it.id, it.name, it.generation, it.logoUrl, it.colorHex) })
            dao.restoreCompanies(snapshot.companies.map { CompanyEntity(it.id, it.name, it.logoUrl) })
            snapshot.games.forEach { dao.save(it.entity(it.platformId, it.companyId)) }
        }
    }
}
