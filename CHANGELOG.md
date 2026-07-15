# Changelog

All notable changes to the MiRumi Android Client will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.5.0] - 2026-07-14 - Phase 4: Registration & UI consistency

### Added

- **End-to-end registration flow (`ui/registro/`)**. New user can go from the "¿No tienes cuenta? Regístrate" link at the bottom of the login screen straight to a full registration form and land in `ListaCasas` without ever touching the login screen again.
  - `RegistroScreen.kt` — Compose form with four fields (nombre, correo, contraseña, confirmar contraseña), password visibility toggles on both password fields, disabled-until-non-blank submit button, `CircularProgressIndicator` in the button while the request is in flight. Uses a real `Scaffold + verticalScroll + Column` layout (not the Figma-export absolute-offset style of the live login screen) so it scales cleanly across screen densities. Every color/shape flows through the new theme tokens — `Fondo`, `Burgundy` (primary CTA), `TextoGris` (placeholder + hint text), `ButtonShape`, `InputShape`.
  - `RegistroViewModel.kt` + `RegistroViewModelFactory.kt` — copy of the `LoginViewModel` sealed-state pattern (`RegistroUiState { Idle, Loading, Success(LoginResponse), Error(message) }` as `StateFlow`, `resetState()`). Client-side validation happens in the ViewModel before any network call: non-blank on all four fields, `android.util.Patterns.EMAIL_ADDRESS` for correo, ≥ 6 chars for contraseña, string-equality between contraseña and confirmar.
  - `RepositoryRegistro.kt` — mirror of `RepositoryLogin`. Unwraps `Response<LoginResponse>`, persists the JWT via `NetworkModule.sessionManager.saveAuthData(...)` so the interceptor picks it up on subsequent requests, kicks off FCM token registration exactly like login does. Maps `409` → "Ese correo ya está registrado" and `400` → "Datos inválidos. Revisa los campos e inténtalo de nuevo." — surfaces as a `Toast` from the screen.
  - `RegistroAPI.kt` — `@POST("api/auth/register")` returning `Response<LoginResponse>`. Path matches the existing `LoginAPI.@POST("login")` no-leading-slash convention so `BuildConfig.BASE_URL` joins the same way.
  - `RegistroRequest.kt` (`model/requests/`) — Gson-annotated mirror of the backend DTO.
  - `NetworkModule.registroApiService` — the standard `by lazy { retrofit.create(...) }` service registration line, alongside `loginApiService`.
  - `Route.Registro : Route("registro")` — added to `ui/navigation/Routes.kt` next to `InicioSesion`.
  - `AppNavigations.kt` — `composable(Route.Registro.route) { RegistroScreen(navController, sessionManager) }` block right after `Route.InicioSesion`.
  - `PrincipalInicioSesin.kt:127` — `onRegisterClick` no longer fires a `Toast("Ir a Registro")` stub; it now navigates `navController.navigate(Route.Registro.route)`.
- **`ui/theme/AppColors.kt` — single source of truth for the palette.** `Fondo`, `LilaLight`, `LilaCard`, `LilaPrimary`, `LilaDark`, `Burgundy`, `TextoGris`, `VerdeSaldo`, `RojoSaldo`. Naming reflects role rather than hue — `LilaPrimary` is the brand purple, `Burgundy` is the auth-flow CTA, `LilaDark` is the deeper accent for headers/checkboxes.
- **`ui/theme/AppShapes.kt` — corner-radius tokens.** `ButtonShape = RoundedCornerShape(12.dp)`, `CardShape = RoundedCornerShape(15.dp)`, `InputShape = RoundedCornerShape(15.dp)`. Every button/card/text-field should reference these.

### Changed

- **Sweep-replaced 60+ inline hex literals with tokens.** Files touched: `PerfilScreen` (17 hits), `TareasScreen` (16), `PrincipalInicioSesin` (6), `RankingScreen` (5), `ListaCasasScreen` (5), `Tareas.kt` (3), `AppNavigations`, `PrincipalInicio`, `GestionUsuariosPiso`, `Home`, `TinderTaskCard`, `ItemScreen`, `InvitacionesScreen`, `ListasScreen`. Every `Color(0xff8061a2)` / `Color(0xFF8061A2)` → `LilaPrimary`; every `Color(0xff5d427a)` → `LilaDark`; every `Color(0xff581327)` → `Burgundy`; every `Color(0xff6c6c6c)` in `TareasScreen` → `TextoGris`. Post-sweep grep of the codebase for those five hex strings returns zero literal hits outside `AppColors.kt` — a hard invariant.
- **`GastosScreen.kt` local const block re-aliased.** `ColorFondo = Fondo`, `ColorLila = LilaLight`, `ColorLilaClaroTarjeta = LilaCard`, `ColorLilaSelected = LilaLight`, `ColorVerdeSaldo = VerdeSaldo`, `ColorRojoSaldo = RojoSaldo`, `ColorTextoGris = TextoGris`, `ColorMoradoOscuro = LilaDark`. All ~100 downstream imports of these names keep resolving — the actual hex values now live in `AppColors.kt`. Same alias treatment applied to `GestionUsuariosPiso.kt`'s local `PurplePrimary`, `BackgroundColor`, `TextGray` constants.
- **Radii normalized to `ButtonShape` / `CardShape`.** `PerfilScreen` logros button (was 15dp) and logout button (was inline 12dp) → `ButtonShape`. `GastosScreen` `ItemGasto` Card (was 24dp) → `CardShape`; balance/saldo Cards (were 16dp × 4 sites) → `CardShape`; "Modificar" OutlinedButton (was `RoundedCornerShape(50)` — a pill) → `ButtonShape`. `TareasScreen` "Completar" OutlinedButton (was 8dp) and "Aceptar" Button (was 20dp) → `ButtonShape`.
- **`TareasScreen` gray-CTA / gray-border fixes.** "Aceptar" primary Button's `containerColor` was `Color(0xff6c6c6c)` (mid-gray, wildly off-brand) → `LilaPrimary`. "Completar" OutlinedButton's `border` and `contentColor` were also mid-gray → `LilaPrimary`. `NewCreateTaskDialog` TextField border overrides (`focusedBorderColor` / `unfocusedBorderColor` both forced to gray at 4 call sites) → focused = `LilaPrimary`, unfocused = `TextoGris`. Focus states now look like the rest of the app.
- **`TareasScreen` + `ListasScreen` header rewritten from absolute-offset positioning to `Scaffold + Column`.** Both screens used a Figma-export layout for their title + tab-pill + list header — `.align(Alignment.TopStart).offset(x = 20.dp, y = 15.dp)` for the title, `.offset(x = 65.dp, y = 75.dp)` for the tab pill, `.offset(y = 115.dp)` for the LazyColumn, `.offset(y = 400.dp)` on `ListasScreen`'s empty-state text. That layout broke on any screen density other than the 390dp × 850dp target the designer laid it out on. Rewrote both to `Scaffold(containerColor = Fondo) { paddingValues -> Column(...) { Spacer; Text(title); Spacer; Box(tab-pill, centered horizontally); Spacer; LazyColumn(...) } }`. Individual card composables (`TaskCardCompletada`, `ItemGasto`, `TaskListItem`, `ShoppingListItemCard`) and all ViewModel data flow are untouched — the surgery is scoped to the two headers.

### Removed

- **`ui/login/LoginScreen.kt`** (orphan blue-themed login using `Color(0xFF1976D2)`) and **`ui/login/Login.kt`** (dead-code stub with a commented-out `LoginDestination` body). Neither was referenced from `AppNavigations` — the live login is `ui/home/PrincipalInicioSesin.kt`. Deleting both eliminates the "why is there a blue login screen in the codebase?" mystery and prevents future greps from surfacing a phantom variant. `LoginViewModel.kt` + `LoginViewModelFactory.kt` are kept in `ui/login/` since `PrincipalInicioSesin` still consumes them.

### Notes

- **Paired with backend `[0.5.0]`.** Together they close the auth surface — registration is a full-stack feature and both sides must be deployed together. Backend gained `POST /api/auth/register` + BCrypt hashing + `409` on duplicate email, and drive-by fixed the login response silently dropping `fotoUrl` (same category of bug as the Phase 3 Tarea/Lista DTO fix).

### Fixed (post-release nav + polish patch)

- **Welcome-screen "Regístrate" was a dead link (`PrincipalInicio.kt`)**: The buildAnnotatedString for "¿No tienes cuenta todavía? Regístrate" at the very-first landing screen was rendered as pure decoration — no `Modifier.clickable` was attached. Only the "Iniciar sesión" button worked, so a brand-new user with no way to log in was stuck. Added `.clickable { navController.navigate(Route.Registro.route) }` to the Text modifier, matching the register wiring already in place on `PrincipalInicioSesin`.
- **`RegistroScreen` back arrow hardened**: The `onBackToLoginClick` handler was `{ navController.popBackStack() }` — correct for the normal login → registro flow, but silently no-ops if the back stack is empty (process death restoring the user directly to `Route.Registro`, or an internal `popUpTo(0){inclusive=true}` having cleared the stack). Now wrapped as `if (!navController.popBackStack()) { navController.navigate(Route.InicioSesion.route) { popUpTo(Route.Registro.route){inclusive=true} } }` — the arrow always resolves to a live navigation, never a dead click.
- **Token-hex leaks in `TareasScreen` + `ListasScreen`**: A visual-consistency audit surfaced ~20 additional inline `Color(0xffddc1fb)`, `Color(0xff6c6c6c)`, `Color(0xfff8f8f8)` literals overriding the tokens on both header-rewritten screens. These weren't picked up in the initial Phase 4 sweep (which targeted the three brand-purple + burgundy patterns). Swept them:
  - Every `Color(0xffddc1fb)` in TareasScreen + ListasScreen → `LilaLight` (tab-pill selected background, "media" priority badge fondo, ranking selector chip fondo, list-item selection highlight — visible in the priority chips on the create-task dialog and the compartida-list selector on Listas).
  - Every `Color(0xfff8f8f8)` in TareasScreen → `Fondo`.
  - Every `Color(0xff6c6c6c)` in ListasScreen → `TextoGris`.
  - Added `TextoGris` import to ListasScreen.
  - Result: `TareasScreen` + `ListasScreen` now render their tokens end-to-end — no local hex literal overriding the palette. The visual diff since the Phase 4 landing was masked by these overrides; with them gone, the header rewrite + tab-pill lila + priority badges + focus borders all read the same purple across screens.
- **`.offset()` audit clean**: `grep .offset\(` on both files returns exactly ONE hit each, both intentional decorative overlays (a badge at `Alignment.TopEnd` on a dialog, and the participant-avatar stack overlap via `.offset(x = (-20 * index).dp)`). All Figma-export header offsets are gone.

### Changed (premium polish patch)

- **CRITICAL — `RegistroScreen` back arrow clipped by status bar (`Modifier.statusBarsPadding()`)**: The root `Box` was rendering behind the system status bar (notifications / battery / time). The IconButton at the top-left was visually cut off AND the system was intercepting the touch region, so on real devices the back arrow was completely dead (worked in emulators only because their chrome hid the issue). Added `.statusBarsPadding()` to the root Box's modifier chain — pushes the whole layout safely below the status bar. Arrow now fully visible and fully clickable.
- **`RegistroScreen` premium rhythm**: bumped horizontal padding `20dp → 24dp` for breathing room, field-to-field spacing `12dp → 16dp` (matching the 16dp vertical rhythm the plan specified), gap after the copy block `32dp → 40dp`, primary button height `52dp → 56dp` (thumb-friendly). Added `elevation = buttonElevation(defaultElevation = 4dp, pressedElevation = 2dp)` on the "Crear cuenta" CTA so it lifts off the border-only form. IconButton bumped to `.size(48dp)` for a hit target that comfortably clears the Android touch-target minimum.
- **`RegistroScreen` brand focus states**: introduced a shared `focusedFieldColors` object — `focusedBorderColor = LilaPrimary`, `unfocusedBorderColor = TextoGris.copy(alpha = 0.4f)`, `cursorColor = Burgundy`. Applied uniformly to all four `OutlinedTextField` callsites so the whole form reads as a single family and users get an unmistakable "you tapped this field" cue in the brand purple.
- **`PrincipalInicioSesin` brand focus states + CTA elevation**: same `focusedFieldColors` pattern applied to email + password fields (via new `LilaPrimary`, `TextoGris`, `InputShape`, `ButtonShape` imports — the wildcard hex-color literals swept in the earlier pass now flow through tokens). Login button bumped `height 52dp → 56dp`, `shape = ButtonShape`, `elevation = buttonElevation(defaultElevation = 4dp, pressedElevation = 2dp)`, text `fontWeight = SemiBold`. Text-field shapes swapped to `InputShape` — no more inline `RoundedCornerShape(15.dp)`.
- **`GastosScreen` card + button elevation**:
  - `ItemGasto` Card `elevation = 0dp → cardElevation(defaultElevation = 2dp, pressedElevation = 4dp)` — every expense row now floats over `Fondo` with a subtle shadow, cueing tappability.
  - `ItemSaldo` Card and balance-summary Card `elevation = 0dp / 1dp → 2dp` (both call sites via `replace_all`) — same lift as `ItemGasto` for visual consistency in the Saldos tab.
  - "Saldar Deudas" Button — `shape = RoundedCornerShape(12dp) → ButtonShape`, `height 52dp`, `elevation = buttonElevation(defaultElevation = 3dp, pressedElevation = 1dp)`. Now reads as a real primary CTA instead of a flat pill.
- **`PerfilScreen` tarjeta usuario + logout CTA elevation**:
  - Header tarjeta (the row that shows the avatar + nombre + Cheff del piso) was a `Row` with `.clip + .background` and no shadow — added `.shadow(elevation = 2.dp, shape = RoundedCornerShape(15.dp))` before the clip so it lifts off `Fondo` like every other card.
  - "Cerrar sesión" Button — height `50dp → 52dp` (matching the other primary CTAs), `elevation = buttonElevation(defaultElevation = 3dp, pressedElevation = 1dp)`, text `fontWeight = Bold → SemiBold` (tighter with the CTA family).

### Performance (Post-It initial load — 3-4s → sub-second)

- **Halved the initial-open network cost by skipping the `isUpdated` shortcut on first hydration (`PizarraViewModel.initialLoad`)**. On Post-It open, the polling loop was calling `load()` — which does *two* sequential network round-trips: (1) `GET isUpdated(lienzoId, lastLoaded)` — a boolean check that answers "has the server bitmap changed since we last loaded?" and (2) `GET getLienzo(lienzoId)` — the bitmap fetch. On a *fresh* open there's no prior local snapshot, so the isUpdated check is pure overhead — the answer is always "yes, load it". Extracted a new `initialLoad()` fast path that skips isUpdated and goes straight to getLienzo. The poll cycle (subsequent iterations) keeps using `load()` — the isUpdated shortcut is worth its cost when 90 %+ of poll ticks return `false` and never fetch the bitmap.
- **Extracted `fetchAndPublishBitmap()` — single shared network + decode + emit path**. Both `initialLoad()` and `load()` now converge on it, so behavior stays consistent. Every heavy operation runs on `Dispatchers.IO`:
  - `responseBody.bytes()` — the raw read from the response stream.
  - `BitmapFactory.decodeByteArray(bytes, 0, bytes.size)` — decoding a full-canvas PNG can cost 100-500 ms on mid-range devices; doing it on `Main` would drop frames and freeze the drawing UI during navigation.
  - Only the terminal `_bitmapState.value = bitmap` emission hops back to `Main` via `withContext(Dispatchers.Main)`. This is the correct dispatcher for StateFlow updates that drive UI recomposition.
- **`_isLoading: StateFlow<Boolean>` exposed on `PizarraViewModel`** — toggled `true` when `initialLoad()` enters, `false` in a `finally` block after hydration completes (success OR failure — the spinner never gets stuck).
- **Async-hydration spinner overlay on `ExpandedPostIt` (`Draggable.kt`)**. The canvas Box now hosts a second layer: a centered `CircularProgressIndicator` in `LilaPrimary` at `40.dp`, visible only while `viewModel.isLoading.collectAsState()` is `true`. Sits on top of the still-empty canvas so the user gets an immediate visual cue that content is coming, instead of a 3-4 s blank stall. Disappears the instant the bitmap emits.
- **`Draggable.kt` `LaunchedEffect` swapped `viewModel.load()` → `viewModel.initialLoad()`** — was calling the slow-path load which paid the isUpdated RTT on first hydration too. Both the `LaunchedEffect` and the `AndroidView.update`'s `view.load()` are idempotent because they converge on the same `fetchAndPublishBitmap` inside the ViewModel — safe to have both entry points, redundancy protects against timing edge cases.
- **`PizarraView.load()` restructured** — the polling coroutine now runs `initialLoad()` on the FIRST iteration (fast path, no `delay(5000)` before it), then falls into the standard `while (isActive) { delay(5000); load() }` poll cycle. Previously the very first iteration was subject to the same `delay(5000)` wait AFTER the load() completed — meaning even after a fast successful fetch, the next iteration was 5 s away. Now the fast path fires immediately on open, and the poll cadence only kicks in AFTER hydration is done.

**Net result**: initial Post-It open goes from ~3-4 s stall to a spinner appearing instantly + bitmap swapping in within one network round-trip + decode (~500-1200 ms on typical mobile connections). No blocking of the Main thread, no dropped frames during navigation.

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
