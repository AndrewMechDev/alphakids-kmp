ALPHAKIDS KMP V2

Estructura Definitiva de Pantallas, Pestañas,

Submódulos y Flujo Completo

ESTADO ACTUAL DE IMPLEMENTACIÓN — Julio 2026

Todas las 6 fases de mejora UX/UI completadas:
- Sistema de diseño global (circadian backgrounds, cards homogéneas, safe areas)
- Splash + Welcome rediseñados
- Login con campos mejorados, contraste y circadian
- Selector de perfiles con avatares circulares (Netflix-style)
- Panel de padres con dashboard de datos reales (AggregatedStats por hijo)
- Dashboard del niño con avatar circular + Alphi sin card
- Hub de juegos con gradientes + selector de palabras en grid
- Escáner de letras simplificado (sin métricas redundantes)
- Todos los colores usan tokens circadian (sin Color.White hardcodeado)

1. FLUJO COMPLETO DEL PRODUCTO

Primera Instalación

AlphaKidsSplash ↓ WelcomeSelection ↓ ParentLogin (o Register) ↓ Verificación OTP ↓ SetupWizard (6 pasos) ↓ AdventureHome

Uso Diario

Si existe un único hijo

AlphaKidsSplash ↓ AdventureHome

Si existen varios hijos

AlphaKidsSplash ↓ ChildProfileSelector ↓ NetflixProfilesScreen ↓ AdventureHome

2. FLUJO DE ACCESO

AlphaKidsSplash

Contenido

•

Logo AlphaKids

•

Mascota Alphi

•

Animación inicial

•

Validación de sesión

•

Carga de configuración

ParentLogin

Contenido

•

Correo

•

Contraseña

•

Recuperar contraseña

1

•

Iniciar sesión

•

Crear cuenta

ParentRegister

Contenido

•

Nombre completo

•

Correo

•

Teléfono

•

Contraseña

•

Confirmar contraseña

•

Términos y condiciones

Verificación de Cuenta

Contenido

•

Código OTP

•

Validación de correo

•

Confirmación de identidad

ParentSetupWizard

Contenido

•

Bienvenida

•

Introducción a AlphaKids

•

Beneficios

•

Explicación del sistema de aprendizaje

•

Indicador de progreso

Acciones

•

Comenzar configuración

CreateChildProfile

Contenido

•

Nombre

•

Edad

•

Fecha de nacimiento

•

Avatar o fotografía

2

Acciones

•

Continuar

ChooseChildAvatar

Contenido

•

Selector de avatar

•

Vista previa

•

Categorías de avatar

Acciones

•

Guardar avatar

ChooseFirstPet

Contenido

•

Tres mascotas iniciales

•

Descripción

•

Personalidad

•

Vista previa

Modal

•

Asignar nombre a mascota

Acciones

•

Confirmar mascota

WelcomeAdventure

Contenido

•

Avatar elegido

•

Mascota elegida

•

Nivel inicial

•

Monedas iniciales

•

Mensaje de bienvenida de Alphi

Acciones

•

Ir al inicio

3

ChildProfileSelector

Contenido

•

Avatar

•

Nombre

•

Edad

•

Nivel

•

Mascota principal

Acciones

•

Seleccionar perfil

•

Crear perfil

•

Editar perfil

3. NAVEGACIÓN PRINCIPAL DEL NIÑO (AdventureHome)

Bottom Navigation (3 tabs):

1. 📊 Inicio — Dashboard + acceso a Jugar y Diccionario (overlay)
2. 🛒 Tienda — Mascotas, alimentos, accesorios
3. 🐾 Mascotas — Perfiles + desbloqueo por nivel

Nota: Diccionario y Logros están integrados dentro de la pantalla de Inicio como
overlay y sub-secciones respectivamente, no como tabs separados.

4. PESTAÑA INICIO (AdventureHome — Tab 1)

Header Global

•

Avatar (circular con color de la paleta de avatarColors)

•

Nombre

•

Nivel

•

Monedas

•

Estrellas

•

Configuración

Panel de Bienvenida

•

Mensaje de Alphi

•

Objetivo del día

4

Resumen de Progreso

•

Palabras aprendidas

•
•

Palabras pendientes
Racha

•

Nivel

•

XP

Mascota Activa

•

Nombre

•

Hambre

•

Felicidad

•

Nivel

Acciones

•

Alimentar

•

Ver perfil

Redirige a:

Perfil Mascota

Actividades Pendientes

•

Actividades asignadas

•

Palabras pendientes

Acción

•

Continuar

Redirige a:

Jugar

Accesos Rápidos

•

Jugar

•

Diccionario

•

Mascotas

•

Logros

5

5. PESTAÑA JUGAR

Vista Principal

Card OCR

Información:

•

Nombre

•

Descripción

•

Recompensas

Acción:

•

Comenzar

Redirige a:

WordScannerChallenge

Card Deletreo

Información:

•

Nombre

•

Descripción

•

Recompensas

Acción:

•

Comenzar

Redirige a:

SpellingAdventure

Vista OCR (WordScannerChallenge)

Contenido

•

Imagen referencia

•

Espacios vacíos

•

Cámara

•

Letras detectadas

•

Tiempo

•

Intentos

•

Aciertos

•

Errores

6

Resultado OCR

•

Monedas

•

XP

•

Estrellas

•

Rendimiento

Vista Deletreo (SpellingAdventure)

Contenido

•

Imagen referencia

•

Cantidad de letras

•

Vocales

•

Consonantes

Funciones

•

Escuchar palabra (Text To Speech)

•

Iniciar grabación

•

Pausar

•

Reintentar

Métricas

•

Intentos

•

Letras correctas

•

Letras incorrectas

•

Tiempo

Resultado Deletreo

•

Monedas

•

XP

•

Estrellas

•

Rendimiento

6. PESTAÑA DICCIONARIO

Vista Principal

Buscador

Filtros

•

Estado

•

Categoría

•

Dificultad

7

Listado de Palabras

Cada card contiene:

•

Imagen

•

Palabra

•

Categoría

Vista Detalle Palabra

•

Imagen grande

•

Palabra

•

Categoría

•

Fecha aprendizaje

•

Veces practicada

•

Estrellas

7. PESTAÑA MASCOTAS

Tab: Mis Mascotas

Muestra

•

Mascotas desbloqueadas

•

Mascotas bloqueadas

Card Mascota

•

Imagen

•

Nombre

•

Nivel

Acción:

•

Ver perfil

Perfil Mascota

Información

•

Nombre

•

Nivel

•

XP

8

•

Hambre

•

Felicidad

•

Energía

Acciones

•

Alimentar

•

Jugar

•

Interactuar

•

Seleccionar principal

Sistema de Desbloqueo

Nivel 0

•

1 mascota

Nivel 10

•

2 mascotas

Nivel 20

•

3 mascotas

Nivel 30

•

4 mascotas

Tab: Tienda

Categoría Mascotas

•

Imagen

•

Nombre

•

Rareza

•

Precio

Categoría Alimentos

•

Imagen

•

Nombre

•

Precio

9

Categoría Accesorios

•

Imagen

•

Nombre

•

Precio

Estados

•

Disponible

•

Comprado

•

Bloqueado

•

Monedas insuficientes

8. PESTAÑA LOGROS

Tab Rangos

•

Rango actual

•

XP actual

•

XP siguiente

Tab Trofeos

•

Nombre

•

Descripción

•

Progreso

•

Estado

Tab Estadísticas

•

OCR completados

•

Deletreos completados

•

Tiempo jugado

•

Palabras aprendidas

•

Monedas ganadas

Tab Historial

•

Logros recientes

•

Recompensas recientes

•

Niveles alcanzados

10

9. PANEL PADRE/TUTOR

Dashboard (implementado con AggregatedStats)

•

Hijos registrados (total count)

•

Palabras aprendidas (suma de todos los hijos)

•

Tiempo total de juego (minutos)

•

Monedas ganadas

•

Estrellas obtenidas

•

OCR completados

•

Deletreos completados

•

Nivel promedio

•

Actividad reciente (lista)

Gestión de Hijos (LazyColumn + cards + AsyncImage)

•

Lista de hijos con avatar circular + nombre + edad + nivel

•

Última conexión

•

Botón Editar

•

Botón Eliminar

•

Botón Agregar hijo

Perfil Hijo

•

Nivel

•

Rango

•

Mascotas

•

Logros

•

Métricas

Suscripción

•

Plan actual

•

Pagos

•

Renovaciones

Soporte

•

FAQ

•

Contacto

•

Reportar problema

Comunicación Institucional (Futuro)

•

Solo padres validados

•

Solo instituciones afiliadas

11

10. COMPONENTES GLOBALES

Monedas

•

Comprar mascotas

•

Comprar accesorios

Estrellas

•

Logros

•

Progreso

XP

•
•

Subir nivel
Desbloquear contenido

Alphi

•

Ayuda contextual

•

Tutoriales

•

Mensajes Freemium

•

Motivación

•

Asistente educativo

11. MODELO FREEMIUM

Gratuito

•

OCR

•

Mascota inicial

•

Perfil básico

•

Juegos básicos

Premium

•

Más mascotas

•

Más contenido

•

Más actividades

•

Funciones avanzadas

12

Bloqueo UX

Alphi explica las ventajas Premium cuando una función bloqueada es seleccionada.

12. FUNCIONALIDADES FUTURAS

•

IA Gemini

•

Comunicación Institucional

•

Analítica Avanzada

•

IA Personalizada

13

