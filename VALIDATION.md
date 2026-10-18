# Validación de GamerDex

Versión validada: 0.7.0 · Room v6 · formato de copia portable v5 (lectura v1–v5).

Fecha: 18 de octubre de 2026.

Entorno: Windows, JDK Microsoft 21.0.8, Gradle 8.11.1, Android SDK 35 y emulador Medium Phone Android 15 (API 35, x86_64).

## Resultados

- Ajuste posterior del empaquetado: `:app:assembleDebug` correcto; el directorio de salida y `output-metadata.json` contienen `GamerDex-0.7.0-debug.apk`, versión 0.7.0 y código 10. El nombre procede de `versionName` y mantiene la variante. No cambia el comportamiento de la app.
- Validación 0.7.0: APK, instrumentación y Lint compilados. Lint: 0 errores y las mismas 82 advertencias anteriores, sin baseline ni supresiones. APK instalado y arranque en frío correcto en API 35.
- 69 pruebas distintas correctas: dominio (12), ZIP (11), proveedores HTTP (8), Room/migraciones (12) y Android (26). La batería Android inicial tuvo dos fallos de desplazamiento en listas diferidas; tras ajustar los selectores se repitieron correctamente los cuatro casos de las dos clases afectadas. No quedan fallos pendientes.
- ScanDex: consulta real con credencial local válida y código público 0711719577966: HTTP 200 y Horizon Forbidden West: Complete Edition / PS5. El código proporcionado 502655067591 tiene checksum inválido; el EAN publicado 5026555067591 para Civilization VI Switch también devuelve 404 con la misma credencial. Se distinguen 404, 200 sin juego asociado, 401/403 y cuota 429 mediante pruebas HTTP. El formulario directo rechaza el código incompleto antes de consultar y mantiene los ceros iniciales. La captura óptica en teléfono real sigue pendiente; no se presenta como validada.
- Evercade: prueba Compose contra el catálogo oficial real, búsqueda y selección de Atari Collection 1, importación de plataforma, imagen, URL y lista con Adventure. Se guarda sin ID RAWG y se verifica su compra en yenes y vínculo al hardware. Es búsqueda por nombre de cartucho/recopilación; el lector HTML depende de la estructura pública del sitio y tiene salida manual ante error.
- Gasto: JPY sin fracciones, otras monedas con precisión de centésimas, sumas independientes por moneda y separación juego/hardware. Fechas desconocidas no usan el año de lanzamiento. Cambiar un precio no duplica adquisiciones; borrar fichas conserva el registro. Restaurar conserva compras archivadas y permite retirarlas del historial.
- Migración real 5 → 6: conserva precios existentes, fecha conocida de hardware y fecha desconocida de juegos anteriores. Las migraciones previas siguen pasando. ZIP v5 conserva historia, yenes y fechas; v4 inicia historia con fichas existentes y las copias v1/v2 siguen convirtiendo notas una sola vez.
- Revisión visual de `docs/previews/render-tests/spending-by-system.png`: hardware de prueba 12.000 JPY en 2022 y videojuego 1.500 JPY en 2023, total 13.500 JPY en la misma unidad, sin mezclar otras monedas. La captura contiene datos sintéticos; no representa la colección del usuario.

Los resultados 0.6.1 que siguen se conservan como historial.

- Validación 0.6.1: APK e instrumentación compilados; Lint con 0 errores y las mismas 82 advertencias anteriores, sin baseline ni supresiones.
- Nueve pruebas Android relevantes correctas: personalización/exportación (2), editor de gamertag (1), copias/gamertags en los nueve temas (3) y recorrido de progreso, apodo y compartición (3). Las pruebas nuevas se corrigieron para devolver Unit a JUnit y desplazar la lista hasta elementos todavía no compuestos.
- Totales: el editor usa el número de copias de todos los juegos y las unidades del inventario de hardware, conservándolos al cambiar entre colección, juego y plataforma. Cambiar las cifras modifica la banda correspondiente en el PNG renderizado.
- Logotipos: seleccionar consola/compañía, recrear Activity, recordar orden, subir un elemento, eliminarlo y habilitar compartición cuando está lista la imagen. Preferencias persisten al crear otra instancia; configuración inválida o categorías futuras no impiden abrir el editor y el límite de seis se valida antes de guardar.
- Exportación: input con seis logotipos y totales serializado/restaurado, recreación de ViewModel con exportInput, escritura y lectura del PNG 1600 × 900. Los píxeles de una imagen transparente propia están presentes en la fila de logotipos; una imagen ausente genera una insignia sin bloquear el archivo. Se conserva el comportamiento del FileProvider y su permiso temporal de lectura.
- Revisión visual del artefacto real `docs/previews/render-tests/gamertag-customized.png`: totales, textos, emblemas y seis posiciones sin solapamiento. Incluye imágenes sintéticas para comprobar transparencias y el caso de imagen no disponible; no son datos de la colección del usuario.
- No cambian el esquema Room ni las copias portables. La selección de logotipos pertenece a las preferencias locales y no se añade al ZIP de juegos/sistemas.

Los resultados 0.6.0 que siguen se conservan como historial.

- Validación 0.6.0: APK, instrumentación Android y Room compilados; Lint con 0 errores y 82 advertencias, sin baseline ni supresiones. APK definitivo instalado y arranque en frío correcto en API 35.
- 58 pruebas distintas correctas: dominio (10), ZIP (10), proveedores HTTP (7), Room/migraciones (10) y Android (21). La batería inicial Android completó 20 casos; después de ajustar el espacio del buscador y añadir el recorrido ScanDex se completaron siete casos focalizados, incluido el nuevo caso con servicios reales.
- Inventario: creación desde formulario, caja, serie, fecha, precio exacto, mando especial y cantidad dos; recreación de Activity antes de guardar; lectura persistente, apertura, edición y borrado. Snapshot/restore transaccional conserva accesorios y fotos; inventarios con IDs duplicados se rechazan sin sustituir datos.
- Migración real 4 → 5 añade hardware y conserva juegos y notas sobre 100. Las migraciones anteriores y las pruebas de colección siguen pasando.
- Copias: v4 incluye fotos de sistemas y mandos. Recuperación real después de borrar la foto original: juego y sistema comparten correctamente la foto recuperada. V1/v2 convierten notas una vez; v3 mantiene 3/100 y se normaliza a v4 sin inventario. Rutas y referencias externas inválidas siguen rechazándose.
- Búsqueda: filtro Mega Drive encuentra Genesis; el selector permite elegir explícitamente Genesis entre varias plataformas. RAWG real confirma Sonic the Hedgehog (1991), ID 53551, con Genesis disponible. Se prueba además la selección de PC para Portal y la ficha con media y multimedia reales.
- ScanDex: contrato HTTP comprueba Authorization, ceros iniciales, ausencia de una clave RAWG en esa petición, 404, respuesta sin coincidencia, cuota y token ausente. Consulta real con token local y EAN público 0711719577966 devuelve Horizon Forbidden West: Complete Edition / PlayStation 5. El recorrido Compose consulta ScanDex, presenta candidatos RAWG y conserva vacío el formulario hasta que el usuario elige un juego. No se usan IDs IGDB para enriquecer datos.
- Preferencias: nueva instalación sin elección usa Cuadrícula; elecciones anteriores Lista/Cover Flow y selección explícita de Cuadrícula se conservan.
- Revisión visual de la ficha de hardware con emblema de Sega, ilustración genérica identificada, adquisición y mando especial. Captura de prueba en `docs/previews/render-tests/hardware-detail.png`. No representa datos del usuario.
- El lector óptico con Google Code Scanner está integrado y compilado; no se ha validado la captura física de un EAN mediante ese módulo en un teléfono real. La ruta de código escrito sí se ha probado de extremo a extremo con ScanDex y RAWG. La cámara CameraX existente sigue pasando su prueba independiente.

Los resultados 0.5.0 que siguen se conservan como historial.

- Validación 0.5.0: APK e instrumentación compilados; Lint con 0 errores y las mismas 82 advertencias anteriores, sin baseline ni supresiones.
- 24 pruebas distintas correctas: 18 de Android y 6 de red. La batería inicial de Android pasó 17 casos; el nuevo recorrido de cuadrícula y los dos de Cover Flow se repitieron correctamente tras ajustar el selector y el desplazamiento de la automatización.
- Cuadrícula: geometría real de tres columnas en vertical y cuatro en horizontal; persistencia al recrear Activity; apertura del detalle y regreso a Lista. El selector permite saltos de línea y no necesita desplazamiento horizontal.
- Preferencias antiguas: la vista anterior se recupera como Lista o Cover Flow; la nueva Cuadrícula persiste al crear otra instancia de preferencias.
- Media RAWG: respuesta 4,25/5 con 123 votos produce 85/100, independientemente de rating_top. Datos ausentes, no finitos, fuera de rango o sin votos no producen una falsa nota cero. Capturas y valoración mantienen estados independientes ante fallos.
- Consulta real desde Compose: autocompletado y ficha muestran la media RAWG con votos y conversión explícita a 0–100, además de sus capturas. Revisión visual de `compact-grid.png` y `community-rating.png` en `docs/previews/render-tests/`; son datos de prueba.
- La nota personal conserva su persistencia 0–100 y aparece separada de la externa, con bandas roja (<50), amarilla (50–74) y verde (≥75), tonos graduales y etiqueta de nivel. Room v4 y formato de copia v3 no cambian.

Los resultados 0.4.1 que siguen se conservan como historial.

- Validación 0.4.1: APK y paquetes de instrumentación compilados; Lint con 0 errores y las mismas 82 advertencias de la base anterior. Arranque en frío correcto en API 35 tras actualizar.
- 29 pruebas apropiadas al cambio, todas correctas: dominio (8), ZIP (9), Room/migración (8) y Android (4: recorrido de progreso/valoración y tres pruebas de copia/gamertags).
- Migración real 3 → 4: notas 0, 2,5, 4,5, 5 y null pasan a 0, 50, 90, 100 y null. Se conservan notas escritas, favoritos y progreso. Reabrir la base dos veces no reconvierte los valores; una nota nueva de 3/100 sigue siendo 3.
- Copias v1/v2 convierten la escala antigua una sola vez al formato v3; exportar y volver a importar conserva los valores. Una nota baja de una copia v3 no se interpreta como una nota antigua. Notas incompatibles con la escala de una copia antigua son rechazadas.
- Recorrido Compose: guardar 90/100, leerlo de Room y mostrar «★ 90 / 100» en la ficha, manteniendo favoritos, platino y porcentaje independientes.
- Consulta real RAWG verificada: la respuesta de detalles incluye rating, ratings_count y metacritic. Estas medias externas se han investigado; no se ha añadido su visualización en 0.4.1. Véase `docs/rawg-ratings.md`.

Los resultados 0.4.0 que siguen se conservan como historial.

- Validación 0.4.0: compilación APK, instrumentación y Lint correctos; 0 errores y 82 advertencias de Lint. No hay baseline ni supresiones.
- 41 pruebas distintas verificadas: dominio (8), RAWG con MockWebServer (4), ZIP (7), Room/migración (7) y Android (15). Las ejecuciones finales de Android cubren 14 casos de la batería general y la cámara en repetición focalizada después de corregir el desplazamiento de la automatización al visor anidado. No quedan fallos pendientes.
- Progreso: porcentajes fuera de rango rechazados, platino limitado a PS3/4/5, historia finalizada independiente del 100 % y de caja/manual/disco. Estadísticas con géneros múltiples sin contar dos veces un género repetido y colección vacía.
- Persistencia: migración real 2 → 3 conserva notas, precio y valoración; favorito, finalizado, porcentaje, platino e ID RAWG se escriben y leen desde Room. Copias v2 conservan los nuevos campos y se siguen leyendo manifiestos v1 sin ellos.
- Recorrido Compose nuevo: guardar PS5 con 87 %, finalizado, favorito, platino y nota 4,5/5; recrear Activity; filtrar favoritos por PS5; abrir listados desde estadísticas y los tres contadores. Apodo guardado desde Preferencias y utilizado al crear un gamertag nuevo.
- Compartir: PNG de 1600 × 900 leído desde la URI content:// de FileProvider; MIME, ClipData y permiso temporal de lectura verificados. Intentar compartir la base de datos mediante ese proveedor es rechazado. El selector y la publicación de WhatsApp/Instagram dependen de las apps instaladas y no se han automatizado.
- RAWG real: autocompletado, ID asociado, portada y caché sin red, ficha y capturas del endpoint de screenshots comprobados con la clave local. La prueba de red verifica también los enlaces HTTPS de vídeos y el caso en que fallan los tráileres pero siguen disponibles las capturas. No se garantiza disponibilidad de vídeo para todos los juegos ni planes de API; no se ha validado reproducción de un tráiler real en un dispositivo físico.
- Revisión visual: portada compacta sin hero decorativo, búsqueda tras los contadores, fondos de tema, textos legibles en tema oscuro, variantes del wordmark y gamertags. Las capturas están en `docs/previews/render-tests/`; representan datos de prueba.

Los resultados 0.3.0, 0.2.1 y 0.2.0 que siguen se conservan como historial.

- Validación 0.3.0: `:app:assembleDebug`, `:app:assembleDebugAndroidTest` y `:app:lintDebug` correctos. Lint: 0 errores, 78 advertencias (las 72 anteriores, cuatro sobre complejidad de trazados de iconos de plataforma y dos sugerencias KTX nuevas). No hay baseline ni supresiones.
- Ocho pruebas de Android completadas, 0 fallos: `CollectionV3FlowTest` (2), `CollectionV2FlowTest` (2), `DesignPreviewTest` (1) y `BackupAndGamertagTest` (3).
- Carrusel: navegación entre tres carátulas, apertura del detalle, recreación de Activity con vista persistente, filtro a una plataforma con un único resultado, controles de extremos desactivados, búsqueda vacía y regreso a cuadrícula. Los juegos y portadas de demostración se crean durante la prueba y se eliminan al terminar.
- Selector: búsqueda de consola, logotipo local visible y selección en el formulario. El formulario anterior y la persistencia del tema siguen pasando sus recorridos.
- Nueve temas capturados desde la app real; nueve gamertags PNG de 1600 × 900 renderizados y decodificados correctamente. Revisión visual de los cinco temas nuevos, carrusel y selector. Archivos en `docs/previews/render-tests/`.
- No hay cambio de esquema de base de datos ni de formato ZIP en 0.3.0. Las pruebas de dominio, red y Room enumeradas más abajo corresponden a 0.2.0 y se conservan como historial.

- Identidad 0.2.1: cinco pruebas específicas en emulador completadas, 0 fallos (`BrandPreviewTest`, `DesignPreviewTest` y `BackupAndGamertagTest`). Se exportaron los recursos Android reales y se revisaron el monograma a 48 px, las máscaras circular y redondeada, la capa monocroma, las cabeceras Cyberpunk/Kawaii y el logotipo en el PNG de gamertag. Capturas de los cuatro temas actualizadas.
- Cierre 0.2.1: `:app:assembleDebug` y `:app:lintDebug` correctos, 0 errores y las mismas 72 advertencias de la base 0.2.0. APK definitivo instalado y arranque en frío correcto en API 35 (2,8 s orientativos).
- Originales SVG y PNG de marca en `docs/branding/`; icono adaptativo en el lanzador y splash API 31+, logotipo con colores de tema en colección, menú y gamertags. El tamaño intrínseco de los vectores del logotipo se limita a 200 dp conservando su proporción y geometría.
- Los resultados de dominio, red, ZIP, Room y recorrido completo enumerados debajo corresponden a la validación 0.2.0; esos módulos no han cambiado con la identidad 0.2.1.

- Dominio: 6 pruebas, 0 fallos (valoración, edición, referencias, URLs HTTPS, importes exactos y contenido físico independiente).
- Red: 2 pruebas, 0 fallos (parseo RAWG, parámetros codificados, ausencia de clave sin llamada HTTP).
- Formato ZIP: 6 pruebas, 0 fallos (datos y fotos, rutas no válidas, referencias externas, versión futura, manifiesto excesivo e integridad referencial).
- Room en emulador: 6 pruebas, 0 fallos (operaciones de colección, logotipos compartidos, sustitución transaccional y migración 1 → 2 con datos existentes).
- Android/Compose: 9 pruebas, 0 fallos (formulario anterior, cámara, RAWG real y caché sin red; edición coleccionista, componentes, estado, precio y género; menú, tema persistente; recuperación portable con foto original borrada; PNG en cuatro temas, exportación recuperada desde SavedStateHandle y capturas visuales).
- Compilación `:app:assembleDebug`: correcta. APK en `app/build/outputs/apk/debug/app-debug.apk`.
- Esquemas Room v1 y v2 generados/versionados en `core/database/schemas/com.gamerdex.core.database.GamerDexDatabase/`.
- Arranque en frío independiente de las pruebas: correcto, sin cierre inesperado.
- Validación final tras los ajustes visuales: `:app:assembleDebug`, `:app:lintDebug` y `:app:connectedDebugAndroidTest` completados correctamente; el recorrido Compose volvió a pasar.
- Validación 0.2.0: 29 pruebas en total, 0 fallos, 0 omitidas. Compilación, Lint y pruebas en emulador completados correctamente.
- Caché de imágenes comprobada con una imagen real de RAWG: carga inicial correcta y segunda carga correcta desde `DataSource.DISK`, con acceso de red y caché de memoria desactivados para la petición. La comprobación está integrada en la prueba real RAWG.
- Lint: 0 errores y 72 advertencias. Son 60 avisos de dependencias, 6 de AGP, 1 del target SDK fijado en 35 y 5 sugerencias de extensiones KTX. No hay baseline ni supresiones.
- Capturas reales de los cuatro temas y ejemplos de gamertags: `docs/previews/render-tests/`. Los PNG de ejemplo sin portada usan datos de prueba, no una colección real.
- Ejemplo de gamertag con imagen real de RAWG y renderizado verificado: `docs/previews/render-tests/gamertag-live-CYBERPUNK.png`.

Primera medición orientativa del emulador: arranque en frío 2,6 s y PSS aproximado 75 MB en la Bóveda vacía. Es una compilación debug, con emulador y herramientas de prueba; no representa rendimiento de producción ni constituye un benchmark.

La revisión visual inicial detectó iconos oscuros de sistema sobre el tema OLED. Se ajustó explícitamente el estilo de las barras para usar iconos claros. También se ajustaron los colores seleccionados de Material 3, se eliminó el boxing del estado de valoración y se añadieron reglas de extracción de datos y el icono monocromático.

## Límites de las comprobaciones

RAWG se ha probado con MockWebServer, una consulta HTTP real y el recorrido de autocompletado desde Compose usando la clave local. La prueba CameraX captura con la cámara virtual, guarda el juego, recrea la Activity y abre la foto en el visor. Se ha probado una migración real v1 → v2 y la recuperación de una copia con sus fotos después de borrar la imagen original. No se ha automatizado la interacción con todos los proveedores del selector de documentos Android, PhotoPicker, la revocación de permisos de fotos, el gesto de zoom ni todos los tamaños y escalas tipográficas. Cámara y rendimiento en hardware físico quedan pendientes.

Las dependencias y el target SDK están fijados, no actualizados automáticamente a los últimos disponibles. Las advertencias de Lint sobre versiones más recientes se conservan para una actualización coordinada y comprobada; no se han ocultado mediante una baseline.
