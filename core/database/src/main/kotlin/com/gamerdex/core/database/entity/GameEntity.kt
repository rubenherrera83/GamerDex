package com.gamerdex.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gamerdex.core.domain.GameCondition
import com.gamerdex.core.domain.EditionType
import com.gamerdex.core.domain.PhysicalCondition
import androidx.room.ColumnInfo

@Entity(
    tableName = "games",
    foreignKeys = [
        ForeignKey(entity = PlatformEntity::class, parentColumns = ["id"], childColumns = ["platformId"], onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = CompanyEntity::class, parentColumns = ["id"], childColumns = ["companyId"], onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE),
    ],
    indices = [Index("platformId"), Index("companyId"), Index("title"), Index("createdAt"), Index("isSpecialEdition")],
)
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val platformId: Long,
    val companyId: Long,
    val releaseYear: Int? = null,
    val coverUrl: String? = null,
    val localCustomPhotos: List<String> = emptyList(),
    val isSpecialEdition: Boolean = false,
    val specialEditionDetails: String? = null,
    val condition: GameCondition = GameCondition.VERY_GOOD,
    val rating: Float? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "'STANDARD'") val editionType: EditionType = EditionType.STANDARD,
    @ColumnInfo(defaultValue = "1") val hasBox: Boolean = true,
    @ColumnInfo(defaultValue = "1") val hasManual: Boolean = true,
    @ColumnInfo(defaultValue = "1") val hasMedia: Boolean = true,
    @ColumnInfo(defaultValue = "'UNSPECIFIED'") val physicalCondition: PhysicalCondition = PhysicalCondition.UNSPECIFIED,
    val pricePaidCents: Long? = null,
    @ColumnInfo(defaultValue = "'EUR'") val priceCurrency: String = "EUR",
    @ColumnInfo(defaultValue = "'[]'") val genres: List<String> = emptyList(),
    @ColumnInfo(defaultValue = "0") val isFinished: Boolean = false,
    @ColumnInfo(defaultValue = "0") val completionPercent: Int = 0,
    @ColumnInfo(defaultValue = "0") val hasPlatinum: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false,
    val rawgId: Long? = null,
    val purchasedOn: String? = null, val ownedSystemId: Long? = null, val catalogUrl: String? = null,
) {
    init {
        require(title.isNotBlank())
        require(platformId > 0 && companyId > 0)
        require(releaseYear == null || releaseYear in 1950..2100)
        require(rating == null || (rating.isFinite() && rating in 0f..100f))
        require(isSpecialEdition || specialEditionDetails == null)
        require(localCustomPhotos.none(String::isBlank))
        require(createdAt >= 0)
    }
}
