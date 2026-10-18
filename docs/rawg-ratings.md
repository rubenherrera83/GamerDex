# Valoraciones de RAWG

Desde 0.5.0 la ficha muestra **Tu nota** y **Comunidad RAWG** por separado. La nota personal se guarda en la colección y usa 0–100. La media externa procede de `GET /games/{id}`, requiere un juego vinculado a RAWG y se consulta con la caché HTTP habitual.

- `rating`: media de la comunidad en escala 0–5, convertida a 0–100 multiplicando por 20. La ficha indica esta conversión.
- `ratings_count`: número de votos, mostrado junto a la media. Un dato ausente, no finito, fuera de rango, cero o sin votos se presenta como «Sin media disponible».
- `rating_top` no determina el denominador de la conversión.
- Los fallos de valoración y multimedia son independientes y permiten reintentar. La nota personal nunca se sobrescribe con la externa.

El color varía entre rojo (<50), amarillo (50–74) y verde (≥75), con tonos adaptados a temas claros y oscuros. La nota personal añade una etiqueta BAJA/MEDIA/ALTA; sin valoración se muestra «Sin valorar».

La media es un dato consultado y no se exporta como dato personal. Su incorporación en 0.5.0 no cambió Room v4 ni las copias v3. Desde 0.6.0, el inventario de hardware utiliza Room v5 y copias v4; la media sigue separada de los datos personales.

RAWG también puede incluir `metacritic`, una puntuación de crítica sobre 100; no está integrada en esta versión. No es necesario consultar otra web para obtener la media de la comunidad RAWG. Estas fuentes representan poblaciones distintas y no se combinan en una media única.

Datos comprobados con respuestas reales y pruebas de contrato, el 17 de octubre de 2026. Referencias: [documentación API](https://api.rawg.io/docs/) y [metadatos disponibles](https://rawg.io/apidocs).
