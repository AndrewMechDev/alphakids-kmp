# Prompts para Claude Code — Replicar Android → iOS

> **Objetivo**: Prompts listos para copiar/pegar en Claude Code para implementar la réplica de Android a iOS en SwiftUI.
>
> **Referencia**: `requirements/ios-replication-spec.md` — especificación completa pantalla por pantalla
> **Skill**: Load `alpha-ios-swift` + `alpha-general` antes de ejecutar cada prompt

---

## 📋 Estructura de los Prompts

Los prompts están divididos en **2 líneas de trabajo paralelas**:

| Línea | Contenido | Puede empezar |
|-------|-----------|---------------|
| **🧱 A — Infraestructura** | Bridges Koin→Swift, Style modifiers, Services | Ahora mismo |
| **🎨 B — UI/UX** | Screens, ViewModels, componentes | Después de que A esté listo |

Cada fase de UI/UX depende de los bridges de A, pero A no depende de nada.

---

## 🧱 PROMPT A1: Bridges Koin→Swift (Infraestructura)

> **Para Claude Code**: Completar y crear los bridges async que conectan sharedLogic (Kotlin KMP) con SwiftUI.

```
Eres un ingeniero iOS con experiencia en KMP y Koin. Trabajas en AlphaKids, una app educativa KMP donde iOS consume el framework SharedLogic (Kotlin compilado a XCFramework).

Tu objetivo es COMPLETAR y CREAR los bridges async Koin→Swift en el proyecto.

## Contexto del proyecto
- Ruta del proyecto: /cwd/alphakids-kmp
- Rama de trabajo: feature/ios-correccion
- Ya existe SharedLogic.framework y Koin está inicializado en iOSApp.swift
- Patrón de bridge existente: AuthRepository+Async.swift en iosApp/iosApp/Bridge/
- Los repositorios Kotlin se resuelven con: InitKoinKt.getXxxRepository() o AppKoin.shared.koin.get()

## Archivos a modificar/crear

### 1. ParentRepository+Async.swift (COMPLETAR — existen 1/8 métodos)
Ruta: iosApp/iosApp/Bridge/ParentRepository+Async.swift
Agregar estos métodos async, usando el MISMO patrón de AuthRepository+Async:
- getChildStats(childId: String) -> ChildStats?
- getRecentActivity() -> [ChildActivity]
- getSubscription() -> SubscriptionInfo?
- getFAQs() -> [FAQItem]
- submitContactForm(name: String, email: String, message: String) -> Bool
- getPublicInstitutions() -> [PublicInstitutionDto]
- createChild(firstName: String, lastName: String, avatarUrl: String?, institutionId: String?, sectionId: String?) -> CreateChildResult?

### 2. GameRepository+Async.swift (COMPLETAR — existen 2/4 métodos)
Ruta: iosApp/iosApp/Bridge/GameRepository+Async.swift
Agregar:
- completeSession(studentId: String, wordId: String?, gameType: String, status: String, attempts: Int32, coinsEarned: Int32, starsEarned: Int32) -> GameSessionResultDto?
- getAchievements(studentId: String) -> AchievementsResponseDto?

### 3. StoreRepository+Async.swift (CREAR NUEVO)
Ruta: iosApp/iosApp/Bridge/StoreRepository+Async.swift
- getPetsCatalog(studentId: String) -> [PetCatalogDto]
- getAccessoriesCatalog(studentId: String) -> [AccessoryCatalogDto]
- buyPet(studentId: String, petId: String) -> BuyPetResponseDto?
- buyAccessory(studentId: String, accessoryId: String) -> BuyAccessoryResponseDto?

### 4. StudentPetRepository+Async.swift (CREAR NUEVO)
Ruta: iosApp/iosApp/Bridge/StudentPetRepository+Async.swift
- getPets(studentId: String) -> [StudentPetDto]
- feedPet(petId: String, foodId: String) -> FeedPetResponseDto?

## Patrón a seguir EXACTAMENTE (de AuthRepository+Async):

```swift
// Cada método sigue este patrón:
static func nombreMetodo(param: String) async throws -> TipoRetorno {
    return try await withCheckedThrowingContinuation { continuation in
        repository.metodoKotlin(param: param) { result, error in
            if let error = error {
                continuation.resume(throwing: error)
            } else if let result = result {
                continuation.resume(returning: result as! TipoRetorno)
            } else {
                continuation.resume(
                    throwing: NSError(
                        domain: "Repositorio",
                        code: -1,
                        userInfo: [NSLocalizedDescriptionKey: "método returned nil"]
                    )
                )
            }
        }
    }
}
```

## Resolución del repositorio Koin:

Cada bridge usa un static var repository, Ejemplo:
```swift
static var repository: SharedLogic.ParentRepository {
    return InitKoinKt.getParentRepository() // o AppKoin.shared.koin.get(...)
}
```

Verificar en InitKoin.kt qué funciones exporta para cada repositorio.

## Verificación:
- Todos los archivos compilan sin errores en Xcode
- Los métodos async pueden ser llamados con `try await` desde cualquier ViewModel
- Los singletons de sharedLogic (SessionManager, GameSessionState, GameProgressManager, AchievementAnalytics) son accesibles directamente desde Swift sin wrapper
```

---

## 🧱 PROMPT A2: Style Modifiers + Services (Infraestructura)

> **Para Claude Code**: Crear los modifiers de estilo (Glassmorphism, Circadian, Fonts) y los servicios nativos iOS (OCR, Audio, TTS).

```
Eres un ingeniero iOS con experiencia en SwiftUI y diseño visual. Trabajas en AlphaKids KMP.

Crea los archivos de estilo y servicios base en el proyecto iOS.

## Contexto
- Ruta: /cwd/alphakids-kmp
- Rama: feature/ios-correccion
- Design system completo en: requirements/alphakids-product-spec/04_Design_Language/ (Colors, Shadows, Gradients, Radius, Typography, etc.)

## Archivos a crear

### 1. Style/GlassmorphismModifier.swift
Ruta: iosApp/iosApp/Style/GlassmorphismModifier.swift

```swift
import SwiftUI

struct GlassBackground: ViewModifier {
    let tint: Color
    let opacity: Double

    func body(content: Content) -> some View {
        content
            .background(
                ZStack {
                    tint.opacity(opacity)
                    Rectangle().fill(.ultraThinMaterial)
                }
                .clipShape(RoundedRectangle(cornerRadius: 20))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(.white.opacity(0.25), lineWidth: 1)
            )
            .shadow(color: .black.opacity(0.15), radius: 15, x: 0, y: 8)
    }
}

extension View {
    func glassBackground(tint: Color = .white, opacity: Double = 0.3) -> some View {
        modifier(GlassBackground(tint: tint, opacity: opacity))
    }
}
```

### 2. Style/CircadianTheme.swift
Ruta: iosApp/iosApp/Style/CircadianTheme.swift

- Enum CircadianPeriod: morning(06-11), afternoon(12-18), evening(19-20), night(21-05)
- static func current() -> CircadianPeriod
- var gradientColors: [Color] con colores del spec 04_Design_Language
- ViewModifier CircadianBackground con LinearGradient de fondo
- Gradientes: Morning warm, Afternoon blue, Evening purple, Night dark indigo
- Apply .preferredColorScheme(.dark) solo en night

### 3. Style/FontExtensions.swift
Ruta: iosApp/iosApp/Style/FontExtensions.swift

- Extensiones de Font para DynaPuff (titles) y DM Sans (body)
- Usar .custom("DynaPuff", size: ...) y .custom("DM Sans", size: ...)
- Si las fonts no están en el bundle, agregar los archivos .otf a Info.plist como UIAppFonts
  - Fuentes en: /cwd/DM_Sans/ y /cwd/DynaPuff/

### 4. Services/VisionOcrService.swift
Ruta: iosApp/iosApp/Services/VisionOcrService.swift

```swift
import Vision

class VisionOcrService {
    func recognizeText(in image: UIImage) async throws -> String {
        guard let cgImage = image.cgImage else { throw OcrError.invalidImage }
        let request = VNRecognizeTextRequest()
        request.recognitionLevel = .accurate
        request.recognitionLanguages = ["es-PE", "en-US"]
        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        try handler.perform([request])
        return request.results?
            .compactMap { $0.topCandidates(1).first?.string }
            .joined(separator: " ") ?? ""
    }
}

enum OcrError: Error {
    case invalidImage
    case noTextFound
}
```

### 5. Services/AudioPlayerService.swift
Ruta: iosApp/iosApp/Services/AudioPlayerService.swift
- AVPlayer wrapper para URLs de Cloudinary
- Métodos: play(url: String), stop()

### 6. Services/TtsService.swift
Ruta: iosApp/iosApp/Services/TtsService.swift
- AVSpeechSynthesizer wrapper
- Método: speak(_ text: String, language: String = "es-PE")
- Pitch: 1.2, Rate: 0.55 (voz amigable para niños)

## Nota:
- No modificar el project.pbxproj a mano — los archivos nuevos se agregan desde Xcode
- Si las fonts no están disponibles, usar system fonts como fallback
- Todos los archivos deben compilar en Xcode con iOS 16.0+
```

---

## 🎨 PROMPT B1: Wizard de Onboarding (6 pantallas)

> **Para Claude Code**: Implementar el flujo completo de onboarding wizard en SwiftUI.

```
Eres un ingeniero iOS con experiencia en SwiftUI. Implementa el flujo de onboarding de 6 pasos (Wizard) para AlphaKids.

## Contexto
- Ruta: /cwd/alphakids-kmp
- Rama: feature/ios-correccion
- Bridges ya completados (ParentRepositoryAsync ya tiene createChild y getPublicInstitutions)
- Style modifiers ya creados (GlassmorphismModifier, CircadianTheme, FontExtensions)
- AppRoute en ContentView.swift tiene las rutas: .splash, .login, .register, .netflixProfiles, .adventureHome
- Referencia de diseño: requirements/alphakids-product-spec/ (04_Design_Language, 05_Component_Library)
- Skill: alpha-ios-swift (tiene patrones de código, bridge usage, glassmorphism)

## Estructura a crear

Crea la carpeta: iosApp/iosApp/Screens/Wizard/ con estos archivos:

### 1. WizardViewModel.swift (COMPARTIDO entre 6 pantallas)
@MainActor class WizardViewModel: ObservableObject
- @Published var: currentStep (WizardStep enum), childName, childAge (Int, default 4), avatarSeed, avatarStyle, selectedPetId, petName, institutionId, institutionName, gradeId, sectionId
- WizardStep enum: welcome, createChild, chooseAvatar, assignInstitution, chooseFirstPet, welcomeAdventure
- Métodos: nextStep(), previousStep(), canGoNext() -> Bool
- createChildProfile() async -> Bool (llama ParentRepositoryAsync.createChild)

### 2. SetupWizardScreen.swift (Paso 1)
- 4 benefit cards con Alphi
- Header con step indicator "Paso 1 de 6"
- "Comenzar configuración" CTA
- Glassmorphism cards, gradient background sky

### 3. CreateChildScreen.swift (Paso 2)
- Form: nombre + edad (Picker 2-12) + preview avatar DiceBear
- Guarda en WizardViewModel
- Navega → ChooseAvatar

### 4. ChooseAvatarScreen.swift (Paso 3)
- Grid DiceBear por categorías (Animals, Explorers, Fantasy como tabs)
- LazyVGrid con AsyncImage de DiceBear URL
- Avatar seleccionado con borde highlight
- Categorías como segment picker

### 5. AssignInstitutionScreen.swift (Paso 4, OPCIONAL)
- "¿Perteneces a un colegio?" con toggle Sí/No
- Si Sí: search bar + lista de instituciones desde API (ParentRepositoryAsync.getPublicInstitutions)
- Selección de grado + sección (opcional)
- Botón "Omitir" y "Continuar"

### 6. ChooseFirstPetScreen.swift (Paso 5)
- 3 mascotas starter: Inti Sol (naranja), Piedra Doce (verde), Triángulo (cyan)
- Cards con imagen + nombre + descripción
- Al seleccionar: sheet modal para poner nombre a la mascota
- Botón "Continuar"

### 7. WelcomeAdventureScreen.swift (Paso 6)
- Celebración: avatar + mascota elegida + stats iniciales (50 monedas, Nivel 1, "Semillita")
- Botón "Ir al inicio" → llama createChildProfile() y navega a adventureHome

### 8. Actualizar ContentView.swift
Agregar .wizard a AppRoute enum
Agregar navigationDestination para .wizard → SetupWizardScreen

## Patrón visual para todas las pantallas:
- Fondo: gradient sky (#4FA8F0 → #C9B8F5) con .ignoresSafeArea()
- Cards: .glassBackground() con .padding(16)
- Botón CTA: Color(hex: "3B7DF6"), full radius pill, floating shadow
- Títulos: DynaPuff, body: DM Sans
- Step indicator en Header: círculos numerados, active = filled, inactive = outline

## Verificación:
- Cada paso guarda datos en WizardViewModel
- createChildProfile() llama a la API con los datos acumulados
- Navegación correcta: WelcomeAdventure → AdventureHome (popToRoot inclusive)
- Back navigation en cada paso funciona correctamente
```

---

## 🎨 PROMPT B2: Tienda + Mascotas + Logros

> **Para Claude Code**: Implementar las 3 tabs faltantes de AdventureHome.

```
Eres un ingeniero iOS con experiencia en SwiftUI. Implementa las pantallas de Tienda, Mascotas y Logros para la Home de AlphaKids.

## Contexto
- Ruta: /cwd/alphakids-kmp
- Rama: feature/ios-correccion
- Bridges: StoreRepositoryAsync y StudentPetRepositoryAsync ya creados
- GameProgressManager singleton accesible desde sharedLogic
- AdventureHomeScreen.swift ya existe con 4 tabs (Inicio funciona, 3 tabs son placeholder)
- AppRoute: .adventureHome lleva a AdventureHomeScreen
- Referencia: requirements/ios-replication-spec.md sección 3.9, 3.10, 3.11

## Archivos a crear/modificar

### 1. AdventureHome/StoreScreen.swift + StoreViewModel.swift
StoreViewModel:
- @Published var: catalog, inventory, selectedCategory, pendingPurchase, coins
- loadCatalog(studentId:) async, buyItem() async

StoreScreen:
- Header: monedas (CoinDisplay) + botón inventario (sheet)
- Category tabs: Mascotas | Alimentos | Accesorios
- LazyVGrid de ProductCard con: imagen, nombre, precio (🪙), nivel requerido (badge), botón "Comprar"
- PurchaseConfirmationDialog: "¿Comprar {item} por {precio} monedas?"
- InventorySheet: grid de items comprados
- Usar GameProgressManager.inventory, addToInventory(), spendCoins()

### 2. AdventureHome/PetsScreen.swift + PetsViewModel.swift
PetsViewModel:
- @Published var: pets, activePet, selectedSubTab
- loadPets(studentId:) async, feedPet() async

PetsScreen:
- Sub-tabs: "Mis Mascotas" | "Tienda" (segmented picker)
- ActivePetCard: imagen grande (mascota_inti_sol/piedra_doce/triangulo) + nombre + stats (hambre, felicidad, energía como ProgressView)
- Otras mascotas: ScrollView horizontal con PetCards pequeñas
- Acciones: "Alimentar", "Jugar", "Interactuar" como botones
- Tienda sub-tab: grid de mascotas para comprar (desbloqueadas/gris+bloqueadas)

### 3. AdventureHome/AchievementsScreen.swift + AchievementsViewModel.swift
AchievementsViewModel:
- @Published var: uiState (enum: loading, error(String), success(AchievementData))
- loadData(studentId:) async, retry()
- AchievementData: ranks, level, xp, xpToNext, trophies, stats, history
- Modelos: RankDef, TrophyStatus, StatItem, HistoryEntry (desde sharedLogic)

AchievementsScreen:
- 4 sub-tabs con segmented picker:
  - Rangos: CurrentRankCard (avatar + nivel + XP bar) + lista de todos los ranks (lock/unlock)
  - Trofeos: LazyVGrid de TrophyCards con progress circle + nombre
  - Estadísticas: LazyVGrid de StatCards (palabras, OCR, deletreo, tiempo, monedas, estrellas)
  - Historial: Timeline con HistoryEntry items (fecha, descripción, emoji)

### 4. Modificar AdventureHomeScreen.swift
Reemplazar los 3 placeholders (Store/Pets/Achievements) con las vistas reales:

```swift
Tab("Tienda", systemImage: "cart") {
    StoreScreen()
}
Tab("Mascotas", systemImage: "pawprint") {
    PetsScreen()
}
Tab("Logros", systemImage: "trophy") {
    AchievementsScreen()
}
```

## Patrón visual:
- Misma estructura que DashboardContent (mismo fondo, glassmorphism)
- Cards: .glassBackground(tint: .white, opacity: 0.3)
- CoinDisplay: HStack con 🪙 + Text(coins) con CoinGold
- XPBar: ProgressView con tint gradiente #6C5CE7 → #8B7CF6
- Estados: Loading (ProgressView), Error (botón reintentar), Empty (EmptyStateView)
```

---

## 🎨 PROMPT B3: Diccionario

> **Para Claude Code**: Implementar el diccionario con alphabet navigator A–Z estilo Apple Music.

```
Eres un ingeniero iOS con experiencia en SwiftUI. Implementa la pantalla de Diccionario con alphabet navigator A–Z.

## Contexto
- Ruta: /cwd/alphakids-kmp
- Rama: feature/ios-correccion
- Bridge: GameRepositoryAsync.getDictionary() y getPlayableWords() ya funcionan
- Dictionary screen es un overlay inline desde DashboardContent
- Referencia: requirements/ios-replication-spec.md sección 3.8
- Referencia: requirements/gemini-scroll-sync-alphabet.md (spec específica del alphabet scroll)
- Referencia Android: sharedUI/.../home/DictionaryScreen.kt (489 líneas)

## Archivos a crear

### 1. Dictionary/DictionaryViewModel.swift
@MainActor class DictionaryViewModel: ObservableObject
- @Published var: words, filteredWords, selectedLetter (String?), searchQuery, activeFilter, selectedWord
- Filter enum: all, learned, pending, easy, hard
- loadData(studentId:) async (llama getPlayableWords + getDictionary, mergea resultados)
- filteredWords computed: aplica searchQuery + filter + selectedLetter

### 2. Dictionary/DictionaryScreen.swift
- Overlay inline (NavigationStack con .toolbar para "Cerrar")
- **Alphabet Navigator**: columna vertical A–Z a la derecha (ScrollViewReader)
  - Drag gesture: scroll a la letra
  - Tap: scroll a la letra
  - Letra activa resaltada
  - Estilo Apple Music wheel
- **Search bar** arriba con .searchable()
- **Filter chips** horizontales: Todas | Aprendidas | Pendientes | Fáciles | Difíciles
- **Word grid**: LazyVGrid 2 columnas de DictionaryWordCard
  - AsyncImage con Cloudinary URL (o placeholder letra)
  - Nombre de la palabra
  - Botón audio 🔊 (llama AudioPlayerService.play)
- **Word detail**: al seleccionar palabra, mostrar card inline más grande con imagen + audio + dificultad

### 3. Dictionary/DictionaryWordCard.swift (componente)
```swift
struct DictionaryWordCard: View {
    let word: WordDto
    let audioService: AudioPlayerService

    var body: some View {
        VStack(spacing: 6) {
            AsyncImage(url: word.imageUrl) { phase in
                switch phase {
                case .success(let img): img.resizable().scaledToFill()
                default: letterPlaceholder
                }
            }
            .frame(width: 64, height: 64)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            Text(word.text).font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
            Button(action: { audioService.play(url: word.audioUrl) }) {
                Image(systemName: "speaker.wave.2.fill").font(.caption)
            }
        }
        .padding(10)
        .glassBackground()
    }
}
```

### 4. Modificar DashboardContent en AdventureHomeScreen.swift
El card "Diccionario" actual abre un placeholder. Reemplazar con:
```swift
.sheet(isPresented: $showDictionary) {
    DictionaryScreen()
}
```

## Verificación:
- Scroll a letra funciona con drag gesture
- Search filtra correctamente
- Filter chips cambian el set de palabras
- Audio reproduce desde Cloudinary URLs
- AsyncImage carga imágenes o muestra placeholder
```

---

## 🎨 PROMPT B4: Flujo de Juego OCR (LearningAdventureHub → WordSelection → Scanner → Result)

> **Para Claude Code**: Implementar el flujo completo de juego OCR (4 screens).

```
Eres un ingeniero iOS con experiencia en SwiftUI, Vision OCR y AVFoundation. Implementa el flujo completo de juego OCR.

## Contexto
- Ruta: /cwd/alphakids-kmp
- Rama: feature/ios-correccion
- Bridges: GameRepositoryAsync (getPlayableWords, completeSession) listos
- Services: VisionOcrService, AudioPlayerService, TtsService ya creados
- Singletons sharedLogic: GameSessionState (setWord, currentWordText, etc.), GameProgressManager (addCoins, completeWord), AchievementAnalytics
- Referencia: requirements/ios-replication-spec.md sección 3.12–3.15
- Recompensas: 1 intento=100coins/40xp/3stars, 2=75/30/2, 3-4=50/20/1, 5+=25/10/1

## Archivos a crear

### 1. Game/LearningAdventureHub.swift
- Alphi grande (imagen alphi_trabajando o SF Symbol emoji)
- Título "¡Elige una actividad!"
- Card "Escaneo de Letras" con gradient Nature (#4FA8F0 → #6C5CE7)
  - Al seleccionar → navega a WordSelectionScreen (agregar AppRoute.wordSelection)
- Card "Aventura de Deletreo" con gradient Magic (#8B7CF6 → #6C5CE7), disabled con "Próximamente"

### 2. Game/WordSelectionScreen.swift + WordSelectionViewModel.swift
WordSelectionViewModel:
- @Published var: state (loading, loaded(PlayableWordsResponseDto), error)
- loadWords(studentId:) async

WordSelectionScreen:
- Si flow == "ASSIGNED": badge "📚 Palabras asignadas por tu profesor" arriba
- Si flow == "CATALOG": badge "📖 Catálogo de palabras"
- LazyVGrid de WordSelectionCard (imagen + texto + gradient)
- Al seleccionar: GameSessionState.shared.setWord(text:id:difficulty:imageUrl:audioUrl:)
  → navega a WordScannerChallenge
- LaunchedEffect: GameSessionState.shared.clear() al entrar

### 3. Game/WordScannerChallenge.swift + GameViewModel.swift
GameViewModel:
- @Published var: scannedText, letterSlots, isCorrect, attempts, startDate, hint
- checkWord() -> valida texto OCR vs GameSessionState.currentWordText
- hints[]: progresivos (primera letra, última letra, etc.)

WordScannerChallenge:
- Header con palabra oculta
- Letter slots: HStack de cuadrados (uno por letra), se llenan al detectar
- CameraView: AVCaptureSession + VisionOcrService en vivo
- Alphi hint bar (TtsService.speak)
- Botón "Escanear" → captura frame → OCR → llena slots
- Botón "Reintentar" si falla
- Éxito → navega a OCRResultScreen con parámetros

### 4. Game/OCRResultScreen.swift
- 🎉 Animación de celebración (confetti o emoji grande)
- Muestra imagen de la palabra desde GameSessionState.currentImageUrl
- Rewards animados: monedas + XP + estrellas (usar calculateRewards(attempts))
- Stats: intentos, tiempo, precisión
- 3 botones:
  - "Seguir jugando" → WordSelection
  - "Repetir" → WordScannerChallenge (misma palabra)
  - "Ir al inicio" → AdventureHome
- Llamar:
  - GameProgressManager.shared.addCoins(Int32(coins))
  - GameProgressManager.shared.completeWord(Int32(stars))
  - AchievementAnalytics.shared.trackSessionCompleted(...)
  - GameRepositoryAsync.completeSession(...) best-effort

### 5. Actualizar ContentView.swift
Agregar AppRoute: wordSelection, wordScannerChallenge, ocrResult(attempts: Int32, time: Int32, wordText: String)

## Nota sobre CameraView:
No uses UIViewRepresentable para la cámara si es muy complejo. Alternativa:
- Botón "Escanear" que abre UIImagePickerController o usa la galería
- La cámara en vivo se puede dejar para una iteración posterior
- Lo IMPORTANTE es que el OCR funcione con imágenes seleccionadas

## Verificación:
- WordSelection carga palabras reales de API
- GameSessionState persiste la palabra entre screens
- OCR detecta texto en español
- Recompensas calculadas correctamente según attempts
- GameProgressManager actualizado localmente
- Analytics registra eventos
- API call best-effort no bloquea la UI
```

---

## 🎨 PROMPT B5: Parent Dashboard (3 tabs)

> **Para Claude Code**: Implementar el dashboard de padres completo.

```
Eres un ingeniero iOS con experiencia en SwiftUI. Implementa el Parent Dashboard (3 tabs + ChildDetail + Support).

## Contexto
- Ruta: /cwd/alphakids-kmp
- Rama: feature/ios-correccion
- Bridge: ParentRepositoryAsync (getChildren, getChildStats, getSubscription, getFAQs, submitContactForm) completado
- NetflixProfilesScreen ya tiene botón "Padre" que debe navegar aquí
- Referencia: requirements/ios-replication-spec.md sección 3.16

## Archivos a crear

### 1. Parent/ParentDashboardScreen.swift
- TabView con 3 tabs: 📊 Dashboard | 👶 Hijos | 💳 Suscripción
- Top bar: "Panel de Padres" + modo niños (vuelve a NetflixProfiles) + ⚙️ menú
- ⚙️ menú: Soporte, Términos, Cerrar sesión

### 2. Parent/ParentInsightCenter.swift (Tab 0)
- @StateObject var viewModel: ParentInsightViewModel
- KPIs grid 4 columnas: Hijos, Palabras, Tiempo, Monedas, Estrellas, OCR, Deletreo, Nivel prom.
- Children list: HStack circular avatars + nombre + nivel
- Activity timeline: recent activity items
- ViewModel llama: getChildren(), getChildStats() para cada hijo

### 3. Parent/ChildDetailScreen.swift (Tab 1 — drill down)
- Ruta: /parent-child-detail/{childId}
- Profile header: avatar DiceBear grande + nombre + nivel + rango
- Stats grid 2x3: Palabras, OCR, Deletreo, Tiempo, Monedas, Estrellas
- WeeklyProgressSection: 7 círculos Lun-Dom, filled = completado
- ViewModel: ChildDetailViewModel llama getChildStats()

### 4. Parent/SubscriptionScreen.swift (Tab 2)
- Current plan card: FREE (gratis) o PREMIUM (dorado con badge)
- Si FREE: botón "Mejorar plan"
- Benefits list con iconos
- Payment history mock
- ViewModel: SubscriptionViewModel llama getSubscription()

### 5. Parent/SupportScreen.swift (menú ⚙️)
- FAQ accordion: lista expandible de preguntas/respuestas
- Contact form: nombre, email, mensaje
- Enviar → submitContactForm()
- ViewModel: SupportViewModel llama getFAQs(), submitContactForm()

### 6. Actualizar ContentView.swift
Agregar AppRoute: parentDashboard, parentChildDetail(String), parentSupport
navigationDestination para cada uno

## Verificación:
- Navegación desde "Padre" en NetflixProfiles funciona
- KPIs se cargan con datos reales de API
- ChildDetail muestra stats correctos
- FAQ accordion expande/colapsa
- Contact form envía correctamente
```

---

## 📋 Orden Recomendado de Ejecución

```
FASE 0: Setup inicial
  1. git checkout feature/ios-correccion
  2. Load skills: alpha-ios-swift + alpha-general

FASE 1: Infraestructura (PUEDE CORRER EN PARALELO)
  Prompt A1 → Bridges Koin→Swift   [15-30 min en Claude]
  Prompt A2 → Style + Services     [10-20 min en Claude]

FASE 2: UI/UX (ORDEN RECOMENDADO, cada prompt es independiente)
  Prompt B1 → Wizard (6 screens)       [60-90 min]
  Prompt B2 → Store + Pets + Logros    [60-90 min]
  Prompt B3 → Diccionario              [30-45 min]

FASE 3: Juego (depende de F1+F3)
  Prompt B4 → Flujo OCR (4 screens)    [60-90 min]

FASE 4: Padre (depende de F1)
  Prompt B5 → Parent Dashboard         [45-60 min]
```

---

## 💡 Tips para usar con Claude Code

1. **Cada prompt es autónomo** — Claude Code tiene toda la info para ejecutarlo
2. **Siempre cargar skills primero**: `/load-skill alpha-ios-swift` y `/load-skill alpha-general`
3. **Verificar rama**: `git branch` → debe decir `feature/ios-correccion`
4. **Después de cada prompt**: correr Xcode build (Cmd+R) para verificar que compile
5. **Errores comunes**: 
   - Si un método Kotlin no se encuentra: revisar `InitKoinKt` para las funciones exportadas
   - Si un DTO no es accesible: el nombre exportado puede tener prefijo `SharedLogic.`
   - Si un método `suspend` no tiene callback: buscar el nombre exacto de la función ObjC generada
6. **Para debugging de bridges**: usar el patrón existente en `AuthRepository+Async.swift` como referencia EXACTA
