# Búsqueda y gasto · 0.7.1

## ScanDex

Comprobación real del 18 de octubre de 2026: el token local obtiene HTTP 200 y una coincidencia con el código de prueba 0711719577966. El valor proporcionado 502655067591 tiene 12 cifras y un dígito de control incorrecto. No se completan ni corrigen códigos automáticamente. El EAN 5026555067591, publicado para Civilization VI de Switch, también devuelve HTTP 404 en ScanDex; esa edición no está en su catálogo en la comprobación, aunque la credencial funciona. Referencias: [producto y EAN](https://videodis.es/switch/11057-civilization-vi-switch.html), [contrato ScanDex](https://scandex.gamery.app/documentation/api/).

El formulario permite escanear o escribir sin abrir antes RAWG. Se conserva el acceso desde el buscador anterior. La validación distingue código incompleto/checksum, 404 desconocido, 200 conocido sin juego asociado, respuesta sin datos de identificación, 401/403 de autorización y 429 de cuota. No se confunde falta de cobertura con clave inválida. Cuando no hay datos se puede buscar por título en RAWG o registrar la copia manualmente. La cámara usa Google Code Scanner; la escritura conserva ceros iniciales.

## UPCitemdb como respaldo (0.7.1)

El 18 de octubre de 2026 se consultó el endpoint gratuito documentado de [UPCitemdb](https://www.upcitemdb.com/wp/docs/main/development/getting-started/) con EAN 5026555067591: HTTP 200, un resultado «Sid Meier's Civilization Vi Switch (sp)». La categoría recibida es genérica/inexacta (`Electronics > Video Game Consoles`); conviene confirmar título y plataforma con el usuario y enriquecer la ficha mediante RAWG. Esta comprobación puntual no demuestra superioridad global de cobertura. Su [plan gratuito](https://www.upcitemdb.com/wp/docs/main/development/plan/) no exige cuenta ni clave y limita a 100 solicitudes combinadas al día, con límites adicionales de frecuencia.

La app consulta primero ScanDex. Ante 404, respuesta sin juego, token ausente/rechazado, cuota o error de red, consulta el endpoint gratuito de UPCitemdb con el mismo código, sin token ni credenciales RAWG/ScanDex. La cancelación del usuario se propaga y no inicia otra consulta. Si ScanDex identifica el juego no se llama al respaldo.

Se muestra la fuente real y, si hubo un fallo de ScanDex, su aviso junto a la identificación alternativa. Si ambos catálogos carecen del código se indica ausencia de coincidencias; si falla un servicio se explica el fallo, sin presentarlo como falta de cobertura. Se conservan la búsqueda por título y el formulario manual.

Solo se admite un producto cuyo EAN/UPC/GTIN coincida con el solicitado (se permite el relleno normalizado con ceros). Otro código, resultados ambiguos o un título vacío producen un error. El título original se muestra sin alterarlo. Para consultar RAWG se retiran únicamente un sufijo regional reconocido y un nombre de plataforma al final; si no hay plataforma reconocible queda por confirmar. No se usan categorías comerciales para inferir sistema ni se asigna automáticamente una versión RAWG. No se importan precios de tiendas, valoración ni datos personales de la compra.

Una instancia única por proceso serializa consultas e impone diez segundos entre peticiones. Caché en memoria: coincidencias quince minutos, ausencias cinco minutos, máximo 500 códigos. No se reintentan automáticamente peticiones fallidas. Ante HTTP 429 o cabecera de cuota restante cero se respeta el reset del servidor (entre diez segundos y 24 horas); sin reset válido se pausa quince minutos. Las respuestas previamente cacheadas siguen disponibles durante esa pausa. La cuota gratuita depende principalmente de la IP, por lo que otras apps o dispositivos de la misma red pueden consumirla y reiniciar la app no restablece la cuota del proveedor. [Límites oficiales](https://www.upcitemdb.com/wp/docs/main/development/api-rate-limits/).

[PriceCharting](https://www.pricecharting.com/api-documentation) también documenta consulta por UPC y título/consola. Requiere suscripción; su licencia estándar es de uso interno y ofrecer datos de precios en una app para terceros requiere acuerdo comercial. [Barcode Lookup](https://www.barcodelookup.com/api) ofrece búsquedas UPC/EAN genéricas mediante clave y planes de pago. Estas dos fuentes no se han integrado.

## Evercade

El buscador utiliza [el catálogo oficial de cartuchos](https://evercade.co.uk/cartridges/), sin requerir una clave RAWG. Busca nombres de cartuchos/recopilaciones, no todos los títulos contenidos globalmente. Al elegir uno consulta su ficha y añade sus juegos incluidos a las notas. Se registra un cartucho como una copia Evercade, con editorial Blaze Entertainment, imagen y enlace a la fuente; no se inventa año de lanzamiento ni un ID RAWG. El formulario permite corregir los campos importados.

No se ha localizado un endpoint público específico de cartuchos en el índice REST de esa web; esta integración lee HTML público con selectores concretos, no una API oficial garantizada. La página del índice se reutiliza en memoria durante diez minutos y solo se solicita la ficha del cartucho elegido. Si cambia el sitio, está inaccesible o no se interpreta el catálogo, se muestra un error y el enlace oficial para continuar manualmente. No se ejecutan scripts del HTML. Se limitan tamaño, número de resultados y URLs de fichas al dominio y ruta oficiales. Los clientes de Evercade, ScanDex y RAWG son independientes y no comparten credenciales.

## Monedas, fechas y compras

EUR, USD y GBP mantienen hasta dos decimales. JPY acepta importes enteros; 1500 JPY se presenta sin fracción y nunca como 15 JPY. Para conservar compatibilidad con los campos existentes, el entero almacenado representa centésimas también en JPY: 1500 yenes = 150000 unidades internas. Se validan múltiplos de cien en JPY; el usuario siempre escribe y ve yenes, no unidades internas. Las sumas utilizan enteros y se agrupan por moneda, sin tipos de cambio ni un total ficticio mezclando EUR y JPY.

La fecha de compra es independiente del año de lanzamiento y de la fecha de alta. Si se desconoce se muestra Año desconocido. Cambiar un precio, moneda, fecha o vinculación corrige la compra de esa copia: no crea una segunda adquisición. Una nueva copia tiene su propia compra. Se incluyen videojuegos y hardware; los accesorios comparten el precio global de la ficha de sistema, sin inventar importes individuales ni duplicarlos por cantidad de mandos.

El gasto por plataforma agrupa nombres de plataforma tal como están registrados. El gasto por unidad usa el vínculo opcional de cada juego a un equipo registrado (Sistemas → plataforma → Mis equipos) e incluye el precio del propio hardware. Los juegos sin vínculo se agrupan por separado; no se asignan automáticamente a todas las consolas de una misma plataforma. Las unidades repetidas se distinguen por serie o ID cuando sus nombres coinciden. Los sistemas retirados conservan un grupo archivado.

El registro de compras se conserva al borrar un juego o un sistema. La pantalla Archivadas permite retirar una compra de una ficha eliminada con confirmación; no permite borrar desde ahí una compra con ficha activa. El histórico solo puede incluir lo que se registró: no reconstruye compras de fichas eliminadas antes de esta actualización ni estima importes ausentes. Para corregir una compra activa se edita su ficha.

## Migración y copias

Room 5 → 6 añade purchasedOn, ownedSystemId y catalogUrl en games y crea purchases. Inicializa una compra por ficha existente, conservando precio y moneda; las fechas conocidas de hardware se mantienen y las de juegos anteriores quedan desconocidas. No usa el año de lanzamiento ni createdAt como fecha de compra. Guardar ficha y compra se hace en la misma transacción; el historial no tiene borrado en cascada con el inventario.

Las copias portables v5 incluyen las compras archivadas y las fechas/vínculos. Se siguen leyendo v1–v4 y se inicializa su historial con las fichas presentes; no contienen compras de fichas que ya no estaban en esas copias. La confirmación advierte que restaurar sustituye la colección y su historial. Los IDs de unidades archivadas se conservan como referencia histórica aunque ya no exista el hardware. La lógica de conversión antigua de notas 0–5 no cambia.
