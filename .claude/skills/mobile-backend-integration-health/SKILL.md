---
name: mobile-backend-integration-health
description: "Trigger: backend URL, API client, token refresh, release build, signing, Play Store, Ktor, network config. Audit whether the mobile app's backend integration is correctly configured before shipping."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.1"
---

## Activation Contract

Activate when:
- The backend's URL/host changes (new deploy target, domain migration).
- Touching `AlphaKidsApiClient.kt`, `ApiConstants.kt`, `TokenStorage.kt`, or
  any `*RepositoryImpl.kt` that calls `api.httpClient`.
- Adding a brand-new repository/service that talks to the backend.
- Before building a release artifact for Play Store (or any external
  distribution) for the first time, or after touching `androidApp/build.gradle.kts`.
- The user asks "is the backend integration set up right" or reports auth
  failing silently after the app has been open for a while.

This skill exists because a real audit found 4 separate gaps at once: two
repositories silently stopped working after the access token expired, the
production URL was a hand-edited hardcoded string, a network hiccup during
token refresh could deauth a child for no real reason, and there was no way
to produce a signed release build. None of these threw a compile error —
they only show up as "it works in dev, breaks in the field."

## Hard Rules

1. **No literal backend URL outside `ApiConstants.BASE_URL` and
   `androidApp/build.gradle.kts`'s `apiBaseUrl`.** Grep the whole
   `sharedUI`/`sharedLogic`/`androidApp` trees for `https://` domains that
   look like a backend host (`onrender.com`, `railway.app`, `.up.railway.app`,
   or whatever the current host is) outside those two spots. A hardcoded
   URL anywhere else means the next backend migration requires a manual
   find-and-replace across the codebase instead of one config change.
2. **Every repository that calls `api.httpClient` must go through the
   shared `AlphaKidsApiClient`** — never construct a second `HttpClient`
   ad hoc. The `Auth` plugin (bearer token + refresh) is installed once,
   centrally; a second client bypasses it silently.
3. **No repository may hand-roll `if (response.status ==
   HttpStatusCode.Unauthorized) { ... }` retry logic.** That's exactly the
   gap that let `StoreRepositoryImpl`/`StudentPetRepositoryImpl` ship
   without token refresh while `ParentRepositoryImpl`/`GameRepositoryImpl`
   duplicated it seven times. If you see this pattern in a new PR, the
   `Auth` plugin should already be handling it — grep
   `HttpStatusCode.Unauthorized` outside `AlphaKidsApiClient.kt`; it should
   return nothing.
4. **Never clear stored tokens (`tokenStorage.clear()`) from a plain
   `catch (_: Exception)`.** Only clear tokens when the server explicitly
   rejected a request with an auth-related status — a timeout, DNS
   failure, or airplane-mode blip is not proof the refresh token is
   invalid, and clearing on those logs a real, legitimately-authenticated
   user out for no reason.
5. **`HttpTimeout` must stay installed** with explicit
   `requestTimeoutMillis`/`connectTimeoutMillis`/`socketTimeoutMillis` —
   without it, a hung connection blocks a suspend call indefinitely
   instead of failing and letting the UI show a retry state.
6. **Release builds must fail loudly without a real keystore**, never
   silently produce an unsigned artifact. If `keystore.properties` logic
   moves or changes, keep the check as a `gradle.taskGraph.whenReady`
   block (or equivalent that runs once at configuration/graph time) — not
   a per-task `doFirst {}`, which captures a Gradle script object
   reference the configuration cache can't serialize and breaks unrelated
   builds (confirmed the hard way; see `androidApp/build.gradle.kts` history).
7. **`versionCode` must be bumped before every Play Store upload.** Google
   Play rejects a re-used `versionCode` outright — check
   `androidApp/build.gradle.kts:defaultConfig` isn't still `1`/`"1.0"` from
   the last release.
8. **CORS is not a mobile concern.** `CORS_ORIGIN` in the backend's `.env`
   only matters for a browser-based client (the web admin panel). Don't
   spend time investigating CORS for a native Android/iOS bug — Ktor's
   `HttpClient` isn't subject to same-origin policy at all.
9. **`BASE_URL` must stay `https://`.** If `android:usesCleartextTraffic="true"`
   or a `network_security_config.xml` allowing cleartext ever gets added,
   that needs a specific justification in the PR — Play Store's data-safety
   review flags cleartext traffic to arbitrary hosts, and this app talks to
   children's data.
10. **A DTO field's default value must never silently gate a feature for a
    case the backend legitimately omits the field for.** Found: `StudentDto.
    verificationStatus` defaults to `"PENDING"` when the backend response
    doesn't include it — correct for an institutional student mid-review,
    but a freemium student (`institutionId == null`) has no director and no
    verification workflow at all, so the same lenient default routed every
    freemium child into `AwaitingApprovalScreen` (and its one-tap "Cerrar
    sesión" button) instead of straight to Home. Any screen gating on a
    lenient-decoded field must also check the field that makes that gate
    meaningful in the first place (here: `institutionId != null`) — never
    trust the DTO default alone to mean "this case doesn't apply."

## Decision Gates

| Check | Command / where to look | Pass condition |
|---|---|---|
| Hardcoded backend URL | `rg 'https://[a-z0-9.-]+\.(onrender\.com\|railway\.app\|up\.railway\.app)' sharedLogic sharedUI androidApp` | Only `ApiConstants.kt` (fallback) and `androidApp/build.gradle.kts` (`apiBaseUrl` default) match |
| Manual 401 handling | `rg 'HttpStatusCode\.Unauthorized' sharedLogic` | Zero matches outside `AlphaKidsApiClient.kt` |
| Second HTTP client | `rg 'HttpClient\s*\{' sharedLogic sharedUI` | Only `AlphaKidsApiClient.kt` constructs one |
| Token clear on generic catch | Read every `tokenStorage.clear()` call site | Each one is inside a branch that confirmed a real server rejection, not a bare `catch` |
| Timeout installed | Read `AlphaKidsApiClient.kt` | `install(HttpTimeout) { ... }` present with all three fields set |
| Release signing | `./gradlew :androidApp:assembleRelease` with no `keystore.properties` | Fails fast with the "Missing androidApp/keystore.properties" message, not a silent unsigned build |
| Release minification | Read `androidApp/build.gradle.kts` `buildTypes.release` | `isMinifyEnabled = true`, `isShrinkResources = true`, `proguardFiles(...)` present |
| versionCode freshness | Read `defaultConfig.versionCode` | Higher than the last value actually uploaded to Play Store (check the Play Console, not just the repo) |
| Cleartext | `AndroidManifest.xml` | No `usesCleartextTraffic="true"` without a comment justifying it |

## Execution Steps

1. Run every grep in the Decision Gates table above.
2. For any hit outside the allowed files, trace it back to why it's there
   — likely a new repository/service that didn't reuse `AlphaKidsApiClient`.
3. Read `AlphaKidsApiClient.kt` in full; confirm the `Auth { bearer { ... } }`
   block is intact and its `refreshTokens` lambda still distinguishes a
   server rejection (clears tokens) from a thrown exception (doesn't).
4. If auditing before a release: run `assembleRelease` twice — once
   without `keystore.properties` (must fail with the clear message) and
   once with a real one (must produce a signed, minified artifact) — then
   install that signed build and smoke-test login, the child wizard,
   gameplay, and the store, since R8 issues are runtime-only and won't
   show up in a normal debug build or in `./gradlew test`.
5. Confirm `versionCode` was bumped if this audit is happening right
   before a Play Store upload.

## Output Contract

Return:
- Pass/fail per Decision Gate row, with the exact grep output for any hit.
- Any repository found bypassing the shared `Auth` plugin, with file path.
- Confirmation the release build was tested (fail-without-keystore AND
  succeed-with-keystore), or an explicit note that this wasn't run if the
  audit isn't tied to an imminent release.
