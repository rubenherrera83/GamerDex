package com.gamerdex.core.domain

@kotlinx.serialization.Serializable
data class Game(
    val id: Long = 0, val title: String, val platform: String, val company: String,
    val releaseYear: Int? = null, val coverUrl: String? = null,
    val localCustomPhotos: List<String> = emptyList(), val isSpecialEdition: Boolean = false,
    val specialEditionDetails: String? = null, val condition: GameCondition = GameCondition.VERY_GOOD,
    val rating: Float? = null, val notes: String? = null, val createdAt: Long = System.currentTimeMillis(),
    val platformLogoUrl: String? = null, val companyLogoUrl: String? = null,
    val editionType: EditionType = EditionType.STANDARD,
    val hasBox: Boolean = condition != GameCondition.LOOSE && condition != GameCondition.DIGITAL,
    val hasManual: Boolean = condition != GameCondition.LOOSE && condition != GameCondition.DIGITAL,
    val hasMedia: Boolean = condition != GameCondition.DIGITAL,
    val physicalCondition: PhysicalCondition = when (condition) {
        GameCondition.MINT -> PhysicalCondition.MINT; GameCondition.VERY_GOOD -> PhysicalCondition.VERY_GOOD
        else -> PhysicalCondition.UNSPECIFIED
    },
    val pricePaidCents: Long? = null, val priceCurrency: String = "EUR",
    val genres: List<String> = emptyList(), val platformId: Long = 0, val companyId: Long = 0,
    val isFinished: Boolean = false, val completionPercent: Int = 0,
    val hasPlatinum: Boolean = false, val isFavorite: Boolean = false, val rawgId: Long? = null,
    val purchasedOn: String? = null, val ownedSystemId: Long? = null, val catalogUrl: String? = null,
) {
    init {
        require(title.isNotBlank())
        require(platform.isNotBlank() && company.isNotBlank())
        require(releaseYear == null || releaseYear in 1950..2100)
        require(rating == null || (rating.isFinite() && rating in 0f..100f))
        require(isSpecialEdition || editionType != EditionType.STANDARD || specialEditionDetails == null)
        require(localCustomPhotos.none(String::isBlank))
        require(createdAt >= 0)
        require(isValidLogoUrl(platformLogoUrl) && isValidLogoUrl(companyLogoUrl))
        require(pricePaidCents == null || pricePaidCents in 0..99_999_999_999)
        require(priceCurrency in supportedCurrencies && (priceCurrency!="JPY" || pricePaidCents==null || pricePaidCents%100L==0L))
        require(purchasedOn==null || validPurchaseDate(purchasedOn))
        require(ownedSystemId==null || ownedSystemId>0)
        require(isValidLogoUrl(catalogUrl))
        require(genres.none(String::isBlank))
        require(completionPercent in 0..100)
        require(!hasPlatinum || supportsPlatinum(platform))
        require(rawgId == null || rawgId > 0)
    }
    val effectiveEdition: EditionType get() = if (editionType == EditionType.STANDARD && isSpecialEdition) EditionType.SPECIAL else editionType
    val isComplete: Boolean get() = condition == GameCondition.DIGITAL || (hasBox && hasManual && hasMedia)
    val contentsLabel: String get() = if (condition == GameCondition.DIGITAL) "Digital" else if (isComplete) "Completo" else
        listOfNotNull("caja".takeUnless { hasBox }, "instrucciones".takeUnless { hasManual }, "juego".takeUnless { hasMedia }).joinToString(", ", prefix = "Falta: ")
}

fun supportsPlatinum(platform: String): Boolean = Regex("^(playstation|ps)\\s*[345]$", RegexOption.IGNORE_CASE).matches(platform.trim())

fun formatPersonalRating(rating: Float): String = if (rating % 1f == 0f) rating.toInt().toString() else "%.1f".format(rating)

fun isValidLogoUrl(value: String?): Boolean = value == null || runCatching {
    val uri = java.net.URI(value)
    uri.scheme == "https" && !uri.host.isNullOrBlank()
}.getOrDefault(false)
