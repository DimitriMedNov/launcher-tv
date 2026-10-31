# Launcher para la tele del cuarto: brief de diseño

Diseña la interfaz de un launcher (la pantalla de inicio que aparece al prender la tele o al presionar Home) para una tele Hisense con Google TV que está en una recámara. Estilo **futurista**: interfaz tipo panel de mando (HUD), limpia y oscura, con líneas finas luminosas, números grandes y movimientos precisos. Sin anuncios ni recomendaciones de terceros. Entrega un prototipo navegable solo con teclado (flechas, Enter = OK, Escape = Atrás, mantener Enter = mantener OK), porque en la tele todo se maneja con el control remoto: no hay mouse ni pantalla táctil.

Ya existe otro launcher para la tele de la sala (estilo Apple TV). Este debe ser distinto en estilo, con las mismas funciones mejoradas.

## Contexto técnico (no negociable)
- Pantalla de 1920×1080, vista a unos 3 metros (interfaz de "10 pies").
- Área segura: 96 px libres a los lados y 54 px arriba y abajo.
- La tele tiene Android 11 y solo 1.8 GB de RAM. El diseño se programará después en Kotlin + Jetpack Compose for TV, así que todo efecto debe poder hacerse ahí:
  - Fondo de **color**, sin fotos ni videos: degradados y formas simples.
  - Sin desenfoque (blur) ni capas semitransparentes que cubran toda la pantalla. Los paneles de vidrio se simulan con color sólido semitransparente y un borde fino.
  - **Ninguna animación infinita.** Todo lo que se mueve hace su movimiento y se queda quieto (máximo unas 3 vueltas). Si algo se mueve sin parar, la tele repinta toda la pantalla en cada cuadro y se pone lenta.
  - Solo se anima posición, tamaño y transparencia; nada de animar sombras grandes ni degradados de pantalla completa.
- Idioma: español de México. Clima de Progreso, Yucatán, en °C. Hora en formato 4:12 p. m.

## Estilo futurista
- Base casi negra con un toque azul (propón el tono), superficies un escalón más claras y bordes de 1 px luminosos al 15–25 %.
- Un color de acento cian eléctrico para el foco, y uno secundario (violeta o magenta) solo para detalles. Contraste mínimo de 4.5:1 en todo texto.
- Tipografía libre de Google Fonts: una geométrica para títulos (por ejemplo Space Grotesk) y una con números de ancho fijo para hora y datos. Texto mínimo de 24 px; títulos de 40–64 px; hora del protector de 160 px o más.
- Foco clarísimo desde lejos: la tarjeta enfocada crece, gana un borde de acento con resplandor suave y una línea de "escaneo" que la cruza una sola vez.
- Movimiento: 200–300 ms con salida suave. Entradas escalonadas cortas al abrir una sección. Nada rebota ni parpadea.

## Valores de diseño que se deben cumplir
- **Estructura:** cada pantalla armada con piezas reutilizables y consistentes (tarjeta de app, panel, botón, fila de ajuste, chip de dato). Inspírate en catálogos como 21st.dev y Watermelon UI, adaptados a televisión.
- **Datos:** todo número que pueda mostrarse como gráfica se muestra como gráfica sencilla y legible desde lejos (por ejemplo, el pronóstico de 5 días como línea de temperatura con máximas y mínimas). Estilo Bklit.
- **Movimiento:** con intención, un solo tipo por componente, estilo Animate UI y Anime.js. Debe existir una versión con movimiento reducido (solo fundidos).
- **Micro-interacciones:** respuestas pequeñas y satisfactorias al control, estilo Bencho: menú radial al mantener OK, confirmar manteniendo OK con un anillo que se llena, lista que se reordena.
- **Los 4 estados** en toda vista con datos: cargando (esqueleto con la forma del contenido real, no un círculo girando), vacío (mensaje + acción para salir), error (qué pasó + botón Reintentar) y éxito.
- **Accesibilidad:** todo se alcanza con flechas en orden lógico, el foco siempre se ve, Atrás regresa un nivel, nada importante depende solo del color.

## Pantallas a diseñar

### 1. Inicio (mejorado respecto a la sala)
- Barra lateral con Inicio, Entradas y Ajustes; se abre con izquierda.
- Arriba a la derecha: hora, clima actual y un indicador de modo noche.
- Panel de la app enfocada (mitad de arriba): nombre, qué se está viendo o escuchando si se sabe (Seguir viendo, canción de Spotify con su progreso), y botones Abrir / Más info. Si la app no tiene nada que mostrar, un panel sobrio con su color.
- Cuadrícula de apps de 6 columnas, en este orden: Apple TV, Netflix, Disney+, YouTube, Prime Video, Spotify, Kick, y después las demás (Claro video, DAZN, HBO Max, ViX, Mercado Play, FOX One, Twitch, Play Store, Configuración).
- El color del fondo se ajusta suavemente al color de la app enfocada y se queda quieto.
- Mantener OK sobre una app abre un **menú radial**: Abrir, Mover, Ocultar e Información. Se elige con flechas.
- Modo Mover: la tarjeta flota y se mueve con las flechas; OK confirma y Atrás cancela.

### 2. Entradas
- HDMI 1, HDMI 2 y HDMI 3, cada una con su ícono y si tiene señal (el punto de señal late unas veces y se queda fijo). Sin antena.

### 3. Ajustes
- Accesos rápidos: Wifi, Imagen, Sonido y Ajustes de Android.
- **Temporizador para dormir** destacado (15, 30, 60 y 90 min), con la cuenta regresiva visible en el Inicio mientras corre.
- Modo noche: Automático (de 10 p. m. a 7 a. m.), Siempre o Nunca.
- Pronóstico de 5 días como gráfica.
- Apps ocultas: lista para volver a mostrarlas.

### 4. Modo noche
- Toda la interfaz baja de brillo y pasa a tonos cálidos (ámbar tenue en vez de cian), sin perder el contraste de 4.5:1.
- El cambio entre día y noche es un fundido lento de unos 2 segundos.

### 5. Protector de pantalla
- Fondo de color oscuro, hora enorme, fecha y clima.
- Para no marcar la pantalla, la hora cambia de lugar una vez por minuto con un fundido (no se desliza sin parar).
- En modo noche, más tenue todavía.

### 6. Estados del Inicio
1. Cargando: esqueleto con la forma del panel y de la cuadrícula.
2. Sin internet: aviso "Sin conexión" con botón "Abrir ajustes de wifi"; el resto sigue funcionando.
3. Sin apps visibles: "No hay apps en el inicio" con botón "Mostrar apps ocultas".
4. Error en el clima o en el panel: solo ese bloque muestra el error con Reintentar; nada más se rompe.

## Entregables
- Prototipo navegable con teclado de las pantallas 1 a 6, con un atajo para alternar día y noche y otro para ver cada uno de los 4 estados.
- Hoja de estilos lista para pasar a Kotlin + Jetpack Compose for TV: colores (día y noche), tipografías y tamaños, espacios, radios, bordes, resplandor del foco y duraciones de cada animación.
