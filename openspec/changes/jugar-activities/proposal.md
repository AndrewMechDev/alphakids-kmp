# Proposal: Jugar Activities

## Intent

Replace the stub "Jugar" dashboard card with real Spanish word-learning activities: scanning physical letter tiles (OCR) and spelling words aloud (STT/TTS). Kids practice 10 hardcoded Spanish words through camera-based recognition or voice spelling.

## Scope

### In Scope
- LearningAdventureHub — activity picker screen (Scan / Spell)
- WordScannerChallenge — live CameraX preview + ML Kit OCR matching
- OCRResultScreen — match/mismatch result display
- SpellingAdventure — TTS word prompt + STT voice capture
- SpellingResultScreen — spelling evaluation result

### Out of Scope
- iOS (Android-only first slice)
- Backend persistence, pet XP, rewards integration
- Word list editing or dynamic word management

## Capabilities

### New Capabilities
- `jugar-hub`: LearningAdventureHub screen — entry point with activity selection cards
- `word-scanner`: CameraX preview + ML Kit OCR + WordValidator in sharedLogic for exact-match validation against the 10-word bank
- `spelling-adventure`: SpeechRecognizer STT + TextToSpeech TTS + SpellingEvaluator in sharedLogic for voice-based spelling assessment

### Modified Capabilities
None — pure addition of new capabilities.

## Approach

Android-only via `AndroidView` composable embedding CameraX `PreviewView`. OCR via ML Kit Text Recognition (on-device). STT via `SpeechRecognizer`, TTS via `TextToSpeech`. Validation logic in `sharedLogic/src/commonMain/kotlin/org/alphakids/app/jugar/` (`WordValidator`, `SpellingEvaluator`) with the 10-word bank hardcoded. OCR match: trim + uppercase exact comparison. Jugar card in `DashboardContent.kt` retargeted from tab-2 stub to `LearningAdventureHub` route. Navigation routes added to Screen sealed class.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `sharedUI/.../jugar/` | New | Hub + Scan + Spell + Results screens |
| `sharedLogic/.../jugar/` | New | WordValidator, SpellingEvaluator, hardcoded 10-word bank |
| `sharedUI/.../home/DashboardContent.kt` | Modified | Jugar card → navigate to hub route |
| `androidApp/.../jugar/` | New | AndroidView + CameraX + TTS/STT platform adapters |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| OCR accuracy on irregular lighting/textures | Medium | Exact-match after trim+uppercase; frame stabilization hints in UX |
| STT accuracy with young children's voices | Medium | Accept partial + similar-sounding matches; voice retry button |
| Camera permissions denied at runtime | Low | Runtime permission launcher with rationale dialog (already declared in manifest) |

## Rollback Plan

1. Delete `sharedUI/.../jugar/` and `sharedLogic/.../jugar/` directories
2. Revert `DashboardContent.kt` Jugar card back to tab-2 stub navigation
3. Remove jugar navigation routes from `Screen` sealed class
4. CameraX/ML Kit deps remain in gradle catalog (shared infrastructure)

## Dependencies

- `CAMERA` + `RECORD_AUDIO` permissions already declared in `AndroidManifest.xml`
- CameraX + ML Kit already referenced in `gradle/libs.versions.toml` (per project-infrastructure spec)

## Success Criteria

- [ ] Scan a physical word tile → OCR recognizes it → result screen shows correct match or mismatch from the 10-word bank
- [ ] Hear a TTS word prompt → speak it back → STT captures → evaluator compares → result screen shows spelling outcome
- [ ] `./gradlew allTests` passes without regression
