# GamerDex

Versión 0.9.0: gamertags de PCs y sistemas con componentes, temas Escritorio Aurora y Órbita Verde.

## Novedades 0.9.0

- Nuevo modo Sistema / PC en Gamertags y acceso directo desde la ficha de hardware. Usa el nombre, plataforma y primera foto del equipo, con hasta diez componentes/accesorios elegidos y sus cantidades; las notas son opcionales. Sirve también para consolas y microordenadores.
- Panel de componentes en dos columnas, fotografía ajustada al marco, totales globales y hasta seis logotipos. Guardar y compartir siguen usando PNG 1600 × 900. Los textos largos se recortan y se muestra ese criterio en el editor.
- Los PCs registran componentes en los accesorios existentes, con etiquetas de componente al elegir PC/ordenador. No se copian a la tarjeta los campos de serie, precio, fecha ni las notas generales del sistema.
- Temas «Escritorio Aurora» (PC/Windows, cintas azules y lavanda) y «Órbita Verde» (Xbox, órbitas y verde), con fondos, paletas y variantes del wordmark compartidas por app y exportaciones. El icono se conserva.
- APK `GamerDex-0.9.0-debug.apk`; Room v6 y ZIP v5 no cambian. [Uso de los gamertags](docs/gamertags.md).

## Novedades 0.8.0

- El historial de gasto permite combinar año y plataforma, elegir Año desconocido y limpiar filtros. El resumen y los desgloses usan la misma selección, sin convertir monedas.
- «Azul 16-bit», inspirado en la era Sega: fondo azul marino, acentos celestes/dorados, anillos, líneas de velocidad y wordmark delineado. Disponible en Preferencias y en los gamertags; el icono de app se conserva.
- Las fichas de hardware incorporan curiosidades en español y cifras de unidades con fuentes y fecha de revisión. Catálogo inicial de 26 familias; una plataforma sin ficha lo indica y una cifra desconocida no se estima.
- Top de hasta cinco juegos RAWG por plataforma, con al menos diez votos y nota global 0–100. Se consultan hasta 200 candidatos ordenados por nota; las valoraciones no son específicas de cada versión. Hay estados de carga, error, reintento, ausencia de votos y plataforma no soportada.
- El contador de sistemas de portada cuenta unidades del inventario, también modelos repetidos, y abre «Mis sistemas». Los filtros de colección no cambian ese total.
- APK `GamerDex-0.8.0-debug.apk`; Room v6 y copias v5 no cambian. [Criterios y fuentes](docs/systems-and-statistics.md).

## Novedades 0.7.1

- ScanDex sigue siendo la primera fuente; UPCitemdb gratuito actúa como respaldo ante ausencia de coincidencia, token ausente/rechazado, cuota o fallo de servicio. No requiere una clave adicional.
- La búsqueda identifica la fuente y conserva el aviso de fallo de ScanDex si utiliza el respaldo. El usuario confirma título y plataforma en RAWG antes de importar. No se importan ofertas como precio pagado.
- UPCitemdb respeta un intervalo de diez segundos entre peticiones, evita consultas repetidas mediante caché temporal y aplica una pausa cuando recibe un límite del servidor. La cuota gratuita por IP es compartida; el respaldo no garantiza cobertura de todas las ediciones.
- APK con nombre y versión automáticos: `GamerDex-0.7.1-debug.apk`. Room v6 y copias v5 no cambian.

## Novedades 0.7.0

- El alta ofrece Escanear código de barras, Introducir EAN/UPC y Buscar cartucho Evercade directamente. Los códigos incompletos se explican antes de consultar; ScanDex distingue códigos desconocidos, coincidencias pendientes, credenciales rechazadas y cuotas.
- Evercade y sus variantes se añaden al catálogo de sistemas. Un buscador independiente consulta el catálogo público oficial de cartuchos, importa nombre/portada/enlace y anota los juegos incluidos. No crea IDs RAWG ni equivale a una API oficial de Evercade.
- Precios en JPY para juegos y hardware, con yenes enteros y sin conversión automática de monedas.
- Fecha de compra opcional para juegos y vinculación a una unidad de Mis sistemas. Estadísticas muestra gasto de videojuegos, hardware y conjunto, por moneda, año, plataforma y unidad; las fechas desconocidas permanecen sin atribuir a un año.
- El historial conserva las compras al eliminar fichas. Se pueden retirar compras archivadas desde Estadísticas. Las correcciones de precio/fecha actualizan la compra de esa copia sin sumarla otra vez. No reconstruye fichas eliminadas antes de actualizar.
- Room v6 añade fechas/vínculo/fuente en juegos y un registro de compras. Copias v5 incluyen el historial y leen v1–v4; restaurar sustituye también el historial.

[ScanDex, UPCitemdb, Evercade y criterios del gasto](docs/search-and-spending.md).

## Novedades 0.6.1

- Todos los gamertags, también los de un juego o plataforma, muestran el total de copias de videojuegos y de unidades registradas en Mis sistemas.
- Hasta seis logotipos ordenables y eliminables: consolas del catálogo, compañías de la colección, carátulas y cualquier logotipo propio mediante imagen local o URL HTTPS. Los logotipos específicos de juegos se añaden como imagen propia; una carátula no se presenta como si fuera un logotipo.
- La selección persiste entre sesiones. PNG de 1600 × 900 con banda de totales y fila de marcas, incluida al guardar y compartir. Se conserva la proporción de las imágenes.
- No cambia Room v5 ni el formato de copia v4. La personalización del gamertag es una preferencia local, separada del ZIP de la colección.

[Detalles del editor y las imágenes](docs/gamertags.md).

## Novedades 0.6.0

- Cuadrícula es la vista inicial cuando no existe una elección guardada. Se conservan las preferencias anteriores del usuario.
- «Mis sistemas» registra consolas, PCs, portátiles y microordenadores, múltiples unidades, caja/manual/cables, estado y funcionamiento, serie, fecha y precio, mandos con cantidades y detalles propios, notas, fotos y logotipo personalizable.
- Catálogo ampliado con nombres regionales y sistemas clásicos. RAWG muestra todas las plataformas y pide elegir la versión; hasta 40 resultados con filtros locales por año y sistema.
- ScanDex opcional: lectura de código con Google Play Services o entrada manual, consulta EAN/UPC y búsqueda posterior del título en RAWG. Requiere `SCANDEX_API_TOKEN` en `local.properties` para desarrollo.
- Room v5 y copias v4 con hardware y fotos, compatibles con lectura de v1–v3. Restaurar una copia antigua vacía el inventario de sistemas y lo advierte antes de confirmar.

[Funcionamiento, imágenes y preparación para producción](docs/catalog-and-production.md).

## Novedades 0.5.0

- Selector **Lista / Cuadrícula / Cover Flow**. Lista conserva las tarjetas anteriores; Cuadrícula presenta tres carátulas por fila y cuatro desde 600 dp de ancho. Cover Flow conserva el carrusel horizontal. La elección persiste y se recupera la preferencia de versiones anteriores.
- La ficha distingue **Tu nota** de **Comunidad RAWG**, con número de votos y conversión explícita de la escala 0–5 a 0–100. Se consulta para juegos vinculados a RAWG; una media ausente o sin votos no se presenta como cero. Los errores de valoración y multimedia se gestionan por separado.
- Las notas usan rojo por debajo de 50, amarillo entre 50 y 74 y verde desde 75, con tonos graduales y etiquetas de nivel para la nota personal.
- Se mantienen Room v4 y las copias portables v3. Las medias externas no sustituyen datos personales ni se incluyen en la copia de la colección. [Detalles de las valoraciones](docs/rawg-ratings.md).

## Novedades 0.4.1

La valoración personal usa 0–100 en el formulario y la ficha. La migración Room 3 → 4 multiplica por 20 las notas anteriores: 4,5/5 pasa a 90/100; cero y ausencia de valoración siguen siendo distintos. La conversión se realiza una sola vez, al actualizar la base.

Las nuevas copias usan el formato v3. Al leer copias v1/v2 se convierten sus notas de 0–5 y se normaliza el manifiesto a v3. Exportar y recuperar de nuevo una copia v3 conserva las notas sin multiplicarlas, incluidos valores bajos como 3/100. Las versiones antiguas de la app no leen el nuevo formato de copia.

RAWG puede proporcionar una media de usuarios y puntuaciones Metacritic cuando existan. La media de usuarios se muestra desde 0.5.0; Metacritic no está integrado. [Datos disponibles](docs/rawg-ratings.md).

## Novedades 0.4.0

- Cada copia registra favorito, historia finalizada, porcentaje 0–100 y platino manual en PlayStation 3/4/5. La nota personal es 0–100 desde 0.4.1. Historia finalizada, porcentaje y contenido físico son independientes; un platino tampoco presupone el 100 % de todos los DLC.
- Favoritos permite buscar y filtrar por sistema. Estadísticas muestra totales, finalizados, 100 %, platinos, favoritos, copias físicas completas y distribuciones por plataforma/género; las cifras abren sus juegos. Un juego puede pertenecer a varios géneros.
- La portada coloca logo, contadores y búsqueda al principio. Los contadores abren todos los títulos, sistemas y ediciones especiales. Se elimina el panel decorativo grande; sus motivos pasan al fondo de las pantallas.
- Los nueve temas incluyen variantes tipográficas originales del logotipo y motivos reconocibles de sus géneros visuales. Se conserva el icono GD. No se incluyen fuentes, personajes ni sprites de videojuegos comerciales.
- La ficha carga capturas y tráileres mediante el ID RAWG guardado al autocompletar. Los juegos antiguos/manuales pueden vincularse editando y usando RAWG. Capturas ampliables y reproductor de vídeo con controles, sin reproducción automática. La disponibilidad depende del juego, conexión y acceso API; un fallo de tráileres no oculta las capturas. Atribución y enlace a RAWG incluidos.
- Preferencias guarda un apodo de hasta 32 caracteres. Los nuevos gamertags lo usan como valor inicial, editable por tarjeta. «Compartir gamertag» abre el selector Android con PNG de 1600 × 900 para apps compatibles. FileProvider expone únicamente imágenes temporales de la carpeta de compartir, con permiso de lectura por URI. Los archivos de más de 24 horas se limpian al preparar otro envío; la base de datos no está expuesta.
- Room v3 incorpora migración 2 → 3 conservando los datos anteriores. Las nuevas copias ZIP son formato v2 e incluyen los campos nuevos; se siguen leyendo copias v1. Las versiones antiguas de la app no pueden abrir copias v2. Tema, vista de colección y apodo siguen siendo preferencias locales separadas de la copia de la colección.

API multimedia: [documentación de RAWG](https://api.rawg.io/docs/). Procedencia de iconos de plataforma: [créditos](docs/platform-icons/README.md).

La [identidad visual](docs/branding/README.md) incluye originales SVG, exportaciones PNG y recursos Android vectoriales. El logotipo se adapta a los nueve temas y aparece en la colección, el menú y las tarjetas exportadas.

## Novedades 0.3.0

- Cinco temas adicionales con motivos geométricos originales: Salto Pop (aventura de plataformas y colores vivos), Órbita Pixel (arcade espacial), Mundo Vóxel (bloques y tierra), Zona Royale (energía violeta y cian) y Operación Táctica (oliva, arena y cartografía). No incluyen personajes, sprites ni logotipos de juegos. Disponibles desde Preferencias y en el generador de gamertags.
- Selector «Elegir» junto a Plataforma: búsqueda de consolas, logotipos locales y escritura libre de sistemas personalizados. Los mismos iconos aparecen en filtros, exploración, detalle y carrusel. Algunas generaciones comparten logotipo de familia. [Procedencia y licencia](docs/platform-icons/README.md).
- «Cuadrícula / Carátulas» en la colección: carrusel horizontal con ajuste central, perspectiva, reflejo, contador, botones anterior/siguiente y acceso al detalle. Respeta búsqueda, plataforma y género. Recuerda la vista elegida al cerrar la app; no modifica los datos ni el formato de copias de seguridad.

Aplicación Android modular, Kotlin 2.1.20, JDK 21 con bytecode JVM 17, min SDK 26 y compile/target SDK 35.
Las versiones están fijadas deliberadamente; no se presentan como las últimas disponibles.

## Archivos para revisión

- `gradle/libs.versions.toml`: catálogo completo de dependencias para las cinco fases.
- `core/domain`: modelos, validación y contratos de repositorio independientes de Android.
- `core/database`: entidades Room, DAO reactivo, repositorio transaccional, conversores JSON, esquema v1 y proveedor Hilt.
- `core/network`: Retrofit, serialización, cliente OkHttp e integración RAWG.
- `core/backup`: ZIP portable con manifiesto JSON, fotos embebidas y validación antes de restaurar.
- `app`: Application Hilt, ViewModel con StateFlow, navegación y pantallas Compose.

## Funciones

Cada copia permite distinguir edición estándar, especial o coleccionista; caja, instrucciones y disco/cartucho; estado físico independiente; precintado o digital; precio pagado y moneda (EUR, USD o GBP); y géneros. Los importes se guardan como céntimos enteros, sin redondeos de coma flotante. Los géneros pueden importarse de RAWG o escribirse manualmente.

El menú lateral contiene Colección, Favoritos, Plataformas, Géneros, Estadísticas, Gamertags y Preferencias. Las vistas de plataformas y géneros aplican filtros sobre la colección. En Preferencias se elige entre los nueve temas; cambian colores, motivos decorativos, formas y cabecera, y la elección persiste tras cerrar la app.

En Gamertags puedes elegir juego, plataforma o colección, personalizar tu alias y guardar un PNG de 1600 × 900. El diseño sigue el tema activo. También se accede desde el detalle de un juego. La imagen se genera localmente con Canvas; las portadas remotas usan Coil y su caché.

Bóveda con Lista, Cuadrícula compacta y Cover Flow, búsqueda local, filtros por plataforma y métricas de colección. Alta y edición manual o por autocompletado RAWG, condición, valoración 0–100, notas, edición especial y fotos PhotoPicker o cámara integrada CameraX. Detalle con fondo difuminado, galería paginada y visor ampliable. Borrado con confirmación. Tema OLED con acentos cian, verde y púrpura, bordes iluminados y placeholders shimmer.

La cámara solicita permiso al pulsar «Tomar foto», permite captura con cámara trasera (o frontal si no existe trasera) y conserva el JPEG en el almacenamiento privado de la app. Los borradores de cámara se eliminan al salir sin guardar; las rotaciones conservan las fotos del formulario. Si no hay cámara o permiso, PhotoPicker sigue disponible. Las fotos externas originales nunca se eliminan desde el formulario. Un cierre abrupto del proceso puede dejar una foto de borrador sin asociar en el almacenamiento privado.

«Personalizar logotipos» permite configurar URLs HTTPS de plataforma y compañía, incluidas imágenes SVG. Se guardan en Room y se comparten entre los juegos del mismo catálogo; dejar el campo vacío conserva el logotipo existente. Los chips y el detalle los cargan mediante la caché compartida de Coil; sin imagen o ante un error se muestran iniciales. RAWG no proporciona esos logotipos en el flujo actual: las URLs se configuran manualmente.

Room es la fuente de verdad y las operaciones de colección funcionan sin red. RAWG requiere conexión o una respuesta HTTP cacheada reutilizable. OkHttp permite volver a una respuesta almacenada de hasta siete días si falla la conexión; la disponibilidad depende de las directivas de caché del servidor. Coil usa una instancia compartida con caché de memoria (20%) y disco (100 MB). La búsqueda cancela consultas anteriores y aplica debounce de 350 ms. Las listas usan claves estables y la UI observa el estado con ciclo de vida.

## Ejecutar

1. Configura JDK 21 y Android SDK 35.
2. Copia `local.properties.example` a `local.properties` y ajusta `sdk.dir`.
3. Para buscar metadatos, añade tu `RAWG_API_KEY` de https://rawg.io/apidocs. Sin clave funciona el formulario manual.
4. Ejecuta `./gradlew.bat :app:assembleDebug` o abre el proyecto en Android Studio.

Los APK llevan nombre y versión de la app: `GamerDex-0.9.0-debug.apk` para esta versión. El nombre se toma automáticamente de `versionName`; se conserva el sufijo de variante para distinguir compilaciones debug y release.

La API key de RAWG se inyecta en BuildConfig y puede extraerse del APK. Para distribución pública, sustituir el acceso directo por un backend con credenciales y cuotas propias. No se usan secretos Twitch. El formulario y buscador atribuyen metadatos a RAWG; comprobar sus condiciones de uso para la distribución prevista.

## Esquema Room v6

| Tabla | Clave primaria | Restricciones e índices |
| --- | --- | --- |
| platforms | id: Long autogenerado | name obligatorio y único; generation y logoUrl opcionales; colorHex obligatorio |
| companies | id: Long autogenerado | name obligatorio y único; logoUrl opcional |
| games | id: Long autogenerado | platformId y companyId obligatorios; índices en ambas FK, title, createdAt e isSpecialEdition |
| hardware | id: Long autogenerado | payload JSON con ficha de sistema y accesorios, validado en el dominio |
| purchases | key: String (`GAME:id` / `HARDWARE:id`) | payload JSON de adquisición, conservado al borrar la unidad de la colección |

`games.platformId → platforms.id` y `games.companyId → companies.id`: ON DELETE RESTRICT y ON UPDATE CASCADE. Se permiten varias copias de un mismo título y plataforma. Los nombres únicos usan la comparación BINARY de SQLite; la normalización de nombres corresponderá al repositorio.

Game contiene title, releaseYear nullable, coverUrl nullable, localCustomPhotos como TEXT JSON, isSpecialEdition, specialEditionDetails nullable, condition como TEXT con el nombre estable del enum, rating nullable (0–100), notes nullable y createdAt en milisegundos UTC.

En v2 se añaden editionType, hasBox, hasManual, hasMedia, physicalCondition, pricePaidCents nullable, priceCurrency y genres como lista JSON. El indicador isSpecialEdition se conserva para compatibilidad y se actualiza a partir del tipo de edición al guardar.

En v3 se añaden isFinished, completionPercent, hasPlatinum, isFavorite y rawgId nullable. El dominio valida el porcentaje 0–100 y restringe el platino a PS3/PS4/PS5; las pantallas ofrecen el control únicamente para esos sistemas.

En v4 se conservan las columnas y se convierte rating a 0–100. La migración transforma los valores existentes; la versión del esquema distingue las notas antiguas de las nuevas, sin inferir su escala a partir del número. V5 añade hardware sin modificar juegos, plataformas ni compañías.

V6 añade purchasedOn, ownedSystemId y catalogUrl opcionales en juegos y el registro de compras. Una adquisición se actualiza al corregir su ficha y permanece al borrar el juego o sistema. La fecha desconocida se conserva como tal; no se utiliza el año de lanzamiento como fecha de compra.

Las entidades y el dominio validan título/nombre no vacío, año 1950–2100, valoración finita 0–100, URIs no vacías y coherencia de edición especial; la entidad Platform valida el color hexadecimal. Estas validaciones Kotlin no son CHECK de SQLite; las restricciones SQL efectivas son PK, FK, NOT NULL y UNIQUE. El repositorio normaliza espacios exteriores y guarda referencias y juego en una única transacción.

Las fotos guardan referencias URI, no bytes. PhotoPicker conserva el permiso persistente antes de guardar la URI. Si se revoca posteriormente o se elimina el archivo original, aparece el placeholder. No se requiere permiso de acceso a toda la biblioteca.

## Migraciones y validación

Room exporta los esquemas v1–v6 a `core/database/schemas`. La migración 1 → 2 conserva juegos, fotos, notas y referencias; convierte las ediciones especiales y los estados anteriores en los nuevos campos. La migración 2 → 3 inicializa progreso y favorito sin alterar valoraciones ni adquisición; la 3 → 4 convierte las notas a 0–100; la 4 → 5 añade el inventario de hardware; la 5 → 6 inicia el historial a partir de las fichas existentes. Las migraciones tienen pruebas con bases de versiones anteriores reales. No se permite recreación destructiva.

## Copia y recuperación

En Preferencias, «Crear copia de seguridad» abre el selector de documentos de Android. El ZIP v5 incluye todos los juegos, sistemas, plataformas, compañías e historial de compras, sus identificadores y campos, y los bytes de las fotos locales. Se leen copias v1–v5. Las portadas y logotipos remotos se conservan como URLs. No incluye credenciales RAWG ni preferencias de tema. El formato portable evita copiar una base SQLite abierta con archivos WAL pendientes.

«Recuperar copia de seguridad» valida versión, integridad referencial, fotos, rutas y tamaños antes de mostrar juegos, sistemas, compras, fotos y fecha. Al confirmar, la colección y su historial se sustituyen en una sola transacción Room y las fotos se copian al almacenamiento privado. Una copia inválida no modifica la colección. Los archivos fotográficos originales de la colección anterior se conservan.

Límites por archivo de copia: 512 MiB descomprimidos, 32 MiB por foto, 8 MiB de manifiesto y hasta 5000 fotos. Si una foto original ya no se puede leer, la exportación falla y no se presenta como completa. Las copias son ZIP sin cifrar; guárdalas en una ubicación privada si incluyen datos personales.

El Gradle Wrapper fija Gradle 8.11.1. Comandos de validación:

```powershell
.\gradlew.bat :core:domain:test :core:network:testDebugUnitTest :core:backup:testDebugUnitTest :app:assembleDebug :app:lintDebug
# Con dispositivo o emulador Android conectado:
.\gradlew.bat :core:database:connectedDebugAndroidTest :app:connectedDebugAndroidTest
# Para excluir la prueba de RAWG que necesita Internet y consume cuota:
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.notAnnotation=androidx.test.filters.LargeTest
```

Pruebas: invariantes de dominio y URLs de logotipos; parseo RAWG y consulta codificada; clave ausente; integridad referencial y logotipos compartidos en Room; recorrido Compose de alta, detalle, edición y eliminación; captura CameraX, conservación de foto tras recreación y visor; autocompletado real de RAWG con guardado local y carga de imágenes desde disco sin acceso de red. La prueba real RAWG se omite si no hay clave y puede excluirse con el comando anterior. Los resultados se documentan en `VALIDATION.md`.

## Límites actuales

No hay sincronización automática entre dispositivos ni copias programadas. Los logotipos personalizados externos requieren URLs configuradas por el usuario; las plataformas conocidas tienen recursos locales. No se ha realizado una auditoría de rendimiento en hardware físico ni validado una publicación en Play Store. Las fotos privadas de cámara se pierden al desinstalar la app si no se exportan antes en una copia. El borrado de un juego no borra los archivos fotográficos originales ni los JPEG privados previamente guardados. La disponibilidad de destinos de compartir depende de las apps instaladas; su flujo de publicación final pertenece a cada app.
