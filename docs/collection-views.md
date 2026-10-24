# Vistas de colección · 0.19.0

Sistemas permite elegir Tarjetas, Lista y Cuadrícula debajo de la búsqueda. Lista utiliza filas con imagen o logotipo, nombre y recuentos de videojuegos y unidades; Cuadrícula agrupa varias plataformas por fila. Tarjetas conserva la presentación anterior. La elección se guarda localmente y también se aplica a Mis equipos, donde la fila compacta abre la ficha de cada unidad. No cambia la agrupación por familias ni sus totales.

Juegos mantiene Cuadrícula primero y como vista inicial de instalaciones nuevas. Lista muestra un pequeño rectángulo con miniatura de 36 × 48 dp, nombre, plataforma y botón de favorito con área táctil de 48 dp. La fila tiene un mínimo de 64 dp y puede crecer con títulos largos o letra ampliada. Tarjetas conserva las carátulas grandes de la antigua Lista; Cover Flow sigue disponible. La selección se recuerda y se aplica también a juegos de un sistema, favoritos y otros listados de juegos.

El orden inicial de cualquier consulta de juegos es Título A–Z, en las cuatro vistas. Filtrar o cambiar la presentación mantiene el orden. Ordenar permite elegir explícitamente otro criterio para esa consulta; regresar de una ficha, cambiar de sección o recrear Activity conserva esa elección. Todos los juegos limpia la consulta y recupera A–Z. Abrir otra lista de sistema, favoritos o estadísticas comienza en A–Z. Un espacio de estado nuevo evita restaurar el antiguo Alta reciente automático tras actualizar.

La búsqueda, los favoritos y la apertura de fichas funcionan en todas las vistas. Las preferencias de presentación de sistemas y juegos son independientes. No hay cambios de base de datos, copias ni transiciones entre pantallas.
