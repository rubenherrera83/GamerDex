package com.gamerdex.core.database

import androidx.room.withTransaction
import com.gamerdex.core.database.entity.*
import com.gamerdex.core.domain.*
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomCollectionRepository @Inject constructor(private val db: GamerDexDatabase) : CollectionRepository {
    private val dao get() = db.collectionDao()
    private val hardwareDao get() = db.hardwareDao()
    private val purchases get()=db.purchaseDao()
    private val json = Json { encodeDefaults = true }
    private fun HardwareEntity.decode() = json.decodeFromString<Hardware>(payload).copy(id = id)
    override fun observeHardware() = hardwareDao.observe().map { rows -> rows.map { it.decode() } }
    override fun observePurchases()=purchases.observe().map{rows->rows.map{json.decodeFromString<Purchase>(it.payload)}}
    private suspend fun record(value:Purchase)=purchases.save(PurchaseEntity(value.key,json.encodeToString(value)))
    override suspend fun saveHardware(hardware: Hardware) { db.withTransaction {
        val generated=hardwareDao.save(HardwareEntity(hardware.id,json.encodeToString(hardware)))
        record(hardwarePurchase(hardware.copy(id=hardware.id.takeIf{it>0} ?: generated)))
    } }
    override suspend fun removeArchivedPurchase(key:String) { db.withTransaction {
        val value=purchases.get(key)?.let{json.decodeFromString<Purchase>(it.payload)} ?: return@withTransaction
        require(if(value.kind==PurchaseKind.GAME) dao.allGames().none{it.game.id==value.sourceId} else hardwareDao.all().none{it.id==value.sourceId})
        purchases.delete(key)
    } }
    override suspend fun deleteHardware(id: Long) = hardwareDao.delete(id)
    private fun GameRecord.toDomain() = with(game) { Game(
        id, title, platform.name, company.name, releaseYear, coverUrl, localCustomPhotos,
        isSpecialEdition, specialEditionDetails, condition, rating, notes, createdAt, platform.logoUrl, company.logoUrl,
        editionType, hasBox, hasManual, hasMedia, physicalCondition, pricePaidCents, priceCurrency, genres, platformId, companyId,
        isFinished, completionPercent, hasPlatinum, isFavorite, rawgId, purchasedOn, ownedSystemId, catalogUrl,
    ) }
    private fun Game.entity(platformId: Long, companyId: Long) = GameEntity(
        id = id, title = title.trim(), platformId = platformId, companyId = companyId, releaseYear = releaseYear,
        coverUrl = coverUrl, localCustomPhotos = localCustomPhotos, isSpecialEdition = effectiveEdition != EditionType.STANDARD,
        specialEditionDetails = specialEditionDetails.takeIf { effectiveEdition != EditionType.STANDARD }, condition = condition,
        rating = rating, notes = notes, createdAt = createdAt, editionType = effectiveEdition, hasBox = hasBox, hasManual = hasManual,
        hasMedia = hasMedia, physicalCondition = physicalCondition, pricePaidCents = pricePaidCents, priceCurrency = priceCurrency,
        genres = genres.map(String::trim).filter(String::isNotBlank).distinct(),
        isFinished = isFinished, completionPercent = completionPercent, hasPlatinum = hasPlatinum, isFavorite = isFavorite, rawgId = rawgId,
        purchasedOn=purchasedOn, ownedSystemId=ownedSystemId, catalogUrl=catalogUrl,
    )
    override fun observeGames() = dao.observeGames().map { records -> records.map { it.toDomain() } }
    override suspend fun save(game: Game) { db.withTransaction {
        val platform = game.platform.trim().also { require(it.isNotBlank()) }
        val company = game.company.trim().also { require(it.isNotBlank()) }
        dao.insert(PlatformEntity(name = platform, colorHex = "#00F0FF"))
        dao.insert(CompanyEntity(name = company))
        game.platformLogoUrl?.let { dao.updatePlatformLogo(platform, it) }
        game.companyLogoUrl?.let { dao.updateCompanyLogo(company, it) }
        val generated=dao.save(game.entity(requireNotNull(dao.platformId(platform)), requireNotNull(dao.companyId(company))))
        val saved=game.copy(id=game.id.takeIf{it>0} ?: generated,title=game.title.trim(),platform=platform,company=company)
        record(gamePurchase(saved,hardwareDao.all().map{it.decode()},purchases.get("GAME:${saved.id}")?.let{json.decodeFromString<Purchase>(it.payload)}))
    } }
    override suspend fun delete(id: Long) = dao.delete(id)
    override suspend fun snapshot(): CollectionSnapshot = db.withTransaction {
        CollectionSnapshot(games = dao.allGames().map { it.toDomain() },
            platforms = dao.allPlatforms().map { Platform(it.id, it.name, it.generation, it.logoUrl, it.colorHex) },
            companies = dao.allCompanies().map { Company(it.id, it.name, it.logoUrl) }, hardware = hardwareDao.all().map { it.decode() }, purchases=purchases.all().map{json.decodeFromString<Purchase>(it.payload)})
    }
    override suspend fun replace(snapshot: CollectionSnapshot) {
        snapshot.validate()
        db.withTransaction {
            hardwareDao.clear()
            purchases.clear()
            dao.clearGames(); dao.clearPlatforms(); dao.clearCompanies()
            dao.restorePlatforms(snapshot.platforms.map { PlatformEntity(it.id, it.name, it.generation, it.logoUrl, it.colorHex) })
            dao.restoreCompanies(snapshot.companies.map { CompanyEntity(it.id, it.name, it.logoUrl) })
            snapshot.games.forEach { dao.save(it.entity(it.platformId, it.companyId)) }
            snapshot.hardware.forEach { hardwareDao.save(HardwareEntity(it.id, json.encodeToString(it))) }
            val records=snapshot.purchases.associateBy{it.key}.toMutableMap()
            snapshot.games.forEach{game->val value=gamePurchase(game,snapshot.hardware,records["GAME:${game.id}"]);records[value.key]=value}
            snapshot.hardware.forEach{h->val value=hardwarePurchase(h);records[value.key]=value}
            records.values.forEach{record(it)}
        }
    }
}
