package com.gamerdex.core.domain

import java.util.Locale

data class SystemFact(val text:String,val source:String)
data class SystemHistory(val family:String,val sales:String?,val facts:List<SystemFact>,val salesSource:String?=null)

/** Explicit aliases only: a model must never silently turn into a different console. */
object SystemKnowledge {
    private val aliases=mapOf(
        "mega drive" to listOf("Genesis","Sega Genesis","Mega Drive II","Genesis Model 2","Sega Mega Drive","Sega Mega Drive II"),
        "master system" to listOf("Master System II","Sega Master System","Sega Mark III","Sega Mark 3"),
        "snes" to listOf("Super Nintendo","Super Famicom","Super Famicon"),"nes" to listOf("Famicom","Famicon","Nintendo Entertainment System"),
        "playstation" to listOf("PS1","PSX","PS One"),"playstation 2" to listOf("PS2"),"playstation 3" to listOf("PS3"),"playstation 4" to listOf("PS4","PS4 Pro"),"playstation 5" to listOf("PS5","PS5 Pro"),
        "nintendo switch" to listOf("Switch","Nintendo Switch OLED","Nintendo Switch Lite"),"nintendo 3ds" to listOf("Nintendo 2DS","New Nintendo 3DS"),"nintendo ds" to listOf("Nintendo DS Lite"),
        "sega saturn" to listOf("Saturn"),"dreamcast" to listOf("Sega Dreamcast"),"game gear" to listOf("Sega Game Gear"),
        "sega cd" to listOf("Mega-CD","Mega CD","Sega Mega-CD"),"sega 32x" to listOf("32X"),"jaguar" to listOf("Atari Jaguar"),"gamecube" to listOf("Nintendo GameCube"),
        "ps vita" to listOf("PlayStation Vita"),"xbox series s/x" to listOf("Xbox Series X","Xbox Series S","Xbox Series X/S"),"pc" to listOf("Windows","Steam Deck"),
        "neo geo" to listOf("Neo Geo AES"),"commodore / amiga" to listOf("Commodore Amiga"),"classic macintosh" to listOf("Apple Macintosh"),"atari 8-bit" to listOf("Atari 400","Atari 800")
    )
    private fun normalize(name:String)=name.trim().lowercase(Locale.ROOT)
    fun family(name:String):String {val key=normalize(name);return aliases.entries.firstOrNull{(base,other)->key==base || other.any{normalize(it)==key}}?.key ?: key}
    private val ids=mapOf("pc" to 4,"playstation 5" to 187,"playstation 4" to 18,"xbox one" to 1,"xbox series s/x" to 186,"nintendo switch" to 7,"ios" to 3,"android" to 21,
        "nintendo 3ds" to 8,"nintendo ds" to 9,"nintendo dsi" to 13,"macos" to 5,"linux" to 6,"xbox 360" to 14,"xbox" to 80,"playstation 3" to 16,"playstation 2" to 15,"playstation" to 27,"ps vita" to 19,"psp" to 17,
        "wii u" to 10,"wii" to 11,"gamecube" to 105,"nintendo 64" to 83,"game boy advance" to 24,"game boy color" to 43,"game boy" to 26,"snes" to 79,"nes" to 49,"apple ii" to 41,"atari 2600" to 23,"atari 5200" to 31,"atari 7800" to 28,"atari st" to 34,"atari lynx" to 46,
        "mega drive" to 167,"sega saturn" to 107,"sega cd" to 119,"sega 32x" to 117,"master system" to 74,"dreamcast" to 106,"3do" to 111,"jaguar" to 112,"game gear" to 77,"neo geo" to 12,"commodore / amiga" to 166,"classic macintosh" to 55,"atari 8-bit" to 25)
    fun rawgId(name:String)=ids[family(name)]
    private const val sega="https://www.sega.jp/history/companyTimeline/en/"
    private const val nintendo="https://www.nintendo.co.jp/ir/en/finance/hard_soft/index.html"
    private const val history="https://www.nintendo.com/en-gb/Hardware/Nintendo-History/Nintendo-History-625945.html"
    private const val sony="https://sonyinteractive.com/en/our-company/business-data-sales/"
    private const val ps2="https://www.playstation.com/es-es/playstation-history/2000-ps2-psp/"
    private fun n(name:String,sales:String,fact:String)=SystemHistory(name,"$sales millones · ventas mundiales consolidadas de la familia, a 30/06/2026",listOf(SystemFact(fact,history)),nintendo)
    private val profiles=mapOf(
        "mega drive" to SystemHistory("Mega Drive / Genesis","Más de 15 millones en Norteamérica y 8 millones en Europa; no es un total mundial",listOf(SystemFact("En Norteamérica se llamó Genesis; llegó allí en 1989 y a Europa en 1990.",sega)),sega),
        "master system" to SystemHistory("Master System / Mark III",null,listOf(SystemFact("La Mark III, lanzada en 1985, tuvo accesorios de sonido FM y gafas 3D.",sega))),
        "sg-1000" to SystemHistory("SG-1000","160.000 en el primer año; no es el total histórico",listOf(SystemFact("SG abrevia SEGA GAME. Fue la primera consola doméstica de Sega.",sega)),sega),
        "sega saturn" to SystemHistory("Sega Saturn",null,listOf(SystemFact("Su nombre alude al sexto planeta: Sega la presentaba como su sexta consola doméstica.",sega))),
        "dreamcast" to SystemHistory("Dreamcast",null,listOf(SystemFact("Fue la última consola doméstica de Sega e incorporó comunicación en línea de serie.",sega))),
        "game gear" to SystemHistory("Game Gear",null,listOf(SystemFact("Su sintonizador opcional permitía usar su pantalla en color como televisor portátil.",sega))),
        "nes" to n("NES / Famicom","61,91","Nintendo registra conjuntamente la familia Famicom / NES, nombres que cambian según el mercado."),
        "snes" to n("SNES / Super Famicom","49,10","Super Famicom es el nombre japonés de la familia conocida como Super Nintendo en otros mercados."),
        "nintendo 64" to n("Nintendo 64","32,93","Nació con cuatro puertos de mando, pensados para partidas multijugador locales.").copy(facts=listOf(SystemFact("Incluía cuatro puertos de mando sin necesitar un adaptador multijugador.","https://www.nintendo.com/en-gb/Hardware/Nintendo-History/Nintendo-64/Nintendo-64-625959.html"))),
        "gamecube" to n("Nintendo GameCube","21,74","Fue la primera consola de Nintendo cuyo soporte principal eran discos ópticos.").copy(facts=listOf(SystemFact("Fue la primera consola de Nintendo cuyo soporte principal eran discos ópticos.","https://careers.nintendo.com/our-history/"))),
        "game boy" to n("Familia Game Boy","118,69","Game Boy Camera y Game Boy Printer ampliaban la portátil con fotografía e impresión."),
        "game boy color" to SystemHistory("Game Boy Color",null,listOf(SystemFact("Se presentó en 1998 junto a una etapa que también destacó por Game Boy Camera y Printer.",history))),
        "game boy advance" to n("Game Boy Advance","81,51","Recibió ediciones especiales europeas como Classic NES, Tribal y Zelda."),
        "nintendo ds" to n("Nintendo DS","154,02","La pantalla táctil permitió controlar directamente la exploración en Phantom Hourglass."),
        "nintendo 3ds" to n("Familia Nintendo 3DS","75,94","La familia 3DS figura separada de Nintendo DS en los informes de ventas de Nintendo.").copy(facts=listOf(SystemFact("Nintendo contabiliza las familias DS y 3DS por separado en sus informes.",nintendo))),
        "wii" to n("Wii","101,63","El diseño de su mando se desarrolló buscando nuevas maneras de interactuar con los juegos.").copy(facts=listOf(SystemFact("El equipo exploró incluso incluir una pantalla táctil durante el desarrollo del mando.","https://iwataasks.nintendo.com/interviews/wii/wii_remote/0/0/"))),
        "wii u" to n("Wii U","13,56","Nintendo contabiliza Wii U como una familia independiente de Wii.").copy(facts=listOf(SystemFact("Wii y Wii U aparecen como familias independientes en los informes de ventas.",nintendo))),
        "nintendo switch" to n("Familia Nintendo Switch","156,59","Las cifras de la primera Switch se publican separadas de Switch 2.").copy(facts=listOf(SystemFact("Las cifras de Switch y Switch 2 se publican por separado.",nintendo))),
        "nintendo switch 2" to n("Nintendo Switch 2","23,68","Nintendo publica sus cifras separadas de las de la primera Switch.").copy(facts=listOf(SystemFact("Switch 2 se contabiliza como una plataforma distinta de la primera Switch.",nintendo))),
        "playstation" to SystemHistory("PlayStation","Más de 102,4 millones · sell-in mundial; fuente a 31/03/2012",listOf(SystemFact("PS2 mantuvo compatibilidad con los discos de la primera PlayStation.",ps2)),sony),
        "playstation 2" to SystemHistory("PlayStation 2","Más de 160 millones · sell-in mundial comunicado por Sony",listOf(SystemFact("Además de videojuegos, reproducía DVD y era retrocompatible con discos de PlayStation.",ps2)),sony),
        "psp" to SystemHistory("PSP","Más de 76,4 millones · sell-in mundial; fuente a 31/03/2012",listOf(SystemFact("Sus discos UMD se utilizaron tanto para juegos como para películas y música.",ps2)),sony),
        "playstation 3" to SystemHistory("PlayStation 3","Más de 87,4 millones · sell-in mundial; fuente a 31/03/2017",listOf(SystemFact("Sony publica sus cifras como sell-in: unidades distribuidas al canal, no compras finales.",sony)),sony),
        "playstation 4" to SystemHistory("PlayStation 4","Más de 117 millones · sell-in mundial; fuente a 30/06/2022",listOf(SystemFact("Las cifras oficiales de hardware incluyen productos devueltos y reacondicionados.",sony)),sony),
        "playstation 5" to SystemHistory("PlayStation 5","Más de 95 millones · sell-in mundial; fuente a 30/06/2026",listOf(SystemFact("Sony distingue las ventas al canal (sell-in) de las ventas al consumidor final.",sony)),sony),
        "ps vita" to SystemHistory("PS Vita",null,listOf(SystemFact("Sony indica que no publica las cifras de ventas de hardware de PS Vita en esta tabla.",sony)))
    )
    fun history(platform:String)=profiles[family(platform)]
}
