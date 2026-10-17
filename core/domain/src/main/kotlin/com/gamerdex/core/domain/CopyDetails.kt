package com.gamerdex.core.domain

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

@Serializable enum class EditionType { STANDARD, SPECIAL, COLLECTOR }
@Serializable enum class PhysicalCondition { UNSPECIFIED, MINT, VERY_GOOD, GOOD, ACCEPTABLE, DAMAGED }

fun EditionType.label() = when (this) { EditionType.STANDARD -> "Estándar"; EditionType.SPECIAL -> "Especial"; EditionType.COLLECTOR -> "Coleccionista" }
fun PhysicalCondition.label() = when (this) {
    PhysicalCondition.UNSPECIFIED -> "Sin evaluar"; PhysicalCondition.MINT -> "Como nuevo"; PhysicalCondition.VERY_GOOD -> "Muy bueno"
    PhysicalCondition.GOOD -> "Bueno"; PhysicalCondition.ACCEPTABLE -> "Aceptable"; PhysicalCondition.DAMAGED -> "Dañado"
}

fun parsePriceCents(text: String): Long? {
    if (text.isBlank()) return null
    require(Regex("\\d{1,9}([.,]\\d{1,2})?").matches(text.trim())) { "Usa un importe positivo con hasta dos decimales." }
    return BigDecimal(text.trim().replace(',', '.')).movePointRight(2).longValueExact()
}
fun formatPrice(cents: Long, currency: String = "EUR"): String = NumberFormat.getCurrencyInstance().apply {
    this.currency = Currency.getInstance(currency)
}.format(BigDecimal.valueOf(cents, 2))
