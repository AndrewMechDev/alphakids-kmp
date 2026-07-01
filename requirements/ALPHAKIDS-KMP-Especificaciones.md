# ALPHAKIDS KMP

## Documento Maestro de Análisis, UX/UI, Arquitectura y Roadmap

# 1. Contexto General del Proyecto

## Objetivo

Migrar la aplicación actual desarrollada en Kotlin Android nativo hacia Kotlin Multiplatform (KMP), manteniendo compatibilidad futura con Android e iOS.

Actualmente la aplicación utiliza Firebase para parte de su lógica, sin embargo se planea reemplazar dicha implementación por una arquitectura propia basada en APIs y Backend centralizado.

---

## Arquitectura General

### Aplicación Móvil

Tecnología objetivo:

* Kotlin Multiplatform (KMP)
* Compose Multiplatform
* MVVM
* Clean Architecture

Responsabilidades:

* Experiencia principal del niño.
* Juegos educativos.
* OCR.
* Speech To Text.
* Text To Speech.
* Sistema de recompensas.
* Gestión de mascotas.
* Visualización de progreso.
* Panel básico para padres.

---

### Backend

Tecnología:

* NestJS

Responsabilidades:

* Autenticación.
* Gestión de usuarios.
* Gestión de instituciones.
* Gestión de progreso.
* Gestión de recompensas.
* Métricas.
* Integración IA.
* Comunicación entre Web y App.

---

### Plataforma Web

Tecnología:

* Next.js

Responsabilidades:

* Gestión administrativa.
* Gestión educativa.
* Gestión de palabras.
* Gestión de actividades.
* Seguimiento de estudiantes.
* Gestión institucional.

---

# 2. Flujo Principal del Negocio

## Flujo Educativo

1. El docente registra palabras desde la plataforma web.
2. La información se envía al Backend.
3. El Backend distribuye la información a la aplicación móvil.
4. El niño recibe la actividad.
5. El niño interactúa mediante juegos.
6. Se registran métricas.
7. Se calculan recompensas.
8. Se sincronizan resultados.
9. Padres e instituciones pueden visualizar avances.

---

# 3. Problemas Detectados y Consideraciones Técnicas

## Problemas actuales

### UI Responsiva

Evitar errores como:

Bottom overflowed by X pixels.

La aplicación debe funcionar correctamente en:

* Teléfonos pequeños.
* Teléfonos medianos.
* Teléfonos grandes.
* Tablets.

---

### Firebase

Se planea eliminar la dependencia actual.

Toda la lógica deberá migrarse a:

* Backend NestJS.
* APIs propias.
* Base de datos propia.

---

### Arquitectura

Implementar:

* MVVM
* Repository Pattern
* Use Cases
* Dependency Injection

---

### Animaciones

Se tiene previsto utilizar:

Rive Animations

Las animaciones deberán ser compatibles con Compose Multiplatform.

---

# 4. Funcionalidades Actuales

## OCR de Letras

Objetivo:

Permitir que el niño forme palabras usando letras físicas MDF.

Proceso:

1. El niño forma una palabra físicamente.
2. Toma una fotografía.
3. OCR identifica las letras.
4. Se valida la palabra.
5. Se generan métricas.

---

# 5. Funcionalidades Futuras

## Speech To Text

Objetivo:

Capturar el deletreo realizado por el niño.

Requerimientos:

* Reconocer letra por letra.
* Adaptarse al ritmo del niño.
* Permitir pausas largas.
* Tolerar pronunciaciones imperfectas.

Problema actual:

Los motores tradicionales capturan palabras completas, pero no deletreo progresivo.

Se requiere investigar:

* Speech Recognition Streaming.
* Captura incremental.
* Detección letra por letra.

---

## Text To Speech

Objetivo:

Asistir al niño durante el aprendizaje.

Ejemplo:

Si la imagen es "Zorro":

* Pronunciar "Zorro".
* Indicar cuántas letras tiene.
* Indicar cuántas vocales tiene.
* Indicar cuántas consonantes tiene.

Sin almacenar archivos de audio.

Todo deberá generarse dinámicamente.

---

## Asistente Inteligente

Fase futura.

Posible integración:

* Gemini
* IA Conversacional

Objetivo:

Ayudar al niño a reconocer palabras.

Ejemplo:

"Esta imagen es un zorro."

"¿Sabías que zorro empieza con la letra Z?"

---

# 6. Diseño UX/UI Mobile

## Público Objetivo

Niños en etapa inicial de alfabetización.

Edad estimada:

4 a 8 años.

---

## Identidad Visual

Mascota principal:

Alphi

Pendiente definir:

* Paleta de colores.
* Tipografías.
* Sistema de ilustraciones.
* Sistema de animaciones.

---

## Principios UX

La aplicación debe ser:

* Muy visual.
* Muy amigable.
* Pocos textos.
* Grandes botones.
* Navegación simple.
* Recompensas constantes.
* Retroalimentación inmediata.

---

## Sistema de Gamificación

Elementos:

* Monedas.
* Niveles.
* Rangos.
* Mascotas.
* Logros.
* Recompensas.

---

# 7. Diseño UX/UI – Menú Principal Niño

## Opción 1: Jugar

Contendrá:

### Escaneo de Letras

OCR.

### Deletreo

Speech To Text.

### Ayuda de Pronunciación

Text To Speech.

---

## Opción 2: Diccionario o Cofre de Palabras

Función:

Guardar todas las palabras correctamente aprendidas.

Estructura:

A

* Ana

B

* Barco

C

* Casa

...

Z

* Zorro

Ordenado alfabéticamente.

---

## Opción 3: Logros

Sistema visual de progreso.

Inspiración:

Videojuegos RPG.

MOBA.

Sistema de niveles.

---

## Sistema de Rangos

Nivel 1

Semillita 🌱

Nivel 2

Hoja Verde 🍃

Nivel 3

Pequeño Árbol 🌳

Nivel 4

Pequeño Sabio 🦉

Nivel 5

Guardián del Bosque 🦊

Nivel Máximo

Súper Protector 🐻

---

# 8. Sistema de Mascotas

## Tienda de Mascotas

El niño podrá adquirir mascotas utilizando monedas.

Las mascotas se obtienen exclusivamente jugando.

---

## Tienda de Accesorios

Tipos:

* Galletas.
* Croquetas.
* Huevos.
* Alimentos especiales.

Cada accesorio dependerá del tipo de mascota.

---

## Perfil de Mascota

Cada mascota tendrá:

* Nombre.
* Estado de felicidad.
* Estado de hambre.
* Nivel.
* Experiencia.

Acciones:

* Alimentar.
* Interactuar.
* Visualizar progreso.

---

## Sistema de Desbloqueo

Nivel 0

Elegir 1 mascota obligatoria entre 3 disponibles.

Nivel 10

Desbloquea mascota adicional.

Nivel 20

Desbloquea mascota adicional.

Nivel 30

Desbloquea mascota adicional.

Y así sucesivamente.

---

# 9. Sistema de Recompensas

Factores considerados:

* Tiempo empleado.
* Cantidad de intentos.
* Errores.
* Aciertos.

Ejemplo:

1 intento:
100 puntos

5 intentos:
50 puntos

10 intentos:
20 puntos

Siempre debe existir una recompensa mínima.

---

# 10. Perfil del Padre o Tutor

## Dashboard Principal

Visualizar:

* Hijos registrados.
* Avance general.
* Progreso.
* Métricas.
* Logros.

---

## Gestión de Cuenta

* Perfil.
* Seguridad.
* Configuración.

---

## Suscripciones

* Estado del plan.
* Pagos.
* Renovaciones.

---

## Soporte

* Centro de ayuda.
* Contacto.

---

## Comunicación Institucional (Futuro)

Chat con profesores.

Restricción:

Solo disponible cuando:

* El estudiante pertenezca a una institución afiliada.
* El padre tenga relación validada.

---

# 11. Seguridad Infantil

Objetivo principal:

Proteger la identidad e integridad de los niños.

Medidas:

* No permitir acceso libre a instituciones.
* Validación institucional obligatoria.
* Protección contra suplantaciones.
* Protección contra hostigamiento.
* Restricción de contacto no autorizado.

---

# 12. Modelo Freemium

Versión Gratuita

Permitirá:

* Registro básico.
* Juegos.
* Mascota inicial.
* Perfil básico.

---

Versiones Premium

Permitirá:

* Funciones avanzadas.
* Más mascotas.
* Más contenido.
* Herramientas exclusivas.

---

## Bloqueo UX

Cuando una función premium sea seleccionada:

Alphi mostrará un mensaje amigable explicando las ventajas del plan premium.

---

# 13. Roadmap Futuro

## Fase 1

* Migración a KMP.
* OCR.
* Sistema de recompensas.
* Sistema de mascotas.
* Dashboard de padres.

## Fase 2

* Speech To Text.
* Text To Speech.
* IA de apoyo.

## Fase 3

* Comunicación institucional.
* Funciones avanzadas.
* Sistema Premium.

## Fase 4

* Analítica avanzada.
* IA personalizada.
* Expansión del ecosistema.

Preguntas Técnicas y Decisiones Pendientes (Separadas del Documento Principal)
Arquitectura y Backend
¿Conviene utilizar REST API, GraphQL o WebSockets para la comunicación App ↔ Backend?
¿Qué eventos deberían enviarse en tiempo real y cuáles mediante APIs tradicionales?
¿Cómo modelar el flujo de sincronización entre Web, Backend y App móvil?
Bases de Datos
¿Las métricas de juegos (intentos, errores, aciertos, tiempo) deberían almacenarse en MongoDB o PostgreSQL?
¿Qué información conviene almacenar en una base relacional y cuál en una no relacional?
¿Dónde conviene almacenar el Refresh Token: Redis, PostgreSQL o ambos?
Speech To Text
¿Qué motor es más adecuado para capturar deletreo letra por letra?
¿Cómo manejar pausas largas del niño sin cortar el reconocimiento?
¿Es viable implementar reconocimiento incremental de letras en KMP?
Text To Speech
¿Cuál es la mejor alternativa para generar audio dinámico sin almacenar archivos?
¿Conviene procesarlo localmente o mediante backend?
Gamificación
¿Cuál debería ser la fórmula exacta de puntos según tiempo e intentos?
¿Cómo evitar que el sistema de recompensas sea explotado?
Monetización
¿La pasarela de pagos debe estar en la App, en la Web o en ambas?
¿Las suscripciones deben ser gestionadas únicamente por el perfil del padre?
¿Cómo modelar el sistema Freemium en la base de datos?
Documentación y Automatización
¿Conviene utilizar Notion, Obsidian o ambos para la documentación?
¿Vale la pena integrar MCP de GitHub para commits y gestión de repositorios?
¿Es necesario utilizar MCP especializado para Kotlin Multiplatform o basta con documentación oficial actualizada?
Desarrollo
¿Qué estructura de carpetas, Skills y Agentes se utilizará para mantener el proyecto organizado?
¿Qué Skills específicas se crearán para:
Buenas prácticas Git.
KMP.
UX/UI.
Backend NestJS.
Testing.
Seguridad.
CI/CD.