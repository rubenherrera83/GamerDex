# Primera mejora UX · 0.12.0

Esta entrega aplica el primer bloque de la revisión: portada, alta de juegos y protección de formularios. La navegación inferior, los nuevos filtros/ordenación, el rediseño general de fichas, estadísticas, preferencias y gamertags quedan para entregas posteriores.

## Sistemas

La portada reduce cabecera y tarjetas para mostrar varias plataformas en un teléfono. El logotipo identifica la plataforma; si un equipo tiene foto se presenta como miniatura junto a su marca. Las imágenes grandes siguen en las fichas de equipos. No se sustituyen fotografías ni se eliminan datos.

Se conserva el total de juegos/copias y unidades de cada familia, además de las pestañas Juegos y Mis equipos. La búsqueda encuentra plataformas, nombres de equipos y números de serie. Los temas existentes siguen definiendo colores, formas y fondos.

## Alta de juegos

Buscar por nombre y escanear aparecen directamente. Más formas de identificar reúne entrada manual de EAN/UPC y cartuchos Evercade. Se mantienen RAWG, ScanDex y UPCitemdb, incluida la confirmación de la versión antes de importar.

Título y plataforma son obligatorios. Si no se conoce la compañía, se deja vacía y se guarda el marcador explícito «Sin especificar»; no se atribuye una compañía real. El marcador mantiene compatibles el dominio, Room y las copias anteriores. Al editar ese valor, el campo se presenta vacío. No se cambian las compañías ya registradas.

Las secciones Edición y estado, Compra, Más datos, Partida y valoración y Fotos y notas comienzan cerradas al añadir. Al editar un juego existente comienzan abiertas. Los resúmenes de edición y compra muestran los valores actuales, incluidos los contenidos físicos predeterminados que ya usaba la app. Cerrar una sección no descarta ni omite sus valores al guardar.

Guardar está fijo. Si falta un dato o hay un año, URL, importe, porcentaje, nota o fecha inválidos, se explica dónde corregirlo. La fecha permite calendario o escritura ISO y puede quedar desconocida. La nota permite deslizador o entrada numérica, incluida coma decimal; rechaza valores fuera de 0–100.

## Cambios pendientes

Juegos y equipos comparan los campos editables con su valor inicial. Volver por la barra o por el sistema, con cambios pendientes, ofrece Guardar cambios, Seguir editando y Descartar cambios. Guardar solo está disponible si el formulario es válido; Descartar siempre permite salir. Un formulario sin cambios sale directamente. El cajón lateral no se abre por gesto mientras se edita.

La comparación no depende del estado abierto/cerrado de una sección y sigue funcionando tras recrear Activity. Incluye fotos, metadatos importados, componentes y accesorios. Descartar conserva los datos previamente guardados. Las fotos originales de la galería no se borran; las capturas temporales no guardadas siguen usando la limpieza anterior.

Se mantienen los mecanismos existentes de restauración del estado de pantalla. Esto no incorpora un archivo de borrador permanente ni autoguardado en la colección: hay que confirmar Guardar para persistir los cambios.

Room v6, ZIP v5, el historial de compras y las preferencias no cambian.
