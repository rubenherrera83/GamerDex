# Sistemas, estadísticas y Azul 16-bit · 0.8.0

## Selección del gasto

Estadísticas → Ver gasto por año, plataforma y sistema incorpora selectores de año y plataforma. Todos los años / Todas las plataformas conserva el histórico completo; combinarlos hace una intersección. Año desconocido incluye únicamente compras sin fecha, sin inferirla del lanzamiento ni del alta. Se puede limpiar la selección sin modificar fichas.

Los nombres de plataforma son los registrados en cada compra, como en el desglose anterior: Genesis y Mega Drive no se fusionan automáticamente en el historial financiero. Cada moneda se suma por separado. La selección se mantiene al cambiar entre Por año, Por plataforma, Por sistema y Archivadas. En Archivadas el resumen solo incluye las compras archivadas que cumplen los filtros. Una combinación sin compras se explica y no modifica la colección. Los selectores y el contenido se desplazan dentro del diálogo para conservar acceso en pantallas pequeñas.

El contador SISTEMAS de portada usa el número total de unidades del inventario de hardware; dos consolas del mismo modelo cuentan como dos. Abre Mis sistemas, también cuando el inventario está vacío. No se calcula a partir de las plataformas de los juegos ni cambia al filtrar la colección.

## Curiosidades

`SystemKnowledge` contiene fichas revisadas el 18/10/2026 para 26 familias Nintendo, PlayStation y Sega, con textos en español y enlaces por cifra y dato. Los alias explícitos comparten familia: por ejemplo, Genesis / Mega Drive II y Master System II / Mark III. Los datos describen la familia, no las ventas del modelo concreto ni el inventario del usuario.

Se diferencian cifras mundiales, regionales, de un primer año y sell-in (ventas al canal). Las cifras de hardware vigente llevan fecha de referencia y son una instantánea, no un contador actualizado en vivo. Las familias sin cifra o ficha verificada lo muestran expresamente. No se estiman ventas para PCs genéricos ni sistemas sin documentación incorporada. Las fichas se distribuyen con la app y están disponibles sin conexión; los enlaces y el ranking requieren red o caché.

Fuentes primarias consultadas:

- [Historia de hardware de Sega](https://www.sega.jp/history/companyTimeline/en/).
- [Unidades consolidadas de Nintendo, referencia 30/06/2026](https://www.nintendo.co.jp/ir/en/finance/hard_soft/index.html), [historia de Nintendo](https://www.nintendo.com/en-gb/Hardware/Nintendo-History/Nintendo-History-625945.html), [Nintendo 64](https://www.nintendo.com/en-gb/Hardware/Nintendo-History/Nintendo-64/Nintendo-64-625959.html), [historia de GameCube](https://careers.nintendo.com/our-history/), [desarrollo del mando Wii](https://iwataasks.nintendo.com/interviews/wii/wii_remote/0/0/).
- [Cifras oficiales de Sony Interactive Entertainment](https://sonyinteractive.com/en/our-company/business-data-sales/), [historia de PS2 y PSP en español](https://www.playstation.com/es-es/playstation-history/2000-ps2-psp/).

## Top comunitario

Se utiliza RAWG, no ventas ni Metacritic ni la valoración personal. Los IDs de plataforma se verificaron contra el endpoint público `/platforms`. El mapa es explícito: no se atribuye a Switch 2 el catálogo de Switch ni a Evercade el de otra consola. Algunos nombres describen una familia RAWG amplia (por ejemplo, PC para Steam Deck), por lo que el ranking no certifica compatibilidad de un modelo.

La consulta `/games?platforms=ID&ordering=-rating&page_size=40&page=N` recorre hasta cinco páginas y se detiene al encontrar cinco juegos aptos o terminar los resultados. No se sigue una URL arbitraria del campo next: solo se usa para saber si queda otra página. Se comprueba también que las plataformas devueltas correspondan al ID consultado, se eliminan IDs duplicados y se exigen al menos diez votos y una nota válida. La nota RAWG 0–5 se presenta multiplicada por veinte. Los empates conservan el orden recibido; no se inventa un desempate estadístico.

Se muestran hasta cinco candidatos con sus votos y enlaces a RAWG. La nota pertenece globalmente al juego; no es una valoración específica de su versión de consola. El límite de 200 candidatos puede dejar menos de cinco y se declara en la interfaz. La caché HTTP existente puede reutilizar resultados al fallar la conexión; no se presenta como un ranking universal o actualizado en tiempo real. Error de red/clave, carga, lista vacía y plataforma sin correspondencia se muestran por separado; el resto de la ficha sigue accesible. El ranking no modifica el inventario ni se guarda en el ZIP personal.

## Tema

Azul 16-bit añade motivos geométricos originales de anillos, arcos y líneas horizontales, una paleta azul marino/celeste/dorado y un wordmark de GamerDex delineado. Se comparte el dibujo Canvas entre fondos y PNG de gamertag. El icono actual se conserva. La elección se persiste por el nombre estable SEGA y no cambia las elecciones anteriores. No incorpora personajes, música ni el logotipo oficial de Sega.

No cambian el esquema Room v6 ni el formato portable v5.
