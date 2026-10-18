# Búsqueda y gasto · 0.7.0

## ScanDex

Comprobación real del 18 de octubre de 2026: el token local obtiene HTTP 200 y una coincidencia con el código de prueba 0711719577966. El valor proporcionado 502655067591 tiene 12 cifras y un dígito de control incorrecto. No se completan ni corrigen códigos automáticamente. El EAN 5026555067591, publicado para Civilization VI de Switch, también devuelve HTTP 404 en ScanDex; esa edición no está en su catálogo en la comprobación, aunque la credencial funciona. Referencias: [producto y EAN](https://videodis.es/switch/11057-civilization-vi-switch.html), [contrato ScanDex](https://scandex.gamery.app/documentation/api/).

El formulario permite escanear o escribir sin abrir antes RAWG. Se conserva el acceso desde el buscador anterior. La validación distingue código incompleto/checksum, 404 desconocido, 200 conocido sin juego asociado, respuesta sin datos de identificación, 401/403 de autorización y 429 de cuota. No se confunde falta de cobertura con clave inválida. Cuando no hay datos se puede buscar por título en RAWG o registrar la copia manualmente. La cámara usa Google Code Scanner; la escritura conserva ceros iniciales.

## Evercade

El buscador utiliza [el catálogo oficial de cartuchos](https://evercade.co.uk/cartridges/), sin requerir una clave RAWG. Busca nombres de cartuchos/recopilaciones, no todos los títulos contenidos globalmente. Al elegir uno consulta su ficha y añade sus juegos incluidos a las notas. Se registra un cartucho como una copia Evercade, con editorial Blaze Entertainment, imagen y enlace a la fuente; no se inventa año de lanzamiento ni un ID RAWG. El formulario permite corregir los campos importados.

No se ha localizado un endpoint público específico de cartuchos en el índice REST de esa web; esta integración lee HTML público con selectores concretos, no una API oficial garantizada. La página del índice se reutiliza en memoria durante diez minutos y solo se solicita la ficha del cartucho elegido. Si cambia el sitio, está inaccesible o no se interpreta el catálogo, se muestra un error y el enlace oficial para continuar manualmente. No se ejecutan scripts del HTML. Se limitan tamaño, número de resultados y URLs de fichas al dominio y ruta oficiales. Los clientes de Evercade, ScanDex y RAWG son independientes y no comparten credenciales.

## Monedas, fechas y compras

EUR, USD y GBP mantienen hasta dos decimales. JPY acepta importes enteros; 1500 JPY se presenta sin fracción y nunca como 15 JPY. Para conservar compatibilidad con los campos existentes, el entero almacenado representa centésimas también en JPY: 1500 yenes = 150000 unidades internas. Se validan múltiplos de cien en JPY; el usuario siempre escribe y ve yenes, no unidades internas. Las sumas utilizan enteros y se agrupan por moneda, sin tipos de cambio ni un total ficticio mezclando EUR y JPY.

La fecha de compra es independiente del año de lanzamiento y de la fecha de alta. Si se desconoce se muestra Año desconocido. Cambiar un precio, moneda, fecha o vinculación corrige la compra de esa copia: no crea una segunda adquisición. Una nueva copia tiene su propia compra. Se incluyen videojuegos y hardware; los accesorios comparten el precio global de la ficha de sistema, sin inventar importes individuales ni duplicarlos por cantidad de mandos.

El gasto por plataforma agrupa nombres de plataforma tal como están registrados. El gasto por unidad usa el vínculo opcional de cada juego a Mis sistemas e incluye el precio del propio hardware. Los juegos sin vínculo se agrupan por separado; no se asignan automáticamente a todas las consolas de una misma plataforma. Las unidades repetidas se distinguen por serie o ID cuando sus nombres coinciden. Los sistemas retirados conservan un grupo archivado.

El registro de compras se conserva al borrar un juego o un sistema. La pantalla Archivadas permite retirar una compra de una ficha eliminada con confirmación; no permite borrar desde ahí una compra con ficha activa. El histórico solo puede incluir lo que se registró: no reconstruye compras de fichas eliminadas antes de esta actualización ni estima importes ausentes. Para corregir una compra activa se edita su ficha.

## Migración y copias

Room 5 → 6 añade purchasedOn, ownedSystemId y catalogUrl en games y crea purchases. Inicializa una compra por ficha existente, conservando precio y moneda; las fechas conocidas de hardware se mantienen y las de juegos anteriores quedan desconocidas. No usa el año de lanzamiento ni createdAt como fecha de compra. Guardar ficha y compra se hace en la misma transacción; el historial no tiene borrado en cascada con el inventario.

Las copias portables v5 incluyen las compras archivadas y las fechas/vínculos. Se siguen leyendo v1–v4 y se inicializa su historial con las fichas presentes; no contienen compras de fichas que ya no estaban en esas copias. La confirmación advierte que restaurar sustituye la colección y su historial. Los IDs de unidades archivadas se conservan como referencia histórica aunque ya no exista el hardware. La lógica de conversión antigua de notas 0–5 no cambia.
