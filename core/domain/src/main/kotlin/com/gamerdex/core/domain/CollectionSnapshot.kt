package com.gamerdex.core.domain

import kotlinx.serialization.Serializable

@Serializable data class Platform(val id: Long, val name: String, val generation: Int? = null, val logoUrl: String? = null, val colorHex: String = "#00F0FF")
@Serializable data class Company(val id: Long, val name: String, val logoUrl: String? = null)
@Serializable data class CollectionSnapshot(
    val formatVersion: Int = 4, val createdAt: Long = System.currentTimeMillis(),
    val games: List<Game>, val platforms: List<Platform>, val companies: List<Company>,
    val hardware: List<Hardware> = emptyList(),
) {
    fun validate() {
        require(formatVersion in 1..4) { "Versión de copia no compatible." }
        require(formatVersion >= 4 || hardware.isEmpty()) { "El inventario necesita una copia v4." }
        require(hardware.size <= 100_000 && hardware.all { it.id > 0 } && hardware.distinctBy { it.id }.size == hardware.size)
        require(formatVersion >= 3 || games.all { it.rating == null || it.rating in 0f..5f }) { "La copia antigua contiene notas fuera de su escala." }
        require(games.size <= 100_000)
        require(games.all { it.id > 0 } && games.distinctBy { it.id }.size == games.size)
        require(platforms.all { it.id > 0 && it.name.isNotBlank() && Regex("#[0-9A-Fa-f]{6}").matches(it.colorHex) && (it.generation == null || it.generation > 0) })
        require(companies.all { it.id > 0 && it.name.isNotBlank() })
        require(platforms.distinctBy { it.id }.size == platforms.size && platforms.distinctBy { it.name }.size == platforms.size)
        require(companies.distinctBy { it.id }.size == companies.size && companies.distinctBy { it.name }.size == companies.size)
        val p = platforms.associateBy { it.id }; val c = companies.associateBy { it.id }
        require(games.all { p[it.platformId]?.name == it.platform && c[it.companyId]?.name == it.company }) { "La copia contiene referencias incompletas." }
    }
}
