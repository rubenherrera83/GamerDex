# Gamertags · 0.6.1

Las tarjetas de colección, plataforma y juego muestran siempre los totales de toda la colección: número de copias de videojuegos, incluidas las ediciones repetidas, y número de unidades de hardware guardadas en Mis sistemas. Dos consolas del mismo modelo cuentan como dos sistemas. El número de plataformas distintas sigue siendo un dato diferente en la tarjeta de colección.

«Personalizar logotipos» permite combinar hasta seis elementos. Consolas ofrece todo el catálogo y los sistemas personalizados de la colección. Compañías utiliza los nombres/logotipos guardados y los emblemas locales disponibles. Juegos ofrece las carátulas de la colección. Para un logotipo específico de un juego o cualquier otra marca, Personalizado permite elegir una imagen desde Android o indicar una URL HTTPS; se recomienda PNG transparente. No se intenta obtener un logotipo de juego a partir de RAWG, que aquí aporta carátulas.

Cada elemento puede quitarse o moverse con las flechas de subir/bajar. La selección y su orden se guardan como preferencias locales para próximas sesiones. Los permisos persistentes de las imágenes elegidas se solicitan al proveedor Android. Si se borra el original, se revoca el permiso o falla una URL, el renderer utiliza el emblema local correspondiente o una insignia de texto para que el resto de la tarjeta siga disponible.

El renderer conserva las proporciones y colores de las imágenes personalizadas y tiñe los emblemas vectoriales locales con el tema. Hasta seis cargas de logotipos se ejecutan de forma concurrente y limitada, con ocho segundos de espera máxima por recurso. La vista previa, el PNG guardado y el PNG compartido utilizan el mismo renderer de 1600 × 900.

La solicitud de exportación conserva los totales y los logotipos en SavedStateHandle, de modo que puede volver a generarse si se recrea el ViewModel mientras está abierto el selector de documentos. La compartición mantiene el FileProvider y sus permisos temporales de lectura.

No hay cambio de esquema Room ni de ZIP. Como el apodo, el tema y la vista elegida, los logotipos del gamertag son preferencias de la instalación y no se incluyen en la copia portable de juegos/sistemas. Las URLs remotas se consultan para descargar las imágenes elegidas; no se suben el apodo, el inventario ni los PNG generados a un servicio de composición.
