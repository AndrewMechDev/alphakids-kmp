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

- [ ] T0 `chore/build-toolchain`: commit AGP 9.4.0 / Gradle 9.6.0 bump, skill registry refresh, ignore `.codegraph/`.
- [ ] T1 `fix/release-audio-crash`: C1 (R.raw reflection stripped by R8), M8 (MediaPlayer on main thread, never released, one service per screen).
- [ ] T2 `perf/startup-assets`: H8 (fixed 4 s splash delay), L8 (Calendar per glass color call). Image re-encoding deferred (assets untouched).
- [ ] T3 `fix/economy-api-wiring`: H3 (coins never loaded from API), H4 (store/pets hardcoded, wrong pets routes and feed body), M10 (item granted without enough coins).
- [ ] T4 `fix/game-flow-state`: H9 (word lost on process death), M1 (rewards granted twice), M9 (spaces in words), L2 (unencoded route args), L3 (repeat flow, time = 0), M11 (tests for rewards and OCR vote).
- [ ] T5 `fix/viewmodel-lifecycle`: M2 (ViewModels via remember{}, leaked scope), M6 (sequential network calls).
- [ ] T6 `perf/ocr-pipeline`: M4 (recognizer never closed, no throttling, per-frame recomposition).
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

## Progress

| Task | Route | Commit(s) | Checks | Review |
|---|---|---|---|---|

## Next step

T0.
