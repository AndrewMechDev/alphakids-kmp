# Prompt: Auditoría Inicial del Proyecto KMP (Kotlin Multiplatform)

## Contexto
Este es un proyecto Kotlin Multiplatform (KMP) donde un compañero ya avanzó
la app Android. Yo (otro desarrollador) me haré cargo exclusivamente de la
parte iOS. Antes de escribir cualquier código, necesito un reporte de
auditoría completo y detallado del estado actual del repositorio para
evitar conflictos con el módulo `shared` y entender las decisiones ya
tomadas.

NO modifiques ningún archivo. Este es un prompt de solo lectura /
diagnóstico.

## Qué necesito que audites y reportes

### 1. Estructura general del repositorio
- Lista todos los módulos/carpetas de primer nivel (ej. `shared`,
  `androidApp`, `iosApp`, `composeApp`, etc.)
- Indica el archivo de configuración de build usado en cada módulo
  (`build.gradle.kts`, `Package.swift`, `.podspec`, etc.)
- Estructura interna del módulo `shared` (carpetas `commonMain`,
  `androidMain`, `iosMain`, `commonTest`, etc.)

### 2. Integración iOS actual
- ¿Cómo se integra el módulo `shared` con iOS? (CocoaPods, XCFramework
  exportado manualmente, Swift Package Manager, plugin
  kotlin-multiplatform-mobile, etc.) Cita el archivo de configuración
  exacto donde se define esto.
- ¿Existe ya una carpeta/proyecto `iosApp` con algo de código (Swift/
  SwiftUI) o está vacío/placeholder?
- ¿El proyecto usa Compose Multiplatform para iOS (UI compartida) o UI
  nativa en SwiftUI consumiendo solo lógica de `shared`?

### 3. `expect`/`actual` — inventario completo
- Lista TODAS las declaraciones `expect` en `commonMain` con su ruta de
  archivo.
- Para cada una, indica si ya tiene su `actual` implementado en
  `androidMain`, en `iosMain`, en ambos, o en ninguno.
- Señala explícitamente cuáles `actual` de `iosMain` faltan por
  implementar (esto es crítico para mi trabajo).

### 4. Dependencias del `shared`
- Lista las dependencias declaradas en el `build.gradle.kts` del módulo
  `shared`, separadas por sourceSet (`commonMain`, `androidMain`,
  `iosMain`).
- Señala si hay alguna dependencia que sea Android-only y esté mal
  ubicada en `commonMain` (esto rompería la compilación iOS).

### 5. Arquitectura y convenciones ya establecidas
- Patrón de arquitectura usado (MVVM, MVI, Clean Architecture, etc.) si
  es identificable.
- Convenciones de nombres de paquetes, clases, y organización de
  carpetas.
- Manejo de red (Ktor, otro), manejo de errores/excepciones, y manejo de
  estado (StateFlow, Flow, etc.) en `commonMain`.
- Cualquier convención de inyección de dependencias (Koin, manual, etc.)

### 6. Puntos de riesgo / conflicto
- Identifica archivos en `shared/commonMain` que probablemente necesiten
  cambios para soportar iOS pero que también use la app Android (riesgo
  de romper el trabajo de mi compañero).
- Indica si hay `TODO`, `FIXME`, o comentarios relevantes dejados por mi
  compañero relacionados con iOS.
- Indica el estado del control de versiones: ¿hay una rama específica
  para iOS o se trabaja todo sobre `main`/`develop`?

### 7. Entorno y herramientas
- Versión de Kotlin y del plugin KMP usados (revisa
  `gradle/libs.versions.toml` o el `build.gradle.kts` raíz).
- Indica si el proyecto requiere Xcode para compilar el target iOS o si
  hasta ahora todo se ha manejado solo a nivel Kotlin/Gradle.

## Formato de salida
Devuélveme el reporte en Markdown, con las secciones numeradas igual que
arriba, usando bloques de código para citar rutas de archivos y
fragmentos relevantes. Al final, agrega una sección
**"Recomendaciones antes de empezar iOS"** con tu propio análisis de
riesgos y sugerencias de orden de trabajo.