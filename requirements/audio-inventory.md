# Inventario de Audio — AlphaKids

## Estructura general

| Categoría | Cantidad | Ruta | Prefijo |
|-----------|----------|------|---------|
| UI | 1 | `assets/audio/ui/` | `jugar.mp3` |
| Instrucciones | 20 | `assets/audio/instructions/` | `instruccion_XX.mp3` |
| Celebrar | 12 | `assets/audio/cheer/` | `cheer_XX.mp3` |
| Aliento | 15 | `assets/audio/encourage/` | `encourage_XX.mp3` |
| **Total** | **48** | | |

> Hay 1 archivo extra (`ElevenLabs_2026-05-28T20_48_00_alphi-v2premium_...mp3`) dentro de `instructions/` que **no sigue el naming convention** y **no es referenciado por el código**. Es un archivo huérfano generado por ElevenLabs — candidato a eliminar.

---

## 1. `assets/audio/ui/jugar.mp3`

| Campo | Valor |
|-------|-------|
| **Propósito** | Sonido de acción al presionar "Jugar" en el dashboard infantil |
| **Servicio** | `AudioService.playJugarSound()` |
| **Pool** | `_jugarPool` (1 player, sin variación) |
| **Throttle** | Sin throttle |
| **Consumidor** | `ChildDashboardScreen._showGameOptions()` — línea 150 |

**Flujo:** El niño selecciona una palabra → se dispara `playJugarSound()` → se abre el modal "¿Cómo querés jugar?" (Escanear / Dictado).

---

## 2. `assets/audio/instructions/instruccion_01.mp3` → `instruccion_20.mp3`

| Campo | Valor |
|-------|-------|
| **Propósito** | Instrucción de voz al ingresar a una pantalla de juego. Cada clip dice algo como "Escanéá la palabra..." o "Decí la letra..." para guiar al niño. |
| **Servicio** | `AudioService.playInstructions()` |
| **Pool** | `AudioCategory.instructions` (20 clips, 1-2 players) |
| **Throttle** | Sin throttle |
| **Selección** | Aleatoria (`Random.nextInt(20)`) |

**Consumidores:**

| Archivo | Línea | Momento |
|---------|-------|---------|
| `OcrScannerScreen.initState()` | 33 | Apenas se abre el scanner, al inicio |
| `OcrAudioController._speakInstructions()` | 19 | Apenas se inicializa el controlador OCR |
| `DictadoScreen.initState()` | 33 | Apenas se abre la pantalla de dictado |

**Flujo:** El juego arranca → se ejecuta `playInstructions()` → suena un clip aleatorio de instrucción → el niño escucha qué hacer.

---

## 3. `assets/audio/cheer/cheer_01.mp3` → `cheer_12.mp3`

| Campo | Valor |
|-------|-------|
| **Propósito** | Celebrar un acierto. Aplausos, "Muy bien!", "Lo lograste!" — refuerzo positivo |
| **Servicio** | `AudioService.playCelebration()` |
| **Pool** | `AudioCategory.cheer` (12 clips, 1-2 players) |
| **Throttle** | 10 segundos entre reproducciones (evita spam si el niño acierta rápido seguido) |
| **Selección** | Aleatoria (`Random.nextInt(12)`) |

**Consumidores:**

| Archivo | Línea | Condición |
|---------|-------|-----------|
| `OcrScannerScreen._onControllerChanged()` | 65 | `ScanStatus.match` — la cámara reconoció la palabra correcta |
| `OcrAudioController.onScanResult()` | 30 | `ScanStatus.match` — OCR match |
| `DictadoController._onResult()` | 157 | Letra correcta + palabra completa (`_currentPosition >= _targetWord.length`) |

**Flujo:** El niño acierta → se dispara `playCelebration()` → suena un clip de celebración → feedback visual complementario.

---

## 4. `assets/audio/encourage/encourage_01.mp3` → `encourage_15.mp3`

| Campo | Valor |
|-------|-------|
| **Propósito** | Alentar cuando se equivoca o no reconoce. "Casi!", "Intentá de nuevo!", "No pasa nada!" — mantener motivación |
| **Servicio** | `AudioService.playEncouragement()` |
| **Pool** | `AudioCategory.encourage` (15 clips, 1-2 players) |
| **Throttle** | 10 segundos entre reproducciones |
| **Selección** | Aleatoria (`Random.nextInt(15)`) |

**Consumidores:**

| Archivo | Línea | Condición |
|---------|-------|-----------|
| `OcrScannerScreen._onControllerChanged()` | 67 | `ScanStatus.noMatch` — la cámara leyó algo que no coincide |
| `OcrAudioController.onScanResult()` | 32 | `ScanStatus.noMatch` — OCR no coincide |
| `DictadoController._onResult()` | 164 | Letra incorrecta (`detectedChar != expected`) |

**Flujo:** El niño se equivoca → se dispara `playEncouragement()` → suena un clip de aliento → se reinicia el intento para esa letra.

---

## Mapa completo clip por clip

### Instrucciones (20 clips)

| Archivo | Referencia en código |
|---------|---------------------|
| `instruccion_01.mp3` | `List.generate` index 0 |
| `instruccion_02.mp3` | index 1 |
| `instruccion_03.mp3` | index 2 |
| `instruccion_04.mp3` | index 3 |
| `instruccion_05.mp3` | index 4 |
| `instruccion_06.mp3` | index 5 |
| `instruccion_07.mp3` | index 6 |
| `instruccion_08.mp3` | index 7 |
| `instruccion_09.mp3` | index 8 |
| `instruccion_10.mp3` | index 9 |
| `instruccion_11.mp3` | index 10 |
| `instruccion_12.mp3` | index 11 |
| `instruccion_13.mp3` | index 12 |
| `instruccion_14.mp3` | index 13 |
| `instruccion_15.mp3` | index 14 |
| `instruccion_16.mp3` | index 15 |
| `instruccion_17.mp3` | index 16 |
| `instruccion_18.mp3` | index 17 |
| `instruccion_19.mp3` | index 18 |
| `instruccion_20.mp3` | index 19 |

### Cheer (12 clips)

| Archivo | Referencia en código |
|---------|---------------------|
| `cheer_01.mp3` | `List.generate` index 0 |
| `cheer_02.mp3` | index 1 |
| `cheer_03.mp3` | index 2 |
| `cheer_04.mp3` | index 3 |
| `cheer_05.mp3` | index 4 |
| `cheer_06.mp3` | index 5 |
| `cheer_07.mp3` | index 6 |
| `cheer_08.mp3` | index 7 |
| `cheer_09.mp3` | index 8 |
| `cheer_10.mp3` | index 9 |
| `cheer_11.mp3` | index 10 |
| `cheer_12.mp3` | index 11 |

### Encourage (15 clips)

| Archivo | Referencia en código |
|---------|---------------------|
| `encourage_01.mp3` | `List.generate` index 0 |
| `encourage_02.mp3` | index 1 |
| `encourage_03.mp3` | index 2 |
| `encourage_04.mp3` | index 3 |
| `encourage_05.mp3` | index 4 |
| `encourage_06.mp3` | index 5 |
| `encourage_07.mp3` | index 6 |
| `encourage_08.mp3` | index 7 |
| `encourage_09.mp3` | index 8 |
| `encourage_10.mp3` | index 9 |
| `encourage_11.mp3` | index 10 |
| `encourage_12.mp3` | index 11 |
| `encourage_13.mp3` | index 12 |
| `encourage_14.mp3` | index 13 |
| `encourage_15.mp3` | index 14 |

---

## Archivos huérfanos

| Archivo | Problema |
|---------|----------|
| `assets/audio/instructions/ElevenLabs_2026-05-28T20_48_00_alphi-v2premium_gen_sp75_s50_sb75_se10_b_m2.mp3` | No sigue el naming convention `instruccion_XX.mp3`, no es cargado por `List.generate(20)`. Se genera automáticamente pero no se usa. |

---

## Resumen de conexiones con el código

```
AudioService (singleton)
├── playJugarSound()           → ChildDashboardScreen (botón jugar)
├── playInstructions()         → OcrScannerScreen.initState()
│                                OcrAudioController._speakInstructions()
│                                DictadoScreen.initState()
├── playCelebration()          → OcrScannerScreen (match)
│   (throttle 10s)               OcrAudioController (match)
│                                DictadoController (palabra completa)
└── playEncouragement()        → OcrScannerScreen (noMatch)
    (throttle 10s)               OcrAudioController (noMatch)
                                 DictadoController (letra incorrecta)
```
