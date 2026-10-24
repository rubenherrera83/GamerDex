# Tercera mejora UX · 0.14.0

## Lectura y navegación de las fichas

Las fichas de videojuegos y equipos presentan un resumen con imagen, nombre y plataforma. En los juegos se mantienen la compañía, su logotipo y la estrella de favorito; en los equipos se muestran tipo, región y cantidad de accesorios/componentes. La imagen puede ampliarse cuando existe; una ilustración genérica de hardware no se presenta como fotografía real.

Volver y Editar permanecen en la barra superior. Debajo, una fila de accesos desplazable permite saltar a cada sección. El salto coloca la sección cerca de la parte superior y respeta el límite de desplazamiento de la pantalla. Todas las secciones permanecen abiertas: no hay que descubrir datos dentro de nuevos desplegables. Se conserva el desplazamiento al recrear Activity o volver de editar.

Crear gamertag aparece junto al resumen. Eliminar continúa al final de la ficha y requiere confirmación. Tanto la confirmación como la foto abierta se restauran al recrear Activity; Cancelar no modifica los datos. La barra inferior sigue oculta en fichas y formularios.

## Videojuegos

- **Tu partida**: nota personal coloreada 0–100, media de la comunidad RAWG con votos/estados de consulta, finalizado, porcentaje y platino. Una barra muestra el porcentaje sin confundirlo con el contenido físico de la copia.
- **Tu copia**: edición, detalles especiales, contenido completo o faltante y estado físico. Caja, instrucciones y soporte de juego se distinguen con etiquetas de presencia/ausencia. Las copias digitales indican que no tienen componentes físicos.
- **Compra**: precio con su moneda, fecha y equipo asociado cuando se guardó un vínculo. Cero sigue siendo un importe válido; un dato desconocido se indica expresamente. Un equipo no disponible conserva la referencia guardada, sin inventar su nombre.
- **Datos del juego**: año, géneros, catálogo oficial Evercade y créditos/enlaces RAWG cuando corresponden. La compañía «Sin especificar» se presenta como no indicada, sin cambiar el valor persistido.
- **Notas**: texto completo o ayuda para añadirlo desde Editar.
- **Fotos y multimedia**: capturas/vídeos RAWG y galería de la propia copia, con paginación y ampliación. Los errores y reintentos de valoración y multimedia siguen siendo independientes. La ayuda para vincular RAWG usa el nombre actual del botón Buscar por nombre.

## Equipos

Estado y contenido reúne condición, funcionamiento, caja, instrucciones, cables y serie. Compra presenta fecha/precio por separado. Componentes y accesorios muestra modelos y cantidades, condición, serie y notas de cada pieza; también mantiene las unidades sin componentes. Notas, fotos e historia tienen sus propios accesos.

La galería incluye todas las fotos de la unidad, también la utilizada en la cabecera, y permite ampliar con el mismo visor de los juegos. Si no hay fotos locales se muestra la URL de imagen cuando existe. Las entradas repetidas de una misma imagen se pueden recorrer sin usar la URI como identificador único de página. El visor permite pellizcar entre 1× y 4× y desplazar la imagen ampliada.

Historia y comunidad mantiene curiosidades, unidades vendidas, fuentes, fecha de revisión y top RAWG. Se sigue explicando que estos datos pertenecen a la familia de plataforma y que las notas no son exclusivas de una versión. No cambian consultas, mínimos de votos ni caché.

## Compatibilidad

El diseño utiliza las paletas, tipografías, formas y fondos existentes. Se revisan capturas reales en Cyberpunk y Kawaii. El icono y los temas se mantienen. Room v6, ZIP v5, fotos originales, historial de gasto y formularios permanecen compatibles. Los accesos Añadir notas/fotos abren el editor actual con su validación y protección de cambios pendientes. No se añaden campos persistidos ni migraciones.

Este bloque se limita a las fichas. El rediseño específico de estadísticas, preferencias y del editor de gamertags queda para entregas posteriores.
