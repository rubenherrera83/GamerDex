# Navegación por sistemas

La barra inferior ofrece **Sistemas**, **Juegos**, **Estadísticas** y **Más**. Más abre el menú con los accesos secundarios y los atajos anteriores. Desde 0.11.0, Sistemas reúne la colección y las unidades físicas. Cada plataforma tiene las pestañas **Juegos** y **Mis equipos**; ya no hay una entrada independiente «Mis sistemas».

## Tarjetas y recuentos

Sistemas reúne las plataformas presentes en videojuegos y hardware. Una plataforma aparece aunque solo tenga juegos o solo hardware. Cada tarjeta muestra el número de copias de videojuegos y, por separado, las unidades físicas. Dos copias del mismo título cuentan como dos videojuegos; dos consolas no duplican sus juegos. No se exige vincular cada juego a una unidad física para que aparezca en su plataforma.

Se usa el catálogo de familias existente: por ejemplo Mega Drive, Mega Drive II y Genesis comparten grupo; SNES y Super Famicom también. PC, Windows y Steam Deck usan la familia PC. Switch y Switch 2 siguen separados, igual que Game Boy y Game Boy Color. Los nombres personalizados se comparan sin distinguir mayúsculas. Esto no renombra los datos ni cambia los criterios del historial de gasto.

La miniatura procede de una unidad registrada, dando prioridad a las que tienen foto o URL de imagen. Cuando no hay imagen propia se presenta el logotipo, sin ilustración grande en portada. El logotipo usa la personalización guardada o el catálogo de marcas.

## Colección de una plataforma

Tocar una tarjeta abre Juegos. El encabezado fijo muestra el total de copias y equipos de esa familia, incluso al buscar un título o una unidad. Se conservan las vistas Lista, Cuadrícula y Cover Flow, los favoritos y el acceso a la ficha. Añadir un juego preselecciona la plataforma de una unidad del grupo o, si no existe, la de uno de sus juegos; se puede editar antes de guardar.

Mis equipos muestra exclusivamente las unidades de esa familia. Mantiene fotos, caja/manual/cables, funcionamiento, estado, serie, precio/fecha, accesorios y componentes, curiosidades/ranking y acceso al gamertag. Se puede buscar por nombre, plataforma o serie. La portada también encuentra la familia al buscar el nombre o serie de cualquiera de sus equipos.

Añadir equipo desde una plataforma preselecciona su plataforma, que sigue siendo editable. Desde la portada permite elegir cualquier sistema; tras guardar abre Mis equipos de la familia creada. Si se elige otra plataforma durante el alta, se abre su familia de destino. Editar o eliminar una unidad conserva la separación entre inventario, juegos e historial de compras.

Cada pestaña conserva búsqueda y desplazamiento al alternar, y la pestaña seleccionada se restaura al recrear Activity. Las fichas y formularios regresan a su pestaña de origen.

Desde 0.14.0, las fichas de juegos y equipos agrupan los datos en secciones abiertas y ofrecen accesos rápidos en su barra superior. Volver/Editar permanecen accesibles; fotos ampliables y confirmación de borrado se conservan al recrear Activity. [Uso de las fichas](ux-third-block.md).

Desde 0.15.0, Estadísticas da acceso directo a Gasto en pantalla completa. Volver regresa a los contadores; reabrir Gasto recupera filtros y desgloses. Alternar secciones principales conserva la última consulta abierta en Estadísticas. [Consulta financiera y gráficos](ux-statistics.md).

Volver recupera el listado de sistemas y su búsqueda. **Juegos** en el menú antiguo y **Todos los juegos** en la portada abren la colección global y limpian búsqueda y filtros. **Juegos** en la barra inferior recupera el estado de la colección. El contador **Equipos** de la colección global conserva el total de unidades físicas y abre la portada unificada. [Filtros, ordenación y restauración de estado](ux-second-block.md).

## Pantalla inicial

En Preferencias → Pantalla inicial se elige Sistemas o Todos los juegos. Desde 0.16.0, instalaciones nuevas y anteriores adoptan Sistemas como inicio mediante una migración local de una sola vez. Antes se guardaba Juegos automáticamente sin distinguir una elección explícita. Tras la migración se conserva cualquier elección posterior, también Todos los juegos. La vista elegida se mantiene; el selector muestra Cuadrícula / Lista / Cover Flow.

La preferencia se utiliza en sesiones nuevas. Cambiarla no abandona la pantalla actual; una recreación de Activity o la recuperación de una sesión conserva el destino abierto. Las preferencias locales no forman parte del ZIP de la colección. Room v6 y el formato portable v5 permanecen iguales.

Todas las pantallas comparten una cabecera compacta y fija, con logotipo del tema, título y navegación coherentes. [Cabeceras y preferencias](ux-preferences-and-headers.md).

Desde 0.17.0, entrar, salir y volver entre destinos no utiliza fundidos. Gamertags mantiene su editor por secciones, vista previa ampliable y acciones fijas. [Uso del editor](ux-gamertags.md).

Desde 0.18.0, la navegación inferior reorganiza sus etiquetas si no caben con el tamaño de letra elegido y utiliza una presentación compacta en ventanas bajas. El menú lateral es desplazable; no cambia el destino ni la restauración de estado. [Revisión adaptativa](ux-adaptive-review.md).

Desde 0.19.0, Sistemas y Mis equipos permiten Tarjetas / Lista / Cuadrícula y guardan esa preferencia. Juegos ofrece Cuadrícula / Lista compacta / Tarjetas / Cover Flow. Toda consulta nueva de juegos comienza en Título A–Z; Todos los juegos también restablece ese orden. Volver a una consulta conserva cualquier orden elegido explícitamente. [Vistas y ordenación](collection-views.md).

Desde 0.19.1, todas las distribuciones de la barra inferior muestran icono y nombre, incluido el modo para letra ampliada. En ventanas altas se apilan; en ventanas bajas comparten fila para conservar la altura del contenido. Se mantienen la selección, los destinos y las áreas táctiles de al menos 48 dp.
