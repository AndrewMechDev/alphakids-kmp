# 06_Screens — Estado de documentación

> **Nota:** Los specs de pantallas en esta carpeta representan la visión de diseño original.
> La implementación real ha evolucionado durante las 6 fases de mejora UX/UI (Julio 2026).
> Para el estado actual detallado, ver [`README.md`](../../README.md) y
> [`ALPHAKIDS-KMP-V2-Flujo.md`](../ALPHAKIDS-KMP-V2-Flujo.md).

## Pantallas documentadas (con referencia visual — specs originales)
- Splash
- ParentLogin
- ParentRegister
- ChildCreationWizard (incluye intro parental + 5 pasos)
- ChildProfileSelector
- AdventureHome

## Pantallas implementadas que NO tienen spec visual propio
Estas pantallas existen en el código pero no tienen un `.md` individual en esta carpeta
porque su diseño se definió durante la implementación o en fases UX/UI posteriores:

### Ya implementadas
- **LearningAdventureHub** — Hub de juegos con gradientes + Alphi sin card (Fase 5)
- **OCRChallenge** (`WordScannerChallenge`) — Escáner de letras con cámara + ML Kit (simplificado Fase 6)
- **ResultsScreen** (`OCRResultScreen`) — Resultado del escaneo con monedas, XP, estrellas
- **WordTreasureChest** (`DictionaryScreen`) — Diccionario con scroll-sync A–Z, filtros, búsqueda (rediseñado Fase 5)
- **ParentDashboard** (`ParentHomeScreen` + `ParentInsightCenter`) — Dashboard con AggregatedStats reales del API (Fase 4)
- **ParentChildDetail** (`ChildDetailScreen`) — Detalle individual del hijo
- **SubscriptionCenter** (`SubscriptionScreen`) — Plan y beneficios
- **Settings** — Configuración (gear en navbar del panel de padres)
- **PetMarket** → Integrado en `StoreScreen` (Tab 2 de AdventureHome)
- **PetKingdom** → Integrado en `PetsScreen` (Tab 3 de AdventureHome)
- **HallOfChampions** → Integrado en `AchievementsScreen` (sub-sección de Inicio)

### Pendientes reales (no implementadas)
- **SpellingChallenge** (`SpellingAdventure`) — Deletreo con STT (pendiente)
- **ChildProfileSelector nativo** — Reemplazado por `NetflixProfilesScreen` con avatares circulares (Fase 3)

> Los specs escritos describen la visión original de producto. Para el estado actual
> de implementación, revisar el diagrama de flujo en `ALPHAKIDS-KMP-V2-Flujo.md` o el README principal.
