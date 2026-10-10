# KMP benchmark fixes

## Objective

Fix the real defects of the Android KMP app so it can serve as a clean reference
implementation for the KMP-vs-Flutter benchmark article (alphakids-flutter).

## Problem

An audit of `develop` (0a5f406, 2026-10-09) found defects that would distort a
performance/behaviour comparison (fixed 4 s splash, release audio crash, fake coin
balance, hardcoded store/pets, leaking OCR recognizer, sequential network calls)
plus security/privacy and hygiene defects.

## Why

The article thesis is "KMP is better for native-heavy apps like AlphaKids", backed by
honest measurements. A buggy reference invalidates the comparison.

## Scope

- In: `androidApp`, `sharedLogic`, `sharedUI` (Android).
- Out: `iosApp`, backend `alphakids-api` (debt recorded below), media assets
  (audio, images, icons stay untouched for now).
- Dead code is removed (user decision 2026-10-09). Assets are not dead code.

## Constraints

- Git Flow: one branch per task from `develop`, local `git merge --no-ff` into
  `develop`, push branch and `develop`. No PRs. Branches are never deleted.
- Conventional Commits in English, scoped, work units with tests alongside code.
- Every task must keep `./gradlew :androidApp:assembleDebug allTests` green.

## Tasks

Defect IDs reference the audit (C = critical, H = high, M = medium, L = low).

- [x] T0 `chore/build-toolchain`: commit AGP 9.4.0 / Gradle 9.6.0 bump, skill registry refresh, ignore `.codegraph/`.
- [x] T1 `fix/release-audio-crash`: C1 (R.raw reflection stripped by R8), M8 (MediaPlayer on main thread, never released, one service per screen).
- [x] T2 `perf/startup-assets`: H8 (fixed 4 s splash delay), L8 (Calendar per glass color call). Image re-encoding deferred (assets untouched).
- [x] T3 `fix/economy-api-wiring`: H3 (coins never loaded from API), H4 (store/pets hardcoded, wrong pets routes and feed body), M10 (item granted without enough coins).
- [x] T4 `fix/game-flow-state`: H9 (word lost on process death), M1 (rewards granted twice), M9 (spaces in words), L2 (unencoded route args), L3 (repeat flow, time = 0), M11 (tests for rewards and OCR vote).
- [x] T5 `fix/viewmodel-lifecycle`: M2 (ViewModels via remember{}, leaked scope), M6 (sequential network calls).
- [x] T6 `perf/ocr-pipeline`: M4 (recognizer never closed, no throttling, per-frame recomposition).
- [ ] T7 `fix/auth-session-isolation`: H1 (Ktor bearer cache), H2 (singletons not reset), M5 (logout cancelled), L1 (fake OTP), L4 (raw e.message shown), M11 (auth refresh tests with MockEngine).
- [ ] T8 `fix/manifest-permissions-backup`: H5 (unused sensitive permissions), H6 (wrong tools namespace), H7 (allowBackup with plain tokens).
- [ ] T9 `fix/repository-errors`: M3 (swallowed exceptions incl. CancellationException), M12 (duplicate repo methods).
- [ ] T10 `chore/release-hygiene`: L5 (dead code), L6 (emoji in UI), L7 (versionCode, pre-release deps).

## Acceptance criteria

- Each defect in a task is fixed or explicitly deferred with a reason.
- Debug build and `allTests` pass after every merge into `develop`.
- No asset (audio, image, icon) deleted.

## Checks

- `./gradlew :androidApp:assembleDebug allTests` per task.
- `./gradlew :androidApp:assembleRelease` where release behaviour matters (T1, T10), if a keystore is available.

## Delivery strategy

`single-branch-per-task`: each task is a branch merged locally with `--no-ff` into
`develop`; no PRs by user policy.

## Backend debt (out of scope, alphakids-api)

- No OTP / email verification endpoints; register returns tokens directly.
- `POST /game-sessions/complete` trusts client coins/stars without caps.
- `POST /email/send` reachable by any authenticated user.
- Store balance check and game-session progress update outside transactions.
- Purchase responses carry no coin balance; catalogs not filtered by `isActive`; same pet can be bought twice; no endpoint for pet play or active pet.
- `POST /game-sessions/complete` should deduplicate on `metrics.attemptId`.

## iOS debt (out of scope)

- T3 changed `StoreRepository`, `StudentPetRepository` and `GameProgressManager`; `iosApp` Swift bridges (`StoreRepository+Async.swift`, `StudentPetRepository+Async.swift`) and `PetsScreen.swift`, `StoreScreen.swift`, `AchievementsScreen.swift`, `OCRResultScreen.swift` must be updated before iOS builds again.

## Progress

| Task | Route | Commit(s) | Checks | Review |
|---|---|---|---|---|
| T0 | inline (mechanical) | 4e726e2, b070bc9, 2227515, e4394bd; merge 4943c11 | assembleDebug + allTests green | medium, under budget |
| T1 | delegated (multi-file writer trigger) | 9c4b0cc; merge ebfdc22 | assembleDebug + allTests green; packageRelease keeps 48/48 mp3 (was 1) | medium, under budget |
| T2 | delegated (same writer) | 49a0a02, f68aea5; merge 5a45892 | assembleDebug + allTests green (re-run by parent on develop) | medium, under budget |
| T3 | delegated (multi-file writer trigger) | 340f82c, 536d9f7, a5f4ca0; merge 59fa3a1 | MockEngine contract tests RED on old routes then GREEN; assembleDebug + allTests green (re-run by parent) | medium, slice budget reached; consent granted, reviewer unavailable (provider auth disabled for org), lineage abandoned by user decision |
| T4 | delegated (multi-file writer trigger) | 9bd28c0, 6e4ba94, 3cdf880, 909fb9c; merge 76b9c5e | RED on space normalization and route encoding, then GREEN; assembleDebug + allTests green (146 tests; re-run by parent) | medium, slice budget reached; consent declined by user |
| T6 | delegated (multi-file writer trigger) | a94a0d7, 8b088a6, 94855d6; merge 394c187 | gate/stats/session tests RED (missing types) then GREEN; assembleDebug + allTests green (171 tests) | medium, slice budget reached; consent declined by user |
| T5 | delegated (multi-file writer trigger) | 082be66, 56e3a90, 0209afa; merge 60c1c4e | concurrency tests RED (200/400/200 ms) then GREEN (100/200/100 ms); assembleDebug + allTests green (152 tests) | medium, slice budget reached; consent declined by user |

Notes: T6 pipeline constants live in `OcrPipelineConfig` (1280x720 keep-only-latest, MIN_ANALYSIS_INTERVAL 200 ms, STATS_WINDOW 30); the Flutter replica must use the same values. Recognizer owned by `OcrPipeline` ViewModel (survives rotation, closed in onCleared). Perfetto: `ocr.recognize` section, `ocr.frames.analyzed/skipped` counters. Not device-tested.

Notes: T5 scopes WizardViewModel to nested graph `wizard-flow`; tab VMs live in the home back-stack entry (no re-fetch on tab switch). Remaining: screens use `collectAsState` (not lifecycle-aware); `ChildrenListTab` still fetches from a `LaunchedEffect`. Not device-tested (rotation, process death, wizard back).

Notes: T4 matching ignores Spanish vowel accents but keeps Ñ distinct; slots show letters only (accents kept). Result is reported once per attempt id (SavedStateHandle); an interrupted request is not retried. `GameSessionState` kept because iOS still uses it. Not device-tested (camera, real process death).

Notes: T3 removed pet stats/actions with no backend endpoint (energy, XP, pet level, "Interactuar", "main pet"); "Jugar" is visual only. T3 breaks `iosApp` compilation (Swift bridges and 4 screens use changed/removed APIs) — iOS is out of scope, recorded as debt.

Notes: T1 keeps sounds playing across screen changes on purpose (Dashboard plays "jugar" then navigates); playback stops on app background. Shared cooldown now skips a second cheer within 10 s. Device playback not tested.

## Next step

T7.
