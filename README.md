# AlphaKids KMP

Aplicación educativa infantil (4-8 años) desarrollada con Kotlin Multiplatform + Compose Multiplatform.

## Stack

- **Kotlin Multiplatform** (KMP) — lógica compartida entre Android e iOS
- **Compose Multiplatform** — UI compartida para Android
- **SwiftUI nativo** — UI para iOS
- **Clean Architecture** (Hexagonal) + MVVM por plataforma
- **Koin** — Inyección de dependencias multiplatform
- **Ktor** — Networking REST (preparado, desactivado por ahora)
- **SQLDelight** — Base de datos local

## Módulos

- [`/androidApp`](./androidApp) — Aplicación Android (Compose Multiplatform + Navigation Compose)
- [`/iosApp`](./iosApp) — Aplicación iOS (SwiftUI)
- [`/sharedUI`](./sharedUI) — UI compartida Compose Multiplatform
- [`/sharedLogic`](./sharedLogic) — Lógica de negocio compartida (domain/data/di/platform)

## Flujo actual

```
Splash → Login → Register → OTP → SetupWizard → CreateChild 
→ ChooseAvatar → ChooseFirstPet → Welcome → PlaceholderHome
```

## Comandos

```bash
# Android
./gradlew :androidApp:installDebug          # Instalar en dispositivo
./gradlew :androidApp:assembleDebug          # Build APK
./gradlew allTests                           # Tests compartidos

# iOS
Abrir iosApp/ en Xcode y compilar
```

## Estado del proyecto

- ✅ **Phase 0** — Infraestructura (arquitectura, DI, skills, tooling)
- ✅ **Phase 1** — Onboarding completo (10 pantallas, 61 tests)
- ✅ **Phase 2** — AdventureHome (Home con tabs + Dashboard)
- ⏳ **Tabs restantes** — Diccionario, Tienda, Logros, Mascotas
