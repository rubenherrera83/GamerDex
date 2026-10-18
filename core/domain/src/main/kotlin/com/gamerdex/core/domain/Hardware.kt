package com.gamerdex.core.domain

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable enum class HardwareKind { CONSOLE, COMPUTER, MICROCOMPUTER, HANDHELD }
@Serializable data class Controller(
    val name: String, val quantity: Int = 1, val condition: PhysicalCondition = PhysicalCondition.UNSPECIFIED,
    val serialNumber: String = "", val notes: String = "",
) { init { require(name.isNotBlank()); require(quantity in 1..99) } }

@Serializable data class Hardware(
    val id: Long = 0, val name: String, val platform: String,
    val kind: HardwareKind = HardwareKind.CONSOLE, val region: String = "",
    val hasBox: Boolean = false, val hasManual: Boolean = false, val hasCables: Boolean = false,
    val condition: PhysicalCondition = PhysicalCondition.UNSPECIFIED,
    val working: Boolean? = null, val serialNumber: String = "", val purchasedOn: String? = null,
    val pricePaidCents: Long? = null, val currency: String = "EUR",
    val controllers: List<Controller> = emptyList(), val localPhotos: List<String> = emptyList(),
    val imageUrl: String? = null, val logoUrl: String? = null, val notes: String = "",
) {
    init {
        require(id >= 0 && name.isNotBlank() && platform.isNotBlank())
        require(purchasedOn == null || validPurchaseDate(purchasedOn))
        require(pricePaidCents == null || pricePaidCents in 0..99_999_999_999)
        require(currency in setOf("EUR", "USD", "GBP"))
        require(localPhotos.none(String::isBlank))
        require(isValidLogoUrl(imageUrl) && isValidLogoUrl(logoUrl))
        require(controllers.size <= 100)
    }
}
fun validPurchaseDate(value: String): Boolean = runCatching {
    Regex("\\d{4}-\\d{2}-\\d{2}").matches(value) && LocalDate.parse(value).year in 1900..2100
}.getOrDefault(false)
