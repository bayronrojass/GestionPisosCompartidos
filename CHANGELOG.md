# Changelog

All notable changes to the MiRumi Android Client will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.4.0] - 2026-07-13

### Added

- **`PaginatedResponse<T>` generic network wrapper** (`es.mirumi.es.model.responses.PaginatedResponse`): One data class that mirrors Spring Data's `Page` JSON schema — `content`, `totalElements`, `totalPages`, `size`, `number`, `numberOfElements`, `first`, `last`, `empty`. Every field carries a safe default so a partial JSON payload never crashes deserialization. Used by every endpoint the backend paginated in `[0.4.0]`.

### Changed

- **`CasaAPI.getGastosCasa`**: Return type changed from `Response<List<Gasto>>` to `Response<PaginatedResponse<Gasto>>` to match the new backend wire format. Added optional `@Query` params (`page = 0`, `size = 100`, `sort = null`) with Kotlin defaults, so existing callers that don't pass them keep working. Retrofit drops null `@Query` params automatically, so `sort` stays out of the URL when unused.
- **`TareaAPI.getTareasByCasaId`**: Same treatment as gastos — return type is now `Response<PaginatedResponse<Tarea>>`. Added optional `@Query` params (`completado`, `page = 0`, `size = 100`, `sort`), enabling the "completed tasks board" filter without breaking existing callers.
- **`RepositoryCasa.getGastosCasa`**: Preserves the existing `Response<List<Gasto>>` contract that `GastosViewModel` depends on. Internally unwraps `.body.content`, wrapping it back into `Response.success(...)` for the happy path and forwarding `Response.error(...)` (with code + error body) on failure — no HTTP semantics lost.
- **`RepositoryTarea.getTareasByCasaId`**: Kept its `List<Tarea>` return signature. Now reads `.body()?.content` instead of `.body()`. `TareasViewModel` and `HomeViewmodel` need zero changes.

### Notes

- **UI layer decoupled**: No ViewModel, screen, or composable was modified. Pagination metadata (`totalPages`, `number`, etc.) is available at the API layer for a future scroll-to-load-more implementation but is intentionally not surfaced upward yet — matches the current UX which shows the full page in one shot.
- **Default page size = 100**: Chosen as a working ceiling for the current casa scale (small houses, ~dozens of gastos/tareas at most). Once the UI gains real pagination controls, callers should pass explicit `page` / `size`.
- **Backend contract match**: Aligns 1:1 with backend `[0.4.0] - Phase 3: Robustness & Pagination`.

### Fixed

- **Profile photo not reflecting in UI after successful upload (`PerfilViewModel`)**: The upload endpoint already returns the freshly-updated `UsuarioDTO`, but `subirFotoPerfil` was discarding it and firing `cargarPerfil()` — a second `GET /usuarios/{id}` — as a new coroutine. Simultaneously `PerfilScreen`'s `DisposableEffect` observes `ON_RESUME` (the photo picker returning) and *also* calls `cargarPerfil()`. Two racing coroutines could land the state on the pre-upload snapshot. Fix: consume the upload's own response body directly and update `_uiState` atomically — no second GET, no race. Applied the same treatment to `eliminarFotoPerfil` for symmetry.
- **Ranking avatar clipping bug — "1/8 of image, rest solid purple" (`RankingScreen`)**: `SubcomposeAsyncImage` was given an `error` slot but no `success` slot. In Coil 2.x that path fails to propagate the outer `contentScale = ContentScale.Crop` and `Modifier.fillMaxSize()` through the subcompose boundary, so the internal `Image` draws at intrinsic dimensions inside a bounded Box — leaving the purple background exposed for most of the circle. Rewrote both `PodiumItem` and `RankingListItem` avatar cells to use plain `AsyncImage` (no subcompose slots) layered on top of the fallback letter `Text`, with explicit `Modifier.fillMaxSize().clip(CircleShape)` + `ContentScale.Crop`. Result: bulletproof center-cropped circular avatars.
- **Coil cache-buster removed (`RankingScreen`)**: The `?v=${System.currentTimeMillis()}` suffix mutated on every recomposition, forcing a fresh network fetch every frame. Backend `FileStorageService` already generates UUID-based filenames (each upload gets a genuinely unique URL), so the cache-buster was purely wasteful. Dropped.
- **Profile screen avatar hardened (`PerfilScreen`)**: Same `AsyncImage` + fallback-letter overlay pattern as ranking, so both screens share one predictable rendering strategy across Coil versions.
- **Defensive URL resolver for avatars (`ImageUrlResolver`)**: Added `resolveImageUrl(raw: String?): String?` in `data/remote/ImageUrlResolver.kt`. Handles three shapes robustly:
  1. Already absolute (`http://…` / `https://…`) — passed through untouched.
  2. Root-relative (`/multimedia/uuid.jpg`) — prepended with the server origin derived from `BuildConfig.BASE_URL` (with the trailing `/api` stripped so the multimedia resource root resolves correctly).
  3. Bare filename (`uuid.jpg`) — treated as multimedia, prepended with `<origin>/multimedia/`.
  Returns `null` on null/blank input so the avatar cleanly falls back to the initial-letter placeholder. Wired into both `PerfilScreen` and `RankingScreen` (podium + list rows) — every Coil `model` value now flows through this helper, so future schema changes on the backend (relative paths, filename-only storage, environment-specific origins) don't ripple into the composables.

### Notes

- **Paired with backend fix**: This release lands together with backend `[0.4.0] Fixed → Profile / ranking avatars silently 401` — Coil's requests bypass `AuthInterceptor` entirely (separate OkHttp client), so the backend had to publicly permit `/multimedia/**` before any of these frontend changes could actually surface an image. Both sides must be deployed together.

### Changed (UX polish)

- **`AvatarConFoto` unified across gastos, tareas, and shared surfaces (`GastosScreen`)**: This composable is reused by expense list rows, expense detail cards, task list rows (via `TaskListItem`), and gasto participant chips. It was still on the old `SubcomposeAsyncImage` + `?v=${System.currentTimeMillis()}` cache-buster pattern — same failure mode as the previous ranking bug (Coil 2.x propagation of `contentScale`/`fillMaxSize` through subcompose slots) plus wasted network fetches on every `ON_RESUME`. Rewrote to use plain `AsyncImage` layered on top of the fallback initial letter, with the URL always flowing through `resolveImageUrl(...)` so avatars now render consistently everywhere the composable is used. Also removed the `LifecycleEventObserver` cache-busting — backend `FileStorageService` mints unique UUID URLs per upload, so Coil's URL-keyed cache is naturally correct.
- **Completed-task cards now use `AvatarConFoto` (`TareasScreen.TaskCardCompletada`)**: This card was still rendering `Image(painter = painterResource(id = R.drawable.ic_user))` — a static generic silhouette drawable — regardless of who actually completed the task. Swapped it for `AvatarConFoto(nombre = tarea.asignadoA?.nombre ?: "?", colorFondo = ColorLila, fotoUrl = tarea.asignadoA?.fotoUrl, size = 34.dp)` so the "Hecho por …" row now shows the same avatar treatment used everywhere else. Paired with the backend fix that stops dropping `fotoUrl` from `Tarea.asignadoA` in the API response.

### Fixed

- **Post-It drawing strokes disappearing for ~5 s after finger lift (`PizarraViewModel` + `PizarraView`)**: On `ACTION_UP` the local canvas already carried the freshly-drawn strokes, but `PizarraView.save()` fired `model.save()` (a non-suspending, fire-and-forget launch) followed **immediately** by `load()`. The next poll of `PizarraViewModel.load()` — running while the delta POST was still in flight — would fetch the *pre-stroke* server bitmap and hand it to `setBackgroundBitmap`, which rebuilt `currentBitmap` from scratch and wiped the local strokes. They only reappeared on the next 5-second poll tick after the server had actually processed the delta.
  - **First-pass fix**: introduced a simple `Boolean` `pendingSave` gate. Worked flawlessly for the first stroke, but broke down on every subsequent one — a stale save's success callback would clear `pendingSave = false` while a new `ACTION_UP` was already in progress, so the next poll wiped that new stroke instead. Same visible 5-second delay, one stroke later.
  - **Robust fix (this release)**: replaced the boolean with a monotonic version pair on `PizarraViewModel`:
    - `_dirtyVersion: MutableStateFlow<Long>` — incremented on every `markPending()` call (each `ACTION_UP`).
    - `_syncedVersion: MutableStateFlow<Long>` — advanced via `_syncedVersion.update { maxOf(it, versionAtDispatch) }` inside `save()` once the API call returns, where `versionAtDispatch` is the dirty snapshot captured *at the moment that specific save was dispatched*.
    - `pendingSave: StateFlow<Boolean> = combine(_dirtyVersion, _syncedVersion) { d, s -> d > s }.stateIn(…)`.
    This makes the gate correct regardless of how many saves are in flight, how many overlap, or in what order their responses arrive — a stroke drawn during an in-flight save advances `_dirtyVersion`, so even when the earlier save's response advances `_syncedVersion` first, `pendingSave` stays `true` until the *later* save also completes. `maxOf` on the ack guarantees monotonicity against out-of-order responses.
  - **Data-loss side fix**: while there, hardened `save()` — the previous code passed the mutable `puntos` list to `postDelta` and then called `puntos.clear()` in the success branch. Strokes added by the user *during* the round-trip were silently swallowed by that `.clear()`. Now `save()` snapshots `puntos.toList()` and clears the buffer immediately, so new strokes accumulate into a fresh buffer independent of the in-flight batch.
- **Post-It infinite save loop hammering the backend (`PizarraView`)**: Backend logs showed `Se están aplicando deltas → UPDATE lienzo` firing every few seconds even when nobody was drawing. Two coupled defects were compounding:
  1. `PizarraView.save()` was calling `load()` at the end of its debounced coroutine. Every touch stroke restarted the entire polling loop, and any callsite that reached `save()` outside a real touch (recompositions, lifecycle callbacks, bitmap-arrival collectors touching the View while `activatedDraw` was true) chained through into another restart.
  2. There was no physical-touch gate on `save()` — since the method is `private`, we relied entirely on onTouchEvent's `ACTION_UP` branch being the only caller, which turned out to be fragile.
  - **Fix**: (a) removed the `load()` call from inside `save()` — polling is now started exactly twice: once from `onSizeChanged` when the View first mounts, and once from the `AndroidView` `update` block when `lienzoId` changes on Post-It swap. (b) added an explicit physical-touch gate at the top of `save()`: `if (!model.pendingSave.value) return`. `pendingSave` is set to `true` only by `model.markPending()`, which is only called from the `ACTION_UP` branch — so any spurious/non-touch invocation now no-ops instead of scheduling a debounced POST. Added a doc comment on `setBackgroundBitmap` reaffirming it is a programmatic-only entry point and must never trigger the save cycle.
- **Gasto row avatars only appeared after navigating into and out of the detail screen (`GastosScreen.ItemGasto`)**: `GastosViewModel.cargarUsuariosCasa()` and `cargarGastos()` run in parallel from `init`. On the first render, `_usuariosDetectados` was still an empty `MutableStateFlow`, so `viewModel.getFotoPorNombre(pagadorNombre)` returned `null` and every row showed the letter placeholder. `getFotoPorNombre` reads `_usuariosDetectados.value` directly — a snapshot read that does NOT create a Compose subscription — so when the members list eventually loaded, `ItemGasto` never recomposed. Only re-entering the screen forced a fresh render with the populated map.
  - **Fix**: added `val usuarios by viewModel.usuariosDetectados.collectAsState()` at the top of `ItemGasto` and derived `fotoAvatar = usuarios.find { it.nombre == pagadorNombre }?.fotoUrl` from that state. The `by collectAsState()` delegation creates a proper Compose subscription; the moment the members flow emits, every visible `ItemGasto` recomposes and the avatars pop in with zero navigation dance.
- **Lista rows still showed a generic silhouette even though the backend now sends `fotoUrl` (`ListasScreen.UserAvatar`)**: The composable had a hardcoded `Image(painter = painterResource(id = R.drawable.ic_user), colorFilter = ColorFilter.tint(Color.DarkGray))` — an intentional placeholder marked with the comment "Para producción usar Coil/Glide" — that never consulted `user.fotoUrl`. So even after the paired backend `[0.4.0]` fix that stopped dropping `fotoUrl` from `Lista.propietario` and `Lista.participantes`, the drawable never changed. Replaced the static `Image` with `AvatarConFoto(nombre = user.nombre, colorFondo = ColorLila, fotoUrl = user.fotoUrl, size = size)` so list owners and participant chips now share the same avatar rendering used everywhere else in the app.
- **Completed-task avatars only appeared after a manual action (`TareasScreen.TaskCardCompletada`)**: The same class of state-binding bug we fixed on `GastosScreen.ItemGasto`. `TaskCardCompletada` read `tarea.asignadoA?.fotoUrl` directly — a plain property read that Compose does not track for recomposition. `cargarMiembros(token)` fires from a `LaunchedEffect(Unit)` when the screen enters, so on the first render the members flow is still empty. And critically, the `Tarea` objects loaded by `cargarTareas` may arrive with `asignadoA.fotoUrl == null` (older tareas stored before the backend `[0.4.0]` fotoUrl propagation fix, or races where the JPA fetch loaded `Usuario` proxies without hydrating the `fotoUrl` column). The card had no subscription to anything that would update, so it stayed on the letter placeholder until the user completed another task — which forced a `cargarTareas` → LiveData emission → whole-screen recomposition.
  - **Fix**: added a `viewModel: TareasViewModel` parameter to `TaskCardCompletada`, observed the existing `miembros` LiveData inside via `val miembros by viewModel.miembros.observeAsState(emptyList())`, and derived `fotoUrl = miembros.firstOrNull { it.id == tarea.asignadoA?.id }?.fotoUrl ?: tarea.asignadoA?.fotoUrl`. The `by observeAsState()` delegation creates the exact same kind of subscription that Compose's `collectAsState()` gives us for StateFlow — the moment `cargarMiembros` populates the flow, every visible completed-task card recomposes and derives its avatar URL from the fresh members map. The fallback to `tarea.asignadoA?.fotoUrl` keeps things sensible when a former member (no longer in `miembros`) still owns a historical completed task. Updated the call site in the completed-tasks `LazyColumn` to pass the `viewModel` argument.

## [0.2.0] - 2026-07-13

### Changed

- **Encuesta API route alignment**: `EncuestaAPI` endpoints updated to match backend REST standardization (`api/casa/{id}/lista-encuestas` → `casas/{id}/encuestas`; voting and close actions likewise updated).
- **Removed X-User-Id header from Encuesta calls**: All `EncuestaAPI` methods no longer send the `X-User-Id` header; user identity is now derived server-side from the JWT token (already attached by `AuthInterceptor`).
- **RepositoryEncuesta**: Constructor no longer requires `SessionManager` — authentication is handled transparently by the OkHttp interceptor.
- **EncuestasViewModel / EncuestasViewModelFactory**: Removed `SessionManager` dependency (no longer needed by repository).

## [0.1.0] - 2026-07-12

### Added

- **OkHttp Auth Interceptor**: Centralized `AuthInterceptor` in `NetworkModule` that automatically attaches `Authorization: Bearer <token>` to all outgoing requests when a session exists. Skips requests that already carry an explicit `@Header("Authorization")` to avoid duplication.
- **EncryptedSharedPreferences**: Migrated `SessionManager` from plain `SharedPreferences` to `EncryptedSharedPreferences` (AES-256-GCM key encryption, AES-256-SIV value encryption via AndroidX Security Crypto 1.1.0-alpha06).
- **NetworkModule Initialization**: `NetworkModule.init(context)` centralizes SessionManager creation and OkHttpClient wiring; `MainActivity` calls it before any composable mounts.

### Changed

- **SessionManager**: Primary constructor now accepts `SharedPreferences` (internal visibility for testability); public constructor auto-creates encrypted preferences via `MasterKey`.
- **RepositoryLogin**: Auth token is saved to `SessionManager` immediately after a successful login response, before any authenticated follow-up calls (fixes FCM token registration 403).
- **NetworkModule**: Retrofit instance now uses a configured `OkHttpClient` with the auth interceptor instead of the default client.

### Fixed

- **Post-login 403 on FCM token registration**: `updateUsuarioToken` was called before the JWT was persisted, causing the interceptor to send unauthenticated requests.

### Security

- Auth tokens encrypted at rest on device (AES-256-GCM via Android Keystore-backed MasterKey).
- Token no longer stored in plaintext SharedPreferences.

## [0.0.1] - 2026-06-01

### Added

- Initial Jetpack Compose + Kotlin Android application.
- Screens: Login, Home, Casa management, Tareas, Listas, Eventos, Pizarra, Gastos, Invitaciones, Perfil.
- Retrofit 2.9.0 + OkHttp 4.12.0 networking layer.
- Firebase Cloud Messaging integration.
- QR code scanning for casa invitations.
- Biometric authentication support.
- MQTT real-time updates for Pizarra.
- Product flavors: `emulator` (10.0.2.2) and `realDevice` (dynamic IP).
