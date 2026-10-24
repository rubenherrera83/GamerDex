# Editor de gamertags · 0.17.0

La vista previa abre el editor, con los totales globales de videojuegos y equipos. Se puede ampliar y volver sin perder la edición; la ampliación permanece abierta al recrear Activity. Los botones Compartir y Guardar PNG están fijos al pie del editor y no requieren recorrer sus opciones.

Los accesos superiores llevan a Vista previa, Contenido, Diseño, Componentes (en tarjetas de equipos) y Logos. El desplazamiento y las elecciones se restauran al recrear Activity. Se mantiene la cabecera compartida de la app.

## Contenido e identidad

El apodo parte de Preferencias y se puede modificar para la tarjeta. Colección, Juego, Plataforma y Sistema / PC conservan sus funciones. Los totales siguen contando todos los ejemplares y todas las unidades físicas, también en tarjetas individuales.

El selector de juegos muestra título, plataforma y edición. El de equipos muestra nombre, plataforma y serie para distinguir unidades homónimas; se busca por cualquiera de esos datos. Las series ayudan a elegir el equipo, pero no se trasladan a la imagen. La búsqueda y el selector abierto se conservan al recrear Activity; una búsqueda vacía de resultados lo indica expresamente.

## Diseño y componentes

Por defecto la tarjeta sigue el tema de la app. Elegir diseño permite usar cualquiera de los doce estilos en la tarjeta sin cambiar Preferencias. Usar tema de la app vuelve a enlazarlos. La elección independiente pertenece a la sesión de edición y se restaura con ella; una tarjeta nueva vuelve al tema de la app.

Las tarjetas de equipos conservan hasta diez componentes/accesorios, cantidades y notas opcionales. La sección indica el recuento seleccionado. No se incluyen los campos de precio, fecha ni serie del equipo o accesorios. Los equipos sin accesorios explican dónde añadirlos.

## Logotipos y salida

Se conservan hasta seis logotipos, sus fuentes locales/HTTPS, consolas, compañías, carátulas e imágenes propias. La selección se guarda para futuras tarjetas. Las filas del editor permiten ordenar y quitar imágenes; el selector mantiene categorías, búsqueda, recuento y Listo accesibles sin llegar al final del catálogo.

Mientras se genera una nueva imagen, la anterior permanece en la vista previa con indicador de actualización. Compartir y Guardar se habilitan cuando la imagen corresponde a los controles actuales. Si falla la generación se puede reintentar. Guardar captura la imagen confirmada antes de abrir el selector de archivos y mantiene ese contenido si hay que recrear el ViewModel; cancelar el selector descarta esa exportación pendiente.

El PNG sigue siendo de 1600 × 900. Compartir abre el selector de Android con un URI temporal de FileProvider y permiso de lectura. Guardar usa el selector de documentos del dispositivo. No cambian el renderizador de las tarjetas, el inventario, Room v6 ni ZIP v5.

## Navegación

Las transiciones de entrada, salida y regreso entre destinos de la app están desactivadas: los cambios de pantalla son directos, sin fundido. Se mantienen las animaciones propias de paneles y diálogos.
