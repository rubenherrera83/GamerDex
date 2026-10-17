package com.gamerdex.ui.components

import com.gamerdex.R
import java.util.Locale

object PlatformCatalog {
    val names = listOf("PC", "PlayStation 5", "PlayStation 4", "PlayStation 3", "PlayStation 2", "PlayStation", "PS Vita", "PSP",
        "Xbox Series S/X", "Xbox One", "Xbox 360", "Xbox", "Nintendo Switch 2", "Nintendo Switch", "Wii U", "Wii", "GameCube",
        "Nintendo 64", "SNES", "NES", "Nintendo 3DS", "Nintendo DS", "Game Boy Advance", "Game Boy Color", "Game Boy",
        "Dreamcast", "Sega Saturn", "Mega Drive", "Master System", "Game Gear", "Atari 2600", "Steam Deck", "Android", "iOS")

    fun logo(name: String): Int? {
        val key = name.lowercase(Locale.ROOT).trim().replace(Regex("[\\s_-]+"), " ")
        return when {
            key.contains("playstation") || key.matches(Regex("ps[1-5p]")) || key.contains("ps vita") || key == "psx" -> R.drawable.platform_playstation
            key.contains("xbox") -> R.drawable.platform_xbox
            key.contains("switch") -> R.drawable.platform_nintendoswitch
            key.contains("gamecube") -> R.drawable.platform_nintendogamecube
            key.contains("3ds") || key.contains("2ds") || key == "nintendo ds" || key == "nds" -> R.drawable.platform_nintendo3ds
            key.contains("wii") -> R.drawable.platform_wii
            key.contains("nintendo") || key in setOf("nes", "snes", "n64", "famicom", "super famicom", "ds", "gba", "gbc", "gb") || key.contains("game boy") -> R.drawable.platform_nintendo
            key.contains("sega") || key.contains("dreamcast") || key.contains("mega drive") || key.contains("genesis") || key.contains("master system") || key.contains("game gear") -> R.drawable.platform_sega
            key.contains("atari") -> R.drawable.platform_atari
            key.contains("steam") -> R.drawable.platform_steam
            key == "pc" || key.contains("windows") -> R.drawable.platform_windows
            key.contains("android") -> R.drawable.platform_android
            key in setOf("ios", "macos", "mac", "iphone", "ipad", "apple macintosh") -> R.drawable.platform_apple
            else -> null
        }
    }
}
