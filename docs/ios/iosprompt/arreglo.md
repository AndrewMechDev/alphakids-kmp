# Prompt: Reestructuración de navegación en AdventureHome (antes de Dashboard)

## Contexto
Ya se confirmó que la navegación real en Compose no es un NavHost
anidado, sino un modelo híbrido:
- Dictionary: navegación por estado (`showDictionary` bool) que
  reemplaza el contenido DENTRO del mismo Scaffold — el tab bar
  permanece visible, con botón "Volver" que solo cambia el bool.
- LearningAdventureHub y todo lo que cuelga de él (WordSelection →
  WordScannerChallenge → OCRResult): push real al NavController raíz de
  la app — el tab bar desaparece por completo, navegación de pantalla
  completa con back nativo.
- Salir de AdventureHome: diálogo de confirmación → vuelve a
  WelcomeSelection con popUpTo(0) (equivalente a limpiar el stack).

Antes de implementar el contenido real de Dashboard, hay que ajustar
`AdventureHomeScreen.swift` para que soporte este modelo de navegación
de dos niveles.

## Alcance de este prompt (NO tocar nada fuera de esto)
- Solo trabajar dentro de `iosApp/`.
- NO tocar `sharedLogic/` ni `sharedUI/` bajo ninguna circunstancia.
- NO tocar `GameSession.kt`, `AuthRepository.kt`,
  `AlphaKidsApiClient.kt`, ni ningún DTO.

## Tareas

### 1. Definir dónde vive el NavigationStack raíz
- Confirma si el `NavigationStack` actual está en `ContentView.swift`
  envolviendo todo (incluyendo el `TabView` de AdventureHome). Si no es
  así, muévelo para que sea el `NavigationStack` raíz de la app, con el
  `TabView` como una de las vistas dentro de ese stack — esto es
  necesario para que el push a LearningAdventureHub tape el tab bar
  correctamente (en SwiftUI, un push dentro del NavigationStack que
  contiene al TabView oculta el TabView automáticamente; no hace falta
  sheet ni fullScreenCover).

### 2. Dictionary como overlay dentro del tab, NO como sheet
- Implementar Dictionary con el mismo patrón que Compose: un estado
  booleano (`@State private var showDictionary`) dentro de
  `AdventureHomeScreen`, que condicionalmente reemplaza el contenido del
  tab "Inicio" (ej. con un `if/else` o `ZStack` con transición simple).
- NO uses `.sheet()` ni `.fullScreenCover()` para esto — el
  comportamiento real es que el tab bar se mantiene visible, y esos
  modificadores de SwiftUI lo tapan por defecto. Confirma en tu
  respuesta final que el tab bar sigue visible cuando
  `showDictionary == true`.
- Placeholder de `DictionaryScreen.swift` por ahora (contenido real es
  la Pantalla 7, prompt aparte). Solo necesitas el mecanismo de
  navegación funcionando con un placeholder simple + botón "Volver".

### 3. LearningAdventureHub como push al stack raíz
- La card "¡A Jugar!" en Dashboard debe llamar
  `path.append(AppRoute.learningAdventureHub)` (o el nombre de ruta que
  uses) sobre el `NavigationStack` raíz identificado en el paso 1, no
  sobre un stack local del tab.
- Agrega `.learningAdventureHub` a `AppRoute` con un placeholder simple
  por ahora (contenido real es la Pantalla 8, prompt aparte).
- Confirma que al hacer push, el `TabView` (y su tab bar) deja de verse,
  y que el back nativo de iOS (swipe o botón) regresa correctamente al
  Dashboard con el tab bar visible de nuevo.

### 4. Salir de AdventureHome
- Agregar el mecanismo para "salir" de AdventureHome: un diálogo de
  confirmación (`.confirmationDialog` o `.alert`) accesible desde algún
  punto del tab "Inicio" (revisa dónde vive ese botón en el Compose
  real — probablemente en el header o un ícono de logout), que al
  confirmar resetea el `NavigationPath` del stack raíz a estado inicial
  (equivalente a `popUpTo(0)`) y navega a `.login` (no hay
  `WelcomeSelection` todavía en el plan de iOS; usa `.login` como
  destino por ahora, o pregúntame si prefieres otra ruta).

## Formato de salida
Al terminar, dame un resumen con:
- Confirmación explícita de que el tab bar se mantiene visible en
  Dictionary y desaparece en LearningAdventureHub (probado
  conceptualmente, ya que no hay compilación posible en Windows).
- Lista de archivos creados/modificados con su ruta completa.
- Estructura final de `AppRoute` actualizada.
- Cualquier ambigüedad o decisión que hayas tenido que tomar sin
  confirmación (ej. destino de "salir de AdventureHome").