# Typography.md

## Fuentes del proyecto

El proyecto usa **dos fuentes variables** cargadas via `expect/actual`:

| Familia | Archivo | Uso |
|---------|---------|-----|
| **DynaPuff** | `DynaPuff/DynaPuff-VariableFont_wdth,wght.ttf` | `display`, `headline`, `title`, `label` → titulos, botones, encabezados |
| **DM Sans** | `DM_Sans/DMSans-VariableFont_opsz,wght.ttf` | `body`, `labelSmall` → cuerpo de texto, formularios, metricas |

> Originalmente el spec sugeria "Baloo 2 / Quicksand / Nunito". Se reemplazaron por DynaPuff (estilo redondeado infantil) + DM Sans (estilo limpio legible) para mejor compatibilidad con Compose Multiplatform.

**Carga en Android:** `sharedUI/src/androidMain/res/font/` via `R.font.dynapuff` / `R.font.dm_sans`
**Carga futura iOS:** via bundle (pendiente)
**Fallback:** `FontFamily.Default` si falla la carga

## Escala

| Token | Tamano | Peso | Fuente | Uso observado |
|-------|--------|------|--------|---------------|
| Display | 32-36sp | 800 (ExtraBold) | DynaPuff | "!Bienvenida a AlphaKids!", titulo de Splash |
| H1 | 26-28sp | 700 (Bold) | DynaPuff | Titulos de paso del wizard ("Creemos el perfil de tu hijo") |
| H2 | 20-22sp | 700 | DynaPuff | Nombres de perfil ("Sofia", "Lucas"), subtitulos de seccion |
| H3 | 16-18sp | 600 (SemiBold) | DynaPuff | Titulos de tarjeta ("Mi mascota", "Objetivo del dia") |
| Body | 14-15sp | 400-500 | DM Sans | Descripciones, textos de ayuda |
| Caption | 12-13sp | 400 | DM Sans | Notas legales, "Tu informacion siempre esta protegida" |
| Labels | 13sp | 600 | DM Sans | Etiquetas de input ("Correo electronico", "Contrasena") |
| Button Text | 16sp | 700 | DynaPuff | Title Case, sin uppercase ("Continuar", "Crear mi cuenta") |

## Color de texto por contexto
- Sobre fondo claro (cards blancas): Deep Navy `#1E2749`
- Sobre fondo de gradiente saturado: blanco `#FFFFFF`, drop-shadow sutil opcional
- Texto secundario: Slate Gray `#5B6478`
- Links: Primary Blue `#3B7DF6`
