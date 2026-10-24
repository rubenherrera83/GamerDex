# Segunda mejora UX · 0.13.0

## Navegación

La barra inferior da acceso a Sistemas, Juegos, Estadísticas y Más. Más abre el menú con Favoritos, Géneros, Gamertags y Preferencias; también conserva los accesos anteriores a las secciones principales. Los botones de menú existentes siguen funcionando. Cambiar de sección recupera su estado en vez de abrir otra copia del destino.

La barra se oculta en fichas y formularios. Los formularios mantienen su confirmación de cambios pendientes; no se pueden abandonar mediante la barra inferior. Cambiar de sección, abrir el menú o una ficha cierra el teclado. La preferencia de pantalla inicial sigue aplicándose en sesiones nuevas.

## Colección

La colección global, los juegos de una plataforma, Favoritos y los listados de estadísticas comparten búsqueda, filtros, ordenación y vistas. Se pueden combinar plataforma, género (incluido Sin género), favorito, finalizado y edición especial. Los filtros son intersecciones: cada juego debe cumplir todos los activos. Las etiquetas permiten retirar un filtro; Limpiar filtros conserva la búsqueda y el orden. Borrar búsqueda limpia solo el texto.

El panel muestra las plataformas y géneros presentes en su colección de origen. Dentro de una familia se pueden distinguir los nombres de plataforma guardados, sin renombrarlos. El logo identifica cada plataforma del selector. Favoritos y los listados derivados conservan su restricción de origen al limpiar los filtros adicionales.

La ordenación permite título A–Z/Z–A, fecha de alta reciente, nota personal descendente y precio ascendente/descendente. El orden inicial es fecha de alta para que los juegos recién guardados sigan apareciendo primero. Las notas y precios desconocidos se sitúan al final. Los precios se agrupan por código de moneda y se ordenan dentro de cada moneda: no se aplica conversión ni se comparan importes en monedas distintas. La fecha de alta es independiente de la fecha de compra.

Se mantienen Lista, Cuadrícula de tres/cuatro carátulas y Cover Flow. Cuadrícula sigue siendo el valor de instalación sin preferencia anterior; una elección existente se respeta. Las carátulas compactas muestran favorito, edición especial y finalizado sin añadir el resto de los detalles de la ficha.

La búsqueda, los filtros, el orden y el desplazamiento se guardan con el estado de cada pantalla. Volver desde una ficha o cambiar de pestaña dentro de una plataforma recupera ese estado. La colección global y la portada de Sistemas mantienen su consulta al alternar secciones principales. Los botones de sección abren su pantalla principal, sin quedarse en un listado secundario restaurado. La colección indica cuántos resultados se muestran respecto a su conjunto de origen. Todos los juegos desde Sistemas, y Juegos desde el menú antiguo, mantienen la acción explícita de abrir la colección sin filtros ni búsqueda; Juegos en la barra inferior recupera la colección que se estaba consultando.

## Recuentos y compatibilidad

La cabecera global distingue Ejemplares (cada copia guardada), Equipos (unidades físicas registradas) y Especiales (ejemplares de ediciones especiales). Debajo se indica el número de familias de plataformas con videojuegos; Sistemas mantiene la unión de plataformas con juegos y/o hardware. Los contadores conservan sus accesos a listados y Sistemas.

Room v6, ZIP v5, historial de compras y datos guardados permanecen iguales. Estos cambios de consulta no modifican juegos, equipos ni precios. El rediseño profundo de fichas, estadísticas y preferencias queda para los siguientes bloques.
