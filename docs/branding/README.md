# Identidad GamerDex

Desde 0.4.0, `GamerDexBrand` dibuja variantes de la palabra GamerDex con geometría/pinceles originales y familias de sistema según el tema: inclinada y fragmentada, arcade de píxeles, bloques con volumen, letras multicolor, stencil y otras variantes. Se comparte el mismo dibujo entre Compose y PNG de gamertags. El monograma y el icono del lanzador se conservan. Los SVG de este directorio son los originales de la identidad base, no una exportación de todas las variantes temáticas.

Monograma GD original: trazos sólidos, cortes diagonales y espacio negativo que recuerdan a un cartucho y una bóveda digital. El logotipo incorpora letras geométricas dibujadas como trazados, sin depender de fuentes instaladas. Amarillo ácido `#F5EF26`, cian `#00E6EF` y negro azulado `#080D13` forman la versión principal.

- `gamerdex-icon.svg`: icono con fondo oscuro, lienzo de 108 × 108.
- `gamerdex-symbol.svg`: símbolo transparente, lienzo de 100 × 100.
- `gamerdex-logo.svg`: logotipo transparente, lienzo de 520 × 100.
- `gamerdex-logo-mono.svg`: logotipo blanco para fondos oscuros.
- `gamerdex-icon.png`: exportación Android de 1024 × 1024.
- `gamerdex-logo.png`: logotipo transparente de 2080 × 400.
- `gamerdex-brand-board.png`: lámina de revisión con máscaras y muestra a 48 px.

Los SVG son originales editables. `tools/generate-brand.ps1` mantiene la geometría común de los SVG y los VectorDrawable de Android. Ejecutarlo desde PowerShell regenera los vectores; los PNG se exportan desde `BrandPreviewTest` usando los recursos reales de la aplicación.

El icono del lanzador tiene fondo y primer plano independientes, margen para las máscaras adaptativas y una capa monocroma transparente para iconos temáticos. El logotipo usa dos capas que toman los colores de cada tema en la colección, el menú y los PNG de gamertag. La pantalla de inicio de Android 12 y posteriores utiliza el mismo icono.

Conservar la proporción 5,2:1 del logotipo y el espacio libre alrededor del símbolo. Usar la versión de dos colores sobre fondo oscuro; sobre fondos claros, utilizar los tonos de mayor contraste del tema Kawaii o recolorear los trazados. No añadir sombras ni contornos al monograma.
