package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import java.io.Serializable
import java.math.BigDecimal

data class CopyDetailsState(
    val edition: EditionType = EditionType.STANDARD, val details: String = "", val digital: Boolean = false, val sealed: Boolean = false,
    val box: Boolean = true, val manual: Boolean = true, val media: Boolean = true,
    val physical: PhysicalCondition = PhysicalCondition.UNSPECIFIED, val price: String = "", val currency: String = "EUR", val genres: String = "",
) : Serializable {
    val valid: Boolean get() = runCatching { parsePriceCents(price,currency) }.isSuccess
    companion object {
        fun from(game: Game?) = if (game == null) CopyDetailsState() else CopyDetailsState(game.effectiveEdition, game.specialEditionDetails.orEmpty(),
            game.condition == GameCondition.DIGITAL, game.condition == GameCondition.SEALED, game.hasBox, game.hasManual, game.hasMedia,
            game.physicalCondition, game.pricePaidCents?.let { BigDecimal.valueOf(it, 2).stripTrailingZeros().toPlainString() }.orEmpty(), game.priceCurrency, game.genres.joinToString(", "))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun CopyDetailsFields(value: CopyDetailsState, change: (CopyDetailsState) -> Unit, enabled: Boolean) {
    Text("01 / TU EDICIÓN", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditionType.entries.forEach { edition -> FilterChip(value.edition == edition, { change(value.copy(edition = edition)) }, enabled = enabled, label = { Text(edition.label()) }) }
    }
    if (value.edition != EditionType.STANDARD) OutlinedTextField(value.details, { change(value.copy(details = it)) }, enabled = enabled,
        label = { Text("Contenido de la edición: steelbook, figura…") }, modifier = Modifier.fillMaxWidth())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Copia digital"); Switch(value.digital, { change(value.copy(digital = it)) }, enabled = enabled) }
    if (!value.digital) {
        Text("02 / CONTENIDO Y ESTADO", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Lo tengo completo"); Switch(value.box && value.manual && value.media, { change(value.copy(box = it, manual = it, media = it)) }, enabled = enabled)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(value.box, { change(value.copy(box = !value.box)) }, enabled = enabled, label = { Text("Caja") })
            FilterChip(value.manual, { change(value.copy(manual = !value.manual)) }, enabled = enabled, label = { Text("Instrucciones") })
            FilterChip(value.media, { change(value.copy(media = !value.media)) }, enabled = enabled, label = { Text("Disco / cartucho") })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Precintado"); Switch(value.sealed, { change(value.copy(sealed = it)) }, enabled = enabled) }
        Text("Estado físico", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PhysicalCondition.entries.forEach { condition -> FilterChip(value.physical == condition, { change(value.copy(physical = condition)) }, enabled = enabled, label = { Text(condition.label()) }) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun CopyPurchaseFields(value:CopyDetailsState,change:(CopyDetailsState)->Unit,enabled:Boolean) {
    OutlinedTextField(value.price, { change(value.copy(price = it)) }, enabled = enabled, label = { Text("Precio pagado (opcional)") },
        supportingText = { Text(if(value.currency=="JPY") "Yenes enteros, sin fracciones (ej.: 1500)." else if (value.valid) "Importe exacto, hasta dos decimales." else "Introduce un importe válido: por ejemplo 49,95.") },
        isError = !value.valid, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { supportedCurrencies.forEach { currency -> FilterChip(value.currency == currency, { change(value.copy(currency = currency)) }, enabled = enabled, label = { Text(currency) }) } }
}
