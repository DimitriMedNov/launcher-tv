# Launcher propio para Android TV: brief de diseño

Diseña la interfaz de un launcher (la pantalla de inicio que aparece al prender la tele o al presionar Home) para dos televisores Hisense con Google TV. Debe sentirse como Apple TV (tvOS): limpio, oscuro, con íconos grandes y fondos aéreos en movimiento, sin anuncios ni recomendaciones de terceros. Entrega un prototipo navegable con las flechas del teclado (arriba, abajo, izquierda, derecha, Enter = OK, Escape = Atrás), porque en la tele todo se maneja con el control remoto y no hay mouse ni pantalla táctil.

## Contexto
- Pantalla de 1920×1080, vista a unos 3 metros de distancia (interfaz de "10 pies").
- Área segura: deja 96 px libres en los lados y 54 px arriba y abajo; nada importante fuera de ahí.
- Una de las teles tiene poca memoria (1.8 GB de RAM). Evita efectos pesados: desenfoque (blur) solo en paneles pequeños y nunca sobre el video de fondo completo.
- Idioma: español de México. Ubicación del clima: Progreso, Yucatán (°C). Formato de hora: 4:12 p. m.

## Estilo
- Fondo base: gris casi negro `#1C1C1E`; superficies `#2C2C2E`; texto principal `#FFFFFF`; texto secundario `#AEAEB2` (contraste mínimo 4.5:1 sobre el fondo).
- Color de acento: azul `#0099FE` (foco, interruptores, progreso).
- Tipografía: Inter (libre, muy parecida a SF Pro de Apple). Tamaño mínimo de texto 24 px; títulos 40–56 px; la hora del protector 96 px.
- Íconos de apps: tarjetas 16:9, esquinas redondeadas de 24 px, sin nombre debajo. El nombre aparece solo en la tarjeta enfocada.
- Foco: la tarjeta enfocada crece al 110 %, sube 8 px, gana sombra suave y un brillo sutil. Debe verse clarísimo desde lejos qué está seleccionado.
- Movimiento: transiciones de 200–250 ms con salida suave (ease-out). Nada parpadea ni rebota.

## Pantallas a diseñar

### 1. Inicio
- Fondo: video aéreo a pantalla completa (ciudades, costas, montañas), con un degradado oscuro abajo para que se lean los íconos.
- Arriba a la derecha: hora, clima actual (ícono + "27° Cielo claro") y los íconos de wifi y ajustes.
- "Top Shelf" (franja superior, como en Apple TV): ocupa la mitad de arriba y muestra contenido de la app enfocada. Ejemplos:
  - Live TV: el canal en vivo en miniatura, con el nombre del canal y el programa actual.
  - Netflix / Disney+ / Prime Video: portada grande de "Seguir viendo".
  - Spotify: la canción que suena, con controles.
  - App sin contenido: solo el video aéreo.
- Fila de apps, en este orden: Live TV, Apple TV, Netflix, Disney+, YouTube, Prime Video, Spotify, Kick, Aerial Views, y después las demás (Claro video, DAZN, HBO Max, ViX, Mercado Play, FOX One, Twitch, Play Store, Configuración).
- Una segunda fila opcional, "Entradas": HDMI 1, HDMI 2, HDMI 3, HDMI 4 y Antena, con su ícono.

### 2. Estados del foco
- Tarjeta normal, enfocada y presionada (al dar OK).
- Menú al mantener OK presionado sobre una app: Abrir, Mover, Ocultar, Información de la app. Panel lateral derecho con fondo `#2C2C2E` al 90 %.
- Modo "Mover": la tarjeta flota y se desplaza con izquierda/derecha; OK confirma y Atrás cancela.

### 3. Panel rápido (botón de ajustes)
- Panel lateral derecho con: Entrada (HDMI/Antena), Wifi, Imagen, Sonido, Temporizador para apagar y Ajustes de Android.

### 4. Protector de pantalla
- Video aéreo a pantalla completa.
- Hora grande arriba a la izquierda (Inter, grosor medio).
- Clima arriba a la derecha.
- Descripción del lugar abajo a la derecha ("Vuelo sobre el fiordo de Aurland, Noruega").
- Sin otros elementos.

### 5. Estados obligatorios del Inicio
1. Cargando: esqueleto con la forma de la fila de tarjetas y del Top Shelf (no un círculo girando).
2. Sin internet: el video aéreo se cambia por una foto fija; aviso "Sin conexión" con botón "Abrir ajustes de wifi".
3. Sin apps visibles: mensaje "No hay apps en el inicio" y botón "Agregar apps".
4. Error al cargar el clima o el Top Shelf: se oculta solo ese bloque; nada se rompe.

## Accesibilidad y navegación
- Todo alcanzable con flechas, en orden lógico de izquierda a derecha y de arriba hacia abajo.
- Atrás siempre regresa un nivel; en el Inicio, Atrás dos veces abre el protector.
- Ningún texto sobre video sin degradado o sombra que asegure contraste.

## Entregables
- Prototipo navegable con teclado de las pantallas 1 a 5.
- Hoja de estilos con colores, tipografía, tamaños, radios, espaciados y la especificación del foco, lista para pasar a código (Kotlin + Jetpack Compose for TV).
