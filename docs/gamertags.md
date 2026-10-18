# Gamertags · 0.9.0

## PCs y sistemas

En Mis sistemas, registra el PC como PC / ordenador y añade CPU, GPU, RAM, almacenamiento, placa, fuente, refrigeración y periféricos mediante Componentes y accesorios. Se reutilizan los accesorios existentes, sus cantidades y notas; no se añade un esquema independiente ni se convierten automáticamente los nombres de accesorios antiguos.

«Crear gamertag de este sistema» abre su tarjeta directamente. También se puede entrar desde Gamertags → Sistema / PC y elegir una unidad del inventario. El selector busca por nombre y plataforma y distingue unidades con su ID. Funciona igualmente con consolas y microordenadores. Si no hay inventario, indica que hay que registrar el sistema; no permite exportar un objetivo inexistente.

Por defecto se incluyen los primeros diez elementos de la ficha. Se pueden desmarcar y elegir otros; el límite es de diez elementos, con sus cantidades, no diez unidades físicas. Un par de módulos RAM puede figurar como «2 × RAM DDR5 16 GB». La selección y la unidad se conservan al recrear Activity. Las notas de componentes solo se incorporan al activar su opción. No se copian los campos de serie de sistema/componente, precio, fecha de compra ni notas generales del sistema.

El PNG mantiene 1600 × 900, totales globales y hasta seis logotipos. El nombre del equipo ocupa hasta dos líneas y los componentes usan dos columnas de cinco filas, con hasta dos líneas por elemento. Los textos largos terminan en puntos suspensivos; el editor avisa y permite revisar la vista previa. Se utiliza la primera foto local, o la imagen remota si no hay foto local, ajustando sus proporciones al marco decorativo. Sin foto hay fondo geométrico. Se puede generar también una tarjeta sin accesorios.

Guardar y compartir utilizan el mismo bitmap. Los botones esperan a que la imagen corresponda a la selección actual, evitando exportar la anterior mientras se regenera. La solicitud de exportación conserva los componentes en SavedStateHandle para recrear el PNG si se pierde el ViewModel durante el selector de documentos. Las notas activadas y el contenido de la foto forman parte de la imagen que se comparte.

## Temas y personalización

«Escritorio Aurora» se inspira en PC/Windows con cintas azules/lavanda y un wordmark ligero y redondeado. «Órbita Verde» se inspira en Xbox con verdes, trazos orbitales y letras finas. Ambos se eligen en Preferencias, se aplican a toda la app y a los gamertags y conservan el icono existente. Los motivos son geométricos propios; no se incorporan fondos ni logotipos oficiales de Windows o Xbox al tema.

Las tarjetas de colección, plataforma, juego y sistema/PC muestran siempre los totales de toda la colección: número de copias de videojuegos, incluidas las ediciones repetidas, y número de unidades de hardware guardadas en Mis sistemas. Dos consolas del mismo modelo cuentan como dos sistemas. El número de plataformas distintas sigue siendo un dato diferente en la tarjeta de colección.

«Personalizar logotipos» permite combinar hasta seis elementos. Consolas ofrece todo el catálogo y los sistemas personalizados de la colección. Compañías utiliza los nombres/logotipos guardados y los emblemas locales disponibles. Juegos ofrece las carátulas de la colección. Para un logotipo específico de un juego o cualquier otra marca, Personalizado permite elegir una imagen desde Android o indicar una URL HTTPS; se recomienda PNG transparente. No se intenta obtener un logotipo de juego a partir de RAWG, que aquí aporta carátulas.

Cada elemento puede quitarse o moverse con las flechas de subir/bajar. La selección y su orden se guardan como preferencias locales para próximas sesiones. Los permisos persistentes de las imágenes elegidas se solicitan al proveedor Android. Si se borra el original, se revoca el permiso o falla una URL, el renderer utiliza el emblema local correspondiente o una insignia de texto para que el resto de la tarjeta siga disponible.

El renderer conserva las proporciones y colores de las imágenes personalizadas y tiñe los emblemas vectoriales locales con el tema. Hasta seis cargas de logotipos se ejecutan de forma concurrente y limitada, con ocho segundos de espera máxima por recurso. La vista previa, el PNG guardado y el PNG compartido utilizan el mismo renderer de 1600 × 900.

La solicitud de exportación conserva los totales y los logotipos en SavedStateHandle, de modo que puede volver a generarse si se recrea el ViewModel mientras está abierto el selector de documentos. La compartición mantiene el FileProvider y sus permisos temporales de lectura.

No hay cambio de esquema Room ni de ZIP. Como el apodo, el tema y la vista elegida, los logotipos del gamertag son preferencias de la instalación y no se incluyen en la copia portable de juegos/sistemas. Las URLs remotas se consultan para descargar las imágenes elegidas; no se suben el apodo, el inventario ni los PNG generados a un servicio de composición.
