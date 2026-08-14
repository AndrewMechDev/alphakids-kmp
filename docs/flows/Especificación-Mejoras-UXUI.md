## `# Especificación de Mejoras UX/UI – AlphaKids V2` 

## `## Objetivo General` 

```
Realizar una revisión completa de toda la interfaz de AlphaKids para mejorar la
experiencia visual, la consistencia del diseño y la usabilidad. Todas las
pantallas deben seguir un mismo lenguaje de diseño basado en Apple Human
Interface Guidelines, manteniendo un estilo moderno, minimalista e infantil.
```

```
La aplicación debe funcionar como un único ecosistema visual. No debe existir
ninguna pantalla que parezca pertenecer a otro diseño o que rompa la
consistencia general.
```

```
---
```

## `# 1. Sistema de Diseño Global` 

```
## Flujo Circadiano
```

```
Revisar completamente el sistema de colores.
```

```
Toda la aplicación debe adaptarse correctamente al flujo circadiano (día, tarde
y noche).
```

```
Esto implica que:
```

- `El background nunca debe verse gris, plomo u opaco.` 

- `Los colores deben mantener suficiente saturación y contraste en cualquier horario.` 

- `Cards, botones, iconos, textos y elementos decorativos deben cambiar de forma armónica dependiendo del ciclo circadiano.` 

- `Cada paleta debe sentirse diseñada intencionalmente y no como una mezcla aleatoria de colores.` 

- `Los colores deben conservar una identidad visual consistente en todas las pantallas.` 

```
---
```

## `## Backgrounds` 

```
Evaluar todos los fondos de la aplicación.
```

```
Actualmente algunos fondos lucen demasiado grises o con poco contraste.
```

```
Se debe:
```

- `Incrementar ligeramente la intensidad del color.` 

- `Mantener fondos suaves pero con mayor presencia visual.` 

- `Crear una mejor separación entre fondo y contenido.` 

- `Conservar siempre la identidad premium e infantil.` 

```
---
```

## `## Cards` 

```
Todas las cards de la aplicación deben cumplir las siguientes reglas:
```

- `Mantener exactamente la misma altura cuando pertenezcan a una misma sección.` 

- `Mantener el mismo radio de bordes.` 

- `Mantener la misma separación entre ellas.` 

- `Mantener una jerarquía visual consistente.` 

- `Sus colores deben combinar correctamente con el ciclo circadiano.` 

- `Ninguna card debe verse más pesada que otra si cumplen la misma función.` 

```
---
```

## `## Responsive Design` 

```
Revisar absolutamente todas las pantallas.
```

```
Actualmente existen elementos demasiado cerca de:
```

- `Barra de estado` 

- `Hora` 

- `WiFi` 

- `Cobertura` 

- `Cámara frontal` 

## `Corregir:` 

- `Safe Areas.` 

- `Márgenes superiores.` 

- `Márgenes laterales.` 

- `Espaciados.` 

- `Padding.` 

- `Adaptación para diferentes tamaños de pantalla.` 

```
Ningún elemento debe superponerse con componentes propios del sistema operativo.
```

```
---
```

- `# 2. Pantalla de Inicio` 

- `Eliminar los dos iconos actuales del inicio.` 

- `Diseñar un background mucho más coherente con el concepto general de AlphaKids.` 

- `El fondo debe transmitir un ambiente educativo, infantil y premium.` 

```
---
```

## `# 3. Pantalla "Bienvenido a AlphaKids"` 

## `## Cambios` 

- `Eliminar el icono adicional que aparece en esta pantalla.` 

- `Incrementar ligeramente el contraste del fondo para que tenga mayor presencia.` 

- `Mantener coherencia con el sistema circadiano.` 

```
---
```

```
# 4. Pantalla de Login
```

```
Actualmente el fondo luce demasiado gris.
```

```
Se debe:
```

- `Aumentar la intensidad del background.` 

- `Mejorar el contraste general.` 

```
Además:
```

```
Los TextFields pierden visibilidad.
```

## `Corregir:` 

- `Color del texto.` 

- `Placeholder.` 

- `Color de los botones.` 

- `Estados activos.` 

- `Estados inactivos.` 

```
Todo debe conservar excelente legibilidad durante cualquier cambio del ciclo
circadiano.
```

```
---
```

```
# 5. Pantalla "¿Quién va a usar AlphaKids?"
```

```
## Cards
```

```
Las tarjetas deben estar mejor proporcionadas.
```

```
Actualmente presentan problemas de equilibrio visual.
```

```
Corregir:
```

- `Tamaño.` 

- `Espaciado.` 

- `Distribución.` 

```
---
```

## `## Avatar` 

```
Actualmente aparece el avatar de Alphi.
```

```
Esto es incorrecto.
```

```
Debe renderizarse:
```

```
El avatar real del niño registrado.
```

```
Además:
```

- `Debe aparecer perfectamente centrado.` 

- `No debe deformarse.` 

```
---
```

## `## Forma del Avatar` 

```
Actualmente el contenedor es rectangular.
```

```
Evaluar cambiarlo por un diseño completamente circular.
```

```
La referencia visual debe inspirarse en interfaces como:
```

- `Netflix` 

- `Disney+` 

- `Plataformas modernas de perfiles` 

```
---
```

```
## Orden de perfiles
```

```
El orden debe ser:
```

`1. Perfil del padre.` 

`2. Perfiles de los hijos.` 

`3. Card para agregar nuevo perfil.` 

```
La última card debe utilizar un icono ilustrado de "Agregar perfil", el cual
```

```
será proporcionado posteriormente.
```

```
---
```

```
# 6. Panel del Padre
```

## `## Dashboard` 

```
Existe una card azul/celeste demasiado grande que desperdicia espacio mostrando
poca información.
```

```
Reducir considerablemente su tamaño.
```

```
Redistribuir mejor el contenido.
```

```
---
```

```
## Dashboard Inteligente
```

```
Actualmente no parece un dashboard.
```

```
Debe representar realmente toda la información proveniente de la API.
```

```
Evaluar incluir indicadores como:
```

```
* Hijos registrados.
```

```
* Actividades realizadas hoy.
```

```
* Tiempo de juego.
```

```
* Palabras aprendidas.
```

```
* Letras completadas.
```

```
* Juegos disponibles.
* Juegos completados.
* Nivel actual.
* XP acumulada.
* Monedas obtenidas.
```

```
* Progreso semanal.
* Racha diaria.
```

```
* Última actividad.
```

- `Próximos ejercicios sugeridos. * Estado general del aprendizaje.` 

```
Todo debe estar conectado con el flujo real de datos de la API.
```

```
---
```

## `## Pestaña Hijos` 

```
Actualmente carece de estructura.
```

```
Debe convertirse en un verdadero panel de administración de hijos.
```

```
Evaluar incluir:
```

```
* Lista de hijos.
* Avatar.
* Edad.
* Nivel.
* Última conexión.
* Progreso.
* Botón editar.
* Botón eliminar.
* Botón agregar hijo.
```

```
Todo debe seguir el flujo de la API.
```

```
---
```

## `# Navbar Superior del Panel del Padre` 

```
Actualmente presenta varios problemas.
```

## `## Problemas detectados` 

- `Icono hamburguesa.` 

- `Texto "Panel de Padres".` 

- `Botón Modo Niño.` 

- `Botón Configuración.` 

```
Todo se percibe visualmente desordenado.
```

```
---
```

## `## Rediseño` 

```
Proponer una distribución moderna.
```

```
Debe existir una jerarquía clara entre:
```

## `* Navegación.` 

- `Título.` 

- `Acciones.` 

```
Además:
```

```
La frase "Panel de Padres" nunca debe partirse en dos líneas.
```

```
Debe mantenerse siempre en una sola línea.
```

```
---
```

## `## Configuración` 

```
Mover "Términos y Condiciones" al menú desplegable de la configuración (icono de
engranaje).
```

```
---
```

```
# 7. Dashboard Principal del Niño
```

```
## Encabezado
```

```
Agregar:
```

- `Avatar del niño.` 

- `Nombre del niño.` 

```
Las monedas deben mantenerse.
```

```
Sin embargo:
```

- `Incrementar ligeramente el tamaño del icono de monedas.` 

- `Mejorar la jerarquía visual.` 

```
La tuerca junto a las monedas no aporta una función clara.
```

```
Evaluar eliminarla.
```

```
---
```

## `## Bienvenida` 

```
Eliminar la card que contiene:
```

```
"Bienvenido de vuelta".
```

```
Debe convertirse en un gran título integrado directamente al fondo.
```

```
---
```

## `## Mascota Alphi` 

```
Actualmente está dentro de una card.
```

```
Eliminar dicha card.
```

```
Alphi debe aparecer directamente sobre el background.
```

```
Además:
```

- `Aumentar su tamaño.` 

- `Darle mayor protagonismo.` 

```
---
```

## `## Cards del Inicio` 

```
Todas deben:
```

- `Tener exactamente el mismo tamaño.` 

- `Mantener consistencia visual.` 

- `Mantener armonía con el ciclo circadiano.` 

```
---
```

```
# 8. Vista "Jugar"
```

```
Actualmente el fondo luce gris.
```

```
Corregir:
```

- `Mayor intensidad de color.` 

- `Mayor identidad visual.` 

```
---
```

## `## Cabecera` 

```
Actualmente existen tres cards.
```

```
La primera no es necesaria.
```

```
Eliminarla.
```

```
Conservar únicamente:
```

- `Título.` 

- `Mensaje.` 

- `Mascota Alphi.` 

```
Todo debe verse más grande y mejor distribuido.
```

```
---
```

```
## Cards de Juegos
```

```
Eliminar:
```

- `Barra de monedas.` 

- `Indicador de dificultad ("Fácil").` 

```
Reestructurar todas las cards.
```

```
Mantener un diseño mucho más minimalista.
```

```
Además:
```

- `Aumentar ligeramente el tamaño del icono.` 

- `Aumentar el tamaño del nombre del juego.` 

```
Todas las cards deben conservar exactamente el mismo tamaño.
```

```
---
```

```
# 9. Vista "Elige una Palabra"
```

```
Hacer esta pantalla mucho más interactiva.
```

```
Debe renderizar:
```

```
La imagen asignada por el profesor.
```

```
Cada palabra debe mostrarse en una card individual.
```

```
La card puede ser:
```

- `Cuadrada.` 

- `Rectangular.` 

- `Bordes completamente redondeados.` 

```
Debe sentirse mucho más atractiva para niños.
```

```
---
```

## `## Responsive` 

```
Actualmente los elementos se mezclan con la barra superior del dispositivo.
```

```
Corregir:
```

- `Márgenes.` 

- `Safe Area.` 

- `Padding.` 

```
Esto debe aplicarse a toda la aplicación, no únicamente a esta pantalla.
```

```
---
```

```
# 10. Vista "Escaneo de Letras"
```

```
Actualmente existen:
```

- `Card superior.` 

- `Área de cámara.` 

- `Dos cards inferiores.` 

```
---
```

## `## Primera Card` 

```
Actualmente muestra:
```

- `Imagen.` 

- `Texto "Escanea las letras".` 

- `"Palabras y letras: 4".` 

```
Modificar:
```

```
Mostrar únicamente:
```

```
Cantidad de letras.
```

```
Eliminar cualquier información redundante.
```

```
---
```

## `## Esfera Blanca` 

```
Existe una esfera blanca cuya utilidad no es clara.
```

```
Evaluar eliminarla completamente.
```

```
---
```

```
## Indicadores
```

```
Eliminar:
```

```
* Intentos.
```

- `Detectadas.` 

```
Mantener únicamente:
```

```
Los audios de apoyo.
```

```
---
```

```
## Card "Busca las letras y colócalas en orden"
```

```
Actualmente el contenido se comprime demasiado.
```

```
Debe ser completamente responsive.
```

```
Si es necesario:
```

```
Reducir la longitud de la frase para mejorar la lectura infantil.
```

```
Debe mantener espacios adecuados y una correcta distribución.
```

```
---
```

```
## Background
```

```
El fondo vuelve a verse gris.
```

```
Corregir aumentando la intensidad del color.
```

```
Las cards, textos, iconos y títulos deben mantener coherencia completa con el
flujo circadiano.
```

```
---
```

```
# Revisión Final Obligatoria
```

```
Antes de finalizar la implementación realizar una auditoría completa del
aplicativo verificando:
```

- `Consistencia visual.` 

- `Sistema circadiano.` 

- `Responsive Design.` 

- `Safe Areas.` 

- `Jerarquía tipográfica.` 

- `Contraste.` 

- `Accesibilidad.` 

- `Tamaño de cards.` 

- `Espaciados.` 

- `Iconografía.` 

- `Coherencia con el flujo de la API.` 

- `Uniformidad entre todas las pantallas.` 

```
El objetivo final es que AlphaKids transmita la sensación de un producto
premium, infantil, moderno y completamente coherente, donde todas las pantallas
compartan el mismo lenguaje visual y de interacción.
```

---

# Plan de Ejecución por Fases

Ejecutar con Claude Code una fase a la vez. Compilar y verificar después de cada fase antes de pasar a la siguiente.

---

## Fase 1 — Sistema de Diseño Global

**Cubre:** Secciones 1 (Flujo Circadiano, Backgrounds, Cards, Responsive Design)

**Archivos a modificar:**
- Tema/colores circadianos
- Backgrounds en todas las pantallas
- Estilos de cards compartidos
- Safe areas y padding global

**Verificar:** Compilar sin errores. Revisar que backgrounds, cards, y circadian theme funcionen en día/tarde/noche.

---

## Fase 2 — Pantalla de Inicio + Bienvenida

**Cubre:** Secciones 2 y 3

**Referencia visual:** `images/inicio-alphi/1-inicio.png`

**Archivos a modificar:**
- Pantalla de inicio (Splash / Welcome)
- Pantalla "Bienvenido a AlphaKids"

**Verificar:** Compilar. La pantalla de inicio debe asemejarse visualmente a la imagen de referencia.

---

## Fase 3 — Login + Selector de Perfiles

**Cubre:** Secciones 4 y 5

**Icono para agregar perfil:** `images/agregar-perfil/agregar-perfil.jpeg`

**Archivos a modificar:**
- Pantalla de Login (textfields, botones, contraste)
- Pantalla "¿Quién va a usar AlphaKids?" (cards, avatar circular, orden de perfiles)

**Verificar:** Compilar. Avatar circular funcional, orden correcto (padre → hijos → agregar perfil), icono de agregar perfil visible.

---

## Fase 4 — Panel del Padre

**Cubre:** Sección 6 (Dashboard, Pestaña Hijos, Navbar, Configuración)

**Archivos a modificar:**
- Dashboard del padre
- Pestaña de hijos
- Navbar superior
- Configuración (mover Términos y Condiciones)

**Verificar:** Compilar. Navbar ordenado, dashboard con datos reales de API, pestaña hijos funcional.

---

## Fase 5 — Dashboard del Niño + Jugar + Elegir Palabra

**Cubre:** Secciones 7, 8 y 9

**Archivos a modificar:**
- Dashboard principal del niño (header, bienvenida, Alphi, cards)
- Vista "Jugar" (header, cards de juegos)
- Vista "Elige una Palabra" (cards interactivas, responsive)

**Verificar:** Compilar. Cards del mismo tamaño, Alphi sin card, fondo coherente con circadian.

---

## Fase 6 — Escaneo de Letras + Auditoría Final

**Cubre:** Sección 10 + Revisión Final Obligatoria

**Archivos a modificar:**
- Vista "Escaneo de Letras" (cards, esfera blanca, indicadores, responsive)

**Verificar:** Compilar. Luego ejecutar auditoría completa: consistencia visual, circadian, responsive, safe areas, tipografía, contraste, accesibilidad, cards, espaciados, iconografía, coherencia API, uniformidad entre pantallas.

