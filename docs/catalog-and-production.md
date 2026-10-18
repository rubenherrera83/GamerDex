# Sistemas, búsqueda y proveedores · 0.6.0

## Inventario propio

«Mis sistemas» es independiente de «Plataformas», que sigue filtrando los juegos. Permite registrar varias unidades del mismo modelo: consola, portátil, PC o microordenador; variante y región; caja, instrucciones, cables/fuente; estado físico y funcionamiento; serie; fecha opcional ISO AAAA-MM-DD; precio exacto en céntimos y moneda; notas y fotos.

Los mandos/accesorios tienen nombre o edición, cantidad 1–99, estado físico, serie y notas. Para unidades con series o estados distintos, crea entradas separadas de cantidad uno. No se deducen accesorios incluidos en una consola a partir de su modelo.

La foto elegida tiene prioridad sobre la URL HTTPS opcional. Sin ambas se muestra una ilustración original genérica del tipo de sistema, explícitamente identificada; no pretende representar un modelo concreto. El emblema local de familia se usa cuando existe en el catálogo; para los demás se muestran iniciales. Puede personalizarse el logotipo con una URL HTTPS. Las imágenes remotas se guardan en la copia como URL; las fotos locales se incluyen como bytes.

Room v5 añade la tabla `hardware` sin alterar los juegos. La ficha se serializa en un payload JSON tipado con ID Room propio. Las copias v4 incluyen inventario, mandos y fotos; se leen v1–v4. Restaurar sustituye juegos e inventario de forma transaccional. Una copia anterior no contiene sistemas: la confirmación avisa de que ese inventario se vaciará. El formato v3 conserva sus notas 0–100; solo v1/v2 convierten 0–5 una vez. Versiones antiguas de la app no abren copias v4.

## Nombres y resultados de RAWG

El catálogo incorpora Genesis, Mega Drive II, Famicom, Super Famicom, Sega Mark III, SG-1000, Atari clásicas/Jaguar/Lynx/ST, 3DO, Neo Geo, PC Engine, Commodore, Spectrum, Amstrad, MSX y otros sistemas. Se mantienen los nombres regionales como elecciones distintas; los alias ayudan a buscar sin renombrar colecciones existentes. Las equivalencias se usan para localizar familias, no para garantizar compatibilidad de cartuchos, región o accesorios.

Consulta real del 18 de octubre de 2026: RAWG devuelve Sonic the Hedgehog (1991), ID 53551, con Genesis entre sus plataformas. Antes solo se mostraban las dos primeras plataformas y se elegía la primera al importar. Ahora se muestran todas, con alias; el usuario elige una plataforma antes de importar un juego multiplataforma. Se solicitan hasta 40 resultados y se pueden filtrar localmente por año y plataforma. No es una búsqueda exhaustiva de todo el catálogo; conviene precisar el título si no aparece.

## ScanDex

[Documentación oficial](https://scandex.gamery.app/documentation/api/), [condiciones](https://scandex.gamery.app/terms/), revisadas el 18 de octubre de 2026.

Se usa `GET /api/v2/lookup?value=...`, conservando ceros iniciales y con el token en la cabecera Authorization. Solo se consumen título y plataforma. No se usan ni guardan identificadores IGDB para enriquecer la ficha. Se busca después el título en RAWG y el usuario confirma el juego y su plataforma: no existe una equivalencia automática entre IDs de ambos proveedores.

Añadir `SCANDEX_API_TOKEN` a `local.properties` y recompilar activa las consultas en desarrollo. Sin token aparece un mensaje de servicio no configurado y sigue disponible la búsqueda por título. El lector utiliza Google Code Scanner (Google Play Services) y permite escribir EAN-8, UPC-A, EAN-13 o GTIN-14 a mano. Se comprueba el dígito de control. El módulo de cámara puede necesitar descargarse antes del primer uso. No se envían fotografías a ScanDex: solo el código, al pedir la búsqueda. No se usa el endpoint de sugerencias ni se publica información en el proveedor.

Un código desconocido (404), sin coincidencia, una credencial rechazada o una cuota agotada no bloquean el alta manual. La base del proveedor puede carecer de determinadas ediciones. Activar el servicio exige una cuenta y un token del proveedor; no se ha creado una cuenta en nombre del usuario.

## Publicación de GamerDex

La compilación de desarrollo consume RAWG/ScanDex directamente con credenciales locales inyectadas en BuildConfig. Se excluyen de Git, pero un APK permite extraerlas: `local.properties`, ofuscación o almacenamiento cifrado no convierten una clave compartida incluida en la app en un secreto seguro.

Para producción, usar una cuenta/credencial del proyecto autorizada por el proveedor y guardar el secreto exclusivamente en un backend. La app llamaría a endpoints propios de búsqueda, detalles, multimedia y código de barras; el backend validaría los parámetros, identificaría y limitaría clientes, controlaría cuotas, aplicaría caché conforme a las condiciones y consultaría al proveedor. Sin proxy abierto ni posibilidad de pedir URLs arbitrarias. Rotación/revocación de claves y seguimiento de cuotas se harían en el servidor. Mantener atribuciones y enlaces a las fuentes en Android. No enviar al proveedor inventario privado, series, fotos o precios de compra.

Esta versión no despliega ese servidor ni está preparada para publicar credenciales compartidas en un APK de producción. No hace falta que cada usuario final tenga una cuenta RAWG si el proyecto opera su propio acceso con un plan que lo autorice.

RAWG exige revisar el plan aplicable: su [página de planes](https://rawg.io/apidocs) presenta Free como no comercial y Business para uso comercial, mientras hay textos sobre límites comerciales distintos en la misma página y en sus [términos API](https://rawg.io/tos_api). Antes de monetizar o publicar, confirmar por escrito con RAWG las condiciones, cuotas y permisos efectivos del proyecto. Una cuenta distinta no sustituye esa autorización. ScanDex admite uso de título/plataforma en productos propios con cuenta aprobada; su acceso inicial y futuras tarifas también deben confirmarse para el lanzamiento.
