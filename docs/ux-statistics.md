# Cuarta mejora UX · 0.15.0

## Estadísticas de la colección

La portada muestra primero los ejemplares y equipos del inventario, seguidos de los contadores de favoritos, finalizados, progreso al 100 %, platinos, ediciones especiales y copias físicas completas. Cada contador abre su listado. Tener el juego al 100 % sigue siendo independiente de conservar caja e instrucciones.

Las plataformas tienen logotipo y porcentaje sobre los ejemplares actuales. Los géneros mantienen sus accesos a los juegos; un ejemplar puede pertenecer a varios, por lo que la suma de porcentajes de géneros puede superar 100 %. No se modifica cómo se cuentan copias o versiones.

Gasto está disponible directamente en la barra superior y en el resumen histórico. La consulta ocupa una pantalla completa, sustituyendo el antiguo diálogo de altura limitada. Volver, incluido el botón del sistema, recupera la portada de estadísticas.

## Consulta de gasto

Año y plataforma permanecen visibles arriba. Se combinan entre sí y se pueden limpiar sin cambiar de desglose. Año desconocido contiene las compras sin fecha: no se asigna el año del juego ni se estima una fecha. Las plataformas financieras conservan exactamente el nombre registrado, sin fusionar alias regionales ni familias.

El resumen presenta el número de compras, el total de cada moneda y sus importes de videojuegos/equipos. Los importes desconocidos no se suman y se indica cuántas compras carecen de precio. Cero es un importe válido. Las compras de fichas eliminadas siguen formando parte del gasto histórico registrado.

Los modos Por año, Por plataforma y Por sistema reúnen los mismos registros filtrados. En plataforma y sistema se puede desplegar el desglose anual de cada grupo. Los juegos se atribuyen a una unidad únicamente cuando se guardó ese vínculo; los demás permanecen sin vincular. Se mantienen las referencias a unidades eliminadas y la distinción entre unidades con nombres repetidos.

## Gráficos

El gráfico muestra una moneda cada vez. Su selector no cambia los totales de la selección ni realiza conversiones. Solo aparecen grupos con importes registrados en esa moneda; un importe cero se muestra con su cifra y una barra vacía.

Por año se presentan hasta los seis años más recientes con importes, en orden cronológico, más Año desconocido cuando corresponda. Por plataforma/sistema se presentan hasta los seis grupos de mayor importe. El texto explica este límite y el desglose inferior conserva todos los grupos. Las barras comparan los importes visibles; las cifras formateadas son los valores monetarios exactos.

## Compras archivadas y estado

Archivadas limita el resumen a compras cuyas fichas ya no están en el inventario, aplicando también año y plataforma. Quitar del historial exige confirmación y no modifica la colección actual. La confirmación se recupera al recrear Activity; Cancelar conserva el registro. Si una compra deja de estar archivada, su confirmación pendiente se descarta.

Se conservan año, plataforma, modo, moneda del gráfico y grupos desplegados al volver desde la portada, recrear Activity o alternar secciones principales. Cada modo mantiene su desplazamiento. Cambiar filtros lleva al principio de los resultados del modo consultado.

Room v6, ZIP v5 y el registro de compras no cambian. No se añaden monedas, conversiones, estimaciones ni campos persistidos. Los filtros de pantalla siguen siendo estado de la sesión y no preferencias incluidas en la copia portable. Preferencias y el editor de gamertags quedan para bloques posteriores.
