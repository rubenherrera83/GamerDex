package com.gamerdex.core.domain

enum class CollectionFilter(val title: String) {
    ALL("Todos los títulos"), FAVORITES("Favoritos"), FINISHED("Juegos finalizados"), HUNDRED("Completados al 100 %"),
    PLATINUM("Trofeos platino"), SPECIAL("Ediciones especiales"), PHYSICAL("Copias completas")
}
fun CollectionFilter.accepts(game: Game) = when (this) {
    CollectionFilter.ALL -> true
    CollectionFilter.FAVORITES -> game.isFavorite
    CollectionFilter.FINISHED -> game.isFinished
    CollectionFilter.HUNDRED -> game.completionPercent == 100
    CollectionFilter.PLATINUM -> game.hasPlatinum
    CollectionFilter.SPECIAL -> game.effectiveEdition != EditionType.STANDARD
    CollectionFilter.PHYSICAL -> game.condition != GameCondition.DIGITAL && game.isComplete
}
data class CollectionStats(val totals: Map<CollectionFilter, Int>, val platforms: Map<String, Int>, val genres: Map<String, Int>)
fun collectionStats(games: List<Game>) = CollectionStats(
    CollectionFilter.entries.associateWith { filter -> games.count(filter::accepts) },
    games.groupingBy { it.platform }.eachCount().toSortedMap(),
    games.flatMap { game -> game.genres.distinct().ifEmpty { listOf("Sin género") } }.groupingBy { it }.eachCount().toSortedMap(),
)
