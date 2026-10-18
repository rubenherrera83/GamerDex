package com.gamerdex.gamertag

import android.content.Context
import com.gamerdex.core.domain.isValidLogoUrl
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

enum class GamertagLogoKind { PLATFORM, COMPANY, GAME, CUSTOM }
data class GamertagLogo(val name: String, val image: String? = null, val kind: GamertagLogoKind = GamertagLogoKind.CUSTOM) : java.io.Serializable {
    init { require(name.isNotBlank() && name.length <= 80); require(image == null || isGamertagImage(image)) }
}
fun isGamertagImage(value: String): Boolean = isValidLogoUrl(value) || runCatching { java.net.URI(value).scheme in setOf("content", "file") }.getOrDefault(false)

@Singleton class GamertagSettings @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.getSharedPreferences("gamerdex_preferences", Context.MODE_PRIVATE)
    private val selected = MutableStateFlow(decode(store.getString("gamertag_logos_v1", null)))
    val logos = selected.asStateFlow()
    fun setLogos(value: List<GamertagLogo>) {
        require(value.size <= 6)
        val normalized=value.distinct()
        store.edit { putString("gamertag_logos_v1",encode(normalized)) }
        selected.value=normalized
    }
    companion object {
        fun encode(value: List<GamertagLogo>): String = buildJsonArray { value.forEach { logo -> add(buildJsonObject {
            put("name",logo.name); put("kind",logo.kind.name); logo.image?.let { put("image",it) }
        }) } }.toString()
        fun decode(value: String?): List<GamertagLogo> = runCatching {
            Json.parseToJsonElement(value ?: "[]").jsonArray.take(6).mapNotNull { entry -> runCatching {
                val item=entry.jsonObject
                GamertagLogo(item.getValue("name").jsonPrimitive.content,item["image"]?.jsonPrimitive?.contentOrNull,GamertagLogoKind.valueOf(item.getValue("kind").jsonPrimitive.content))
            }.getOrNull() }.distinct()
        }.getOrDefault(emptyList())
    }
}
