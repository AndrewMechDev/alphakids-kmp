---
name: ocr-scan-flow-health
description: "Trigger: OCR, ML Kit, CameraX, WordScannerChallenge, letter scan, camera preview, reward metrics, XP, precisión. Audit and evolve the letter-scanning game flow without reintroducing its known gotchas."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activate when:
- Touching `WordScannerChallenge.kt`, `OCRResultScreen.kt`, `CameraView.kt`,
  `TextRecognitionAnalyzer.kt`, or `ScreenOrientation.kt`.
- Adding or changing a reward/progress metric anywhere in the app (coins,
  stars, XP, level, accuracy, streaks).
- The user reports OCR misreading a word, letter boxes clipping/cutting
  off, or asks about landscape/rotation on any screen.

This skill exists because a real audit + field testing with kids, parents,
and teachers found 6 separate problems in this one flow: a single garbled
camera frame could silently produce a completely different word ("CASA"
scanned with a mirrored S became a different word entirely — not a
mis-read letter, ML Kit's language model reinterpreted the whole line);
long words got cut off with no scroll; both outcomes required a button tap
that competes with the two hands already holding the word up to the
camera; a hardcoded placeholder hint duplicated the screen's own title;
and three independent, disconnected, locally-invented "XP" numbers existed
across the app that nobody in testing understood — while the *real*
backend-tracked progression currency (stars → level → rank) sat right
there unused for this purpose.

## Hard Rules

1. **Never accept an OCR result from a single camera frame.** ML Kit's
   `TextRecognition` applies its own language-model correction to the
   whole recognized line — one rotated/occluded letter can make it "read"
   an entirely different word, not just mis-read one character. Require
   the same cleaned text across multiple consecutive frames
   (`STABILITY_FRAMES` in `WordScannerChallenge.kt`) before locking in a
   result.
2. **What's shown must be what was scanned.** Never silently substitute,
   correct, or hide the literal detected text — on a mismatch, tell the
   user what was actually detected (`OcrResult.detectedText`), not just
   "failed."
3. **No button on either outcome of an automatic scan flow.** Both hands
   are already holding the word up to the camera. A match and a mismatch
   both already have a voice cue (`AudioCategory.CHEER`/`ENCOURAGE`) —
   drive timing off that with a `delay()` + auto-navigate/auto-reset, not
   a tap target.
4. **Any fixed-size `Row`/list of unknown-length content must scroll.**
   `LetterSlotsRow` was a plain `Row` with no scroll; an 8+ letter word
   could run past the screen edge, cutting off boxes that were actually
   filled correctly. Use `LazyRow`/`LazyColumn` for anything sized by
   variable-length domain data (word length, list length), never a plain
   `Row`/`Column` assumed to "always fit."
5. **Landscape is opt-in per screen, not global.** The app defaults to
   portrait (`AndroidManifest.xml`'s `android:screenOrientation="portrait"`)
   because no other screen is laid out for landscape. A screen that needs
   rotation (long-word framing needs more camera width) opts back in via
   `AllowLandscapeWhileVisible()` (`ScreenOrientation.kt`, Android-only
   expect/actual — `sharedUI` has no iOS target yet) and must keep the
   Activity's `android:configChanges="orientation|screenSize|screenLayout"`
   intact, or rotating destroys/recreates the Activity and wipes the whole
   nav back stack.
6. **Never hardcode a per-item hint/description that duplicates a static
   title.** The API's word-assignment schema has no per-word clue field;
   `App.kt` used to hardcode `hint = "Escanea las letras"` for every API
   word — identical to the screen's own title. Prefer real data that IS
   available (e.g. difficulty label) over inventing filler text.
7. **A reward/progress metric must map to something the backend actually
   tracks, or be clearly a local-only decoration — never a third,
   invented number competing with a real one.** `stars` → `totalStars` →
   `currentRank`/`currentLevel` is the real, backend-tracked progression
   chain (see `AchievementModels.kt`). An "XP" system was independently
   invented in three places (`OCRResultScreen`'s `calculateRewards()`,
   `AchievementsScreen`'s `wordsUsed * 10` rank-card bar, a dead
   `HomeViewModel.UiState.xp` field with zero consumers) — none backed by
   the API, none agreeing with each other, and none understood by parents
   or teachers in testing. When a rank/level progress bar is needed, drive
   it from the real `level`/`nextRankRequiredLevel`, not an invented XP
   formula.
8. **A displayed stat must have real data behind it, even if simple.**
   `formatTime()`/"Tiempo" showed `--` forever because nothing ever
   measured real elapsed time (`WordScannerChallenge` always passed
   `time = 0L`). Don't ship a stat card with permanently-fake output —
   either wire it to something real or remove it.
9. **Pet leveling is a separate system — don't conflate it with the
   child's own reward metrics.** `PetsScreen`'s pet `xp`/`xpToNextLevel`
   is intentional, self-contained, and contextually obvious ("your pet
   leveled up"). It is not the same "XP" problem as Hard Rule #7 and
   wasn't part of the testing feedback that removed the child-facing XP —
   don't remove it without a separate, explicit reason.

## Decision Gates

| Check | Command / where to look | Pass condition |
|---|---|---|
| Single-frame trust | Read `WordScannerChallenge.kt`'s `onTextDetected` | A result only locks in after `pendingCount >= STABILITY_FRAMES` consecutive identical reads |
| Detected text surfaced | Read the mismatch UI branch | Shows `result.detectedText`, not just a generic failure message |
| No manual retry/continue button | `rg 'Button\(' sharedUI/.../jugar/WordScannerChallenge.kt` | No `Button` composable in the result-feedback section — only `LaunchedEffect` + `delay()` driving the automatic flow |
| Overflow-safe letter slots | Read `LetterSlotsRow` | Uses `LazyRow`, not a plain `Row` |
| Scoped landscape | `rg 'screenOrientation' androidApp/src/main/AndroidManifest.xml` | Manifest locks `portrait`; only screens that explicitly call `AllowLandscapeWhileVisible()` can rotate |
| No nested double-scroll | `rg -c 'verticalScroll' sharedUI/src -g '*.kt'` | Every matching file shows exactly 2 (one import + one usage) — see `consistent-back-button`'s note on the same class of bug |
| No fabricated reward metric | `rg '\bxp\b' sharedLogic sharedUI --glob '!*Pets*'` outside `PetsScreen.kt`/pet models | No hits — any progression display should trace back to `stars`/`level`/`currentRank` |
| No permanently-fake stat | Read any `StatItem`/stat card added | Every value is either real (has a code path that can produce something other than a placeholder) or removed |

## Execution Steps

1. Run every Decision Gate grep above.
2. If a rank/level/progress UI is being added or changed, trace it back to
   `AchievementData`/`ChildSummary` — confirm it reads `level`,
   `nextRankRequiredLevel`, or `stars`, never a locally-invented formula.
3. If touching `WordScannerChallenge.kt`'s detection logic, re-verify the
   stability-voting loop wasn't weakened (e.g. `STABILITY_FRAMES` lowered
   without a documented reason, or a code path that can still update
   `showResult`/`result` from a single frame).
4. If adding a new screen or list sized by variable-length data, confirm
   it scrolls (`LazyRow`/`LazyColumn`, or a bounded-height nested
   `verticalScroll` per the `consistent-back-button` skill's PaquitoBot
   lesson).

## Output Contract

Return:
- Pass/fail per Decision Gate row, with the exact grep output for any hit.
- Any new or changed reward/progress metric, with the real field it traces
  back to (or an explicit note that it's intentionally local-only, like
  pet XP).
- Confirmation the OCR flow was tested with at least one deliberately
  ambiguous/rotated letter, if the detection logic changed.
