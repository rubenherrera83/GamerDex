# Revisión visual adaptativa · 0.18.0

La revisión se centra en una ventana de 320 × 640 dp con letra al 160 %, una ventana horizontal de 640 × 320 dp y el tamaño habitual del emulador. Se reproducen con datos sintéticos los problemas encontrados en navegación, menús, colección, estadísticas y gasto; se comprueba también el alta de juegos y las acciones de gamertags.

## Navegación y cabeceras

Cuando caben los cuatro nombres, la barra inferior mantiene iconos y etiquetas. Si falta ancho para la letra elegida, utiliza botones de texto que se reorganizan en filas y conservan los nombres completos, el estado seleccionado y el papel de pestaña para accesibilidad. En ventanas de poca altura usa una fila compacta cuando hay ancho suficiente. No se reduce el tamaño de letra elegido por el usuario.

El menú lateral pasa a ser desplazable, incluida su cabecera y pie, y deja un margen exterior en pantallas estrechas. Preferencias y Gamertags siguen siendo alcanzables aunque no quepan todas las entradas a la vez.

La cabecera común usa menos altura cuando la ventana tiene menos de 480 dp de alto, manteniendo logotipo, título y controles. Se mide el tamaño de la ventana actual para atender también ventanas redimensionadas, no solo la orientación del dispositivo. Se conserva la navegación sin fundido.

## Colección y estadísticas

La cuadrícula utiliza dos columnas al bajar de 360 dp de ancho o superar el 130 % de tamaño de letra y tres cuando en ese modo hay al menos 600 dp de ancho. Con tamaño habitual conserva tres columnas y cuatro al superar 600 dp. La elección Cuadrícula/Lista/Cover Flow se mantiene; solo cambia la distribución disponible.

Los contadores de la colección pasan a filas con número y etiqueta cuando necesitan más ancho. En ventanas bajas se compactan horizontalmente. Conservan recuentos y accesos a los listados. En estadísticas, las tarjetas usan una columna con ventana estrecha o letra ampliada; en dos columnas igualan la altura de cada pareja.

## Gasto y edición

En una ventana alta, año, plataforma y modo de gasto permanecen fijos como antes. En una ventana baja se desplazan junto al listado para dejar espacio útil a totales y desgloses. Se conservan selección, moneda, estado de los grupos y criterios de cálculo; no cambia ningún importe ni agrupación.

El formulario mantiene Guardar visible con letra grande, valida título/plataforma y conserva el contenido al volver desde la confirmación de cambios pendientes. Las pruebas del bloque anterior siguen cubriendo notas, compra, fotos, componentes y protección de formularios.

Room v6, copia ZIP v5, datos guardados y doce temas no cambian. Las capturas y pruebas cubren los tamaños indicados; esta revisión no sustituye una comprobación en dispositivos físicos de distintos fabricantes.
