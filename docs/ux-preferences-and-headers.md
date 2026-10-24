# Preferencias y cabeceras · 0.16.0

Todas las pantallas usan `ScreenHeader`: fondo de superficie, el mismo tamaño de logotipo, título legible con tipografía común y control de navegación. El logotipo conserva el estilo del tema. El título admite dos líneas; la altura crece si lo necesita el tamaño de letra del dispositivo. Menú, Volver y acciones contextuales permanecen arriba al desplazarse. Las pestañas de plataforma, accesos de fichas y filtros de gasto siguen debajo de esta cabecera.

Sistemas, Juegos y Estadísticas comparten así la misma jerarquía. Juegos pierde el subtítulo decorativo «PLAYER HUB» y el rombo; sus contadores y buscador permanecen juntos debajo de la cabecera. El menú lateral conserva la identidad completa de GamerDex. El fondo de ventana sigue la superficie del tema para mantener legibles las barras del sistema al pasar de un tema oscuro a uno claro, también tras recrear Activity.

## Preferencias

- Identidad: apodo usado por defecto en gamertags y aviso de guardado automático.
- Pantalla inicial: Sistemas o Todos los juegos, sin abandonar la sesión actual.
- Copias de seguridad: tarjeta propia antes de los doce temas. Accesos fijos a Identidad, Apariencia y Copias para evitar recorrer la lista completa.
- Apariencia: tema actual, doce opciones con colores de muestra y selección accesible; aplicación y guardado inmediatos. Se conserva la posición al recrear Activity.

Crear y recuperar copias siguen usando el selector de archivos de Android. Recuperar valida el archivo y muestra cantidades/fecha antes de pedir confirmación de sustitución. Cancelar no sustituye datos; las acciones se bloquean mientras se procesa. No cambian el contenido del ZIP, la validación ni el servicio de restauración. Los mensajes de resultado se anuncian a accesibilidad.

## Inicio y vistas

Desde esta actualización, tanto instalaciones nuevas como anteriores adoptan Sistemas como valor inicial. Es una migración local de una sola vez: las versiones anteriores guardaban Juegos automáticamente y no registraban si se había elegido explícitamente. Después de la migración, cualquier elección en Preferencias se conserva, también Todos los juegos. La restauración de una sesión abierta mantiene su pantalla; el nuevo inicio se aplica a sesiones nuevas.

El selector de visualización queda en el orden **Cuadrícula · Lista · Cover Flow**. Cuadrícula sigue siendo el valor por defecto cuando no hay una elección previa; los valores existentes, incluida la migración de la antigua preferencia horizontal, se conservan. Los nombres almacenados de las vistas no cambian.

Room v6 y copia portable v5 no cambian. Los ajustes locales permanecen fuera del ZIP. El editor de gamertags conserva sus funciones y recibe la cabecera común; su reorganización interna queda para el siguiente bloque.