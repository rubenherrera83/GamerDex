package com.gamerdex.core.domain

enum class CollectionOrder(val label: String) {
    TITLE("Título A–Z"), TITLE_DESC("Título Z–A"), NEWEST("Alta reciente"),
    RATING("Tu nota más alta"), PRICE_ASC("Precio ↑ · por moneda"), PRICE_DESC("Precio ↓ · por moneda")
}

/** Prices stay grouped by currency: no implicit exchange rate or comparison across currencies. */
fun browseCollection(
    games: List<Game>, query: String = "", platform: String? = null, genre: String? = null,
    favorites: Boolean = false, finished: Boolean = false, specials: Boolean = false,
    order: CollectionOrder = CollectionOrder.TITLE,
): List<Game> {
    val title = compareBy<Game> { it.title.lowercase(java.util.Locale.ROOT) }.thenBy { it.id }
    val comparator = when (order) {
        CollectionOrder.TITLE -> title
        CollectionOrder.TITLE_DESC -> compareByDescending<Game> { it.title.lowercase(java.util.Locale.ROOT) }.thenBy { it.id }
        CollectionOrder.NEWEST -> compareByDescending<Game> { it.createdAt }.then(title)
        CollectionOrder.RATING -> compareBy<Game> { it.rating == null }.thenByDescending { it.rating }.then(title)
        CollectionOrder.PRICE_ASC, CollectionOrder.PRICE_DESC -> compareBy<Game> { it.pricePaidCents == null }
            .thenBy { if (it.pricePaidCents == null) "" else it.priceCurrency }
            .thenComparator { a, b ->
                val comparison = compareValues(a.pricePaidCents, b.pricePaidCents)
                if (order == CollectionOrder.PRICE_DESC) -comparison else comparison
            }.then(title)
    }
    return games.filter {
        it.title.contains(query.trim(), ignoreCase = true) && (platform == null || it.platform == platform) &&
            (genre == null || genre in it.genres || (genre == "Sin género" && it.genres.isEmpty())) &&
            (!favorites || it.isFavorite) && (!finished || it.isFinished) &&
            (!specials || it.effectiveEdition != EditionType.STANDARD)
    }.sortedWith(comparator)
}
