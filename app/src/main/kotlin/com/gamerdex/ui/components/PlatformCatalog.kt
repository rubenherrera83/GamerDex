package com.gamerdex.ui.components

import com.gamerdex.R
import java.util.Locale

object PlatformCatalog {
    val names = listOf("PC", "PlayStation 5", "PlayStation 4", "PlayStation 3", "PlayStation 2", "PlayStation", "PS Vita", "PSP",
        "Xbox Series S/X", "Xbox One", "Xbox 360", "Xbox", "Nintendo Switch 2", "Nintendo Switch", "Wii U", "Wii", "GameCube",
        "Nintendo 64", "SNES", "NES", "Nintendo 3DS", "Nintendo DS", "Game Boy Advance", "Game Boy Color", "Game Boy",
        "Dreamcast", "Sega Saturn", "Mega Drive", "Genesis", "Mega Drive II", "Genesis Model 2", "Mega-CD", "Sega CD", "Sega 32X",
        "Master System", "Master System II", "Sega Mark III", "SG-1000", "SC-3000", "Game Gear",
        "Famicom", "Super Famicom", "Famicom Disk System", "Super Nintendo", "New Nintendo 3DS", "Nintendo 2DS",
        "Atari 2600", "Atari 5200", "Atari 7800", "Atari Jaguar", "Atari Jaguar CD", "Atari Lynx", "Atari 400", "Atari 800", "Atari ST",
        "3DO", "Neo Geo AES", "Neo Geo CD", "Neo Geo Pocket", "Neo Geo Pocket Color", "PC Engine", "TurboGrafx-16", "PC Engine CD", "TurboGrafx-CD",
        "ColecoVision", "Intellivision", "Magnavox Odyssey 2", "Philips Videopac", "Philips CD-i", "Vectrex",
        "Commodore 64", "Commodore Amiga", "ZX Spectrum", "Amstrad CPC", "MSX", "MSX2", "BBC Micro", "Acorn Electron", "Apple II", "Apple Macintosh",
        "Evercade", "Evercade VS", "Evercade VS-R", "Evercade EXP", "Evercade EXP-R", "Evercade Alpha", "Evercade Nexus", "Super Pocket",
        "Steam Deck", "Linux", "macOS", "Android", "iOS")

    fun family(name: String): String {
        val key=name.lowercase(Locale.ROOT).trim()
        return when(key) {
            "genesis", "mega drive", "mega drive ii", "genesis model 2", "sega genesis" -> "mega drive"
            "snes", "super nintendo", "super famicom", "super famicon" -> "snes"
            "nes", "famicom", "famicon", "nintendo entertainment system" -> "nes"
            "master system", "master system ii", "sega master system", "sega mark iii", "sega mark 3" -> "master system"
            else -> key
        }
    }
    fun matches(name: String, query: String) = name.contains(query,true) || (query.isNotBlank() && family(name).contains(family(query),true))
    fun display(name: String) = when(family(name)) { "mega drive" -> "$name / Mega Drive–Genesis"; "snes" -> "$name / Super Famicom"; "nes" -> "$name / Famicom"; else -> name }

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
            key.contains("sega") || key.contains("dreamcast") || key.contains("mega drive") || key.contains("genesis") || key.contains("master system") || key.contains("game gear") || key in setOf("sg 1000", "sc 3000", "mega cd") -> R.drawable.platform_sega
            key.contains("atari") -> R.drawable.platform_atari
            key.contains("steam") -> R.drawable.platform_steam
            key == "pc" || key.contains("windows") -> R.drawable.platform_windows
            key.contains("android") -> R.drawable.platform_android
            key in setOf("ios", "macos", "mac", "iphone", "ipad", "apple macintosh", "apple ii") -> R.drawable.platform_apple
            else -> null
        }
    }
}
