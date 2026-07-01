# AlphaKids Product Spec — 04, 05, 06, 07 (extracción visual)

## Qué es esto
Este paquete completa, a partir de evidencia visual (10 mockups), las carpetas `04_Design_Language`, `05_Component_Library`, `06_Screens` y `07_User_Flows` definidas en `ALPHAKIDS_PRODUCT_SPEC_V3.md`. Está escrito para ser consumido tanto por humanos como por agentes/subagentes (Claude Code, skills, MCP) en un flujo SDD (spec-driven development): cada archivo es autocontenido, usa nomenclatura consistente con el spec original y marca explícitamente qué fue **confirmado visualmente** vs. **inferido** vs. **pendiente**.

## Metodología
1. Se inspeccionaron 10 capturas de pantalla (Splash, Login, Registro, Onboarding parental, 3 pasos del wizard de creación de hijo, pantalla de bienvenida final, selector de perfil, home del niño).
2. Se extrajeron tokens visuales (color, tipografía, radios, sombras, espaciados, gradientes) y se documentaron como HEX/valores estimados — **no son valores exactos de Figma**, sino una lectura fiel de las imágenes. Deben validarse contra archivos fuente antes de codificarse como design tokens definitivos.
3. Se documentaron componentes reutilizables siguiendo la plantilla oficial del spec (Propósito / Variantes / Estados / Uso permitido / Uso prohibido / Navegación asociada).
4. Se documentaron las 6 pantallas con evidencia visual siguiendo la plantilla oficial (Objetivo / Usuario / Contenido / Secciones / Componentes / Estados / Navegación / Eventos / Analytics).
5. Se documentaron 4 flujos de usuario inferidos de la secuencia de pantallas disponible.

## Hallazgo importante a resolver con el equipo de producto
⚠️ **Numeración de pasos duplicada en el wizard:** la pantalla "Asistente de configuración para padres" y la pantalla "Creemos el perfil de tu hijo" muestran ambas "Paso 1 de 5" con contenidos distintos (misión informativa vs. captura de datos). Ver detalle y recomendación en `06_Screens/ChildCreationWizard/ChildCreationWizard.md`.

## Cobertura actual vs. pendiente

| Sección | Cobertura |
|---|---|
| `04_Design_Language` | ✅ 10/10 archivos completos (con notas de validación donde falta evidencia, ej. Premium) |
| `05_Component_Library` | ✅ 19/19 archivos completos — varios marcados ⏳ pendientes de referencia (Dialogs, BottomSheets, WordCards, AchievementCards, EmptyStates, PremiumComponents) |
| `06_Screens` | ✅ 6/17 pantallas documentadas con evidencia real (Splash, ParentLogin, ParentRegister, ChildCreationWizard, ChildProfileSelector, AdventureHome). 11 pantallas restantes listadas en `06_Screens/README.md`, no creadas para no inventar contenido sin evidencia |
| `07_User_Flows` | ✅ 4/12 flujos documentados (los que cubre el material disponible) |

## Recomendación de siguiente paso
Para cerrar `06_Screens` y `07_User_Flows` al 100%, se necesitan capturas de: LearningAdventureHub, OCRChallenge, SpellingChallenge, ResultsScreen, WordTreasureChest, PetKingdom, PetMarket, HallOfChampions, ParentDashboard, ParentChildDetail, SubscriptionCenter, Settings — junto con cualquier modal/diálogo y estado de error existente, ya que ninguno apareció en el set actual.

Cuando trabajes esto con agents/subagentes vía MCP, cada `.md` puede cargarse como contexto independiente por tarea (ej. un subagente de "component implementation" solo necesita `05_Component_Library/*` + `04_Design_Language/*`; un subagente de "screen scaffolding" necesita además el `.md` de la pantalla específica en `06_Screens/`).
