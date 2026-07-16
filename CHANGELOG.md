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

### Performance (Post-It reopen — sub-second → 0 ms via in-memory cache)

- **New `PizarraBitmapCache` — process-lifetime bitmap store keyed by `lienzoId`** (`ui/pizarra/PizarraBitmapCache.kt`). Backed by `ConcurrentHashMap<Long, Entry>` where each `Entry` holds both the decoded `Bitmap` and the `Instant` at which it was populated (needed for the silent-refresh baseline — see below). `object`-scoped so it survives the ViewModel lifecycle (VM instances die when the ExpandedPostIt leaves composition, but the JVM `object` lives for the whole app process). Reads happen from Main (via ViewModel), writes from `Dispatchers.IO` (after the decode) — `ConcurrentHashMap` handles the concurrency without manual locking.
- **Three-tier hydration ordering in `initialLoad()`** — fastest to slowest:
  1. **Cache hit → 0 ms** — emit the cached bitmap to `_bitmapState`, leave `isLoading = false` from the start (no spinner needed), seed `lastLoaded` from the cached timestamp so the follow-up `isUpdated` check has an accurate baseline, then **fire a silent background refresh** via the existing `load()` (which uses the cheap `isUpdated` boolean shortcut and only re-fetches + re-emits if the server has actually changed since the cache was populated). User sees the drawing instantly on reopen — total perceived latency zero — and any peer edits made in the meantime silently swap in a moment later.
  2. **Cache miss (first-ever open in this process)** — original fast-path behavior kicks in: `isLoading = true`, spinner overlay, single-RTT `fetchAndPublishBitmap()`, cache write, `isLoading = false`.
  3. Ongoing sync via the 5-second poll loop is unchanged.
- **Silent-refresh timestamp accuracy**: the AndroidView `update` block resets `viewModel.lastLoaded = Instant.ofEpochMilli(1000000)` on every Post-It swap so a fresh ViewModel starts from "very old" (guarantees the first `isUpdated` returns true on cache-miss paths). On cache hits that default would trigger a wasteful full-bitmap re-fetch on the very first background poll — so `initialLoad()` restores `lastLoaded` from `PizarraBitmapCache.timestampFor(lienzoId)` right after the cache emit. The follow-up `isUpdated(lienzoId, cachedTimestamp)` now compares against the real baseline and correctly returns `false` when nothing has changed server-side. Net: cache-hit reopen usually costs ONE cheap boolean RTT (the isUpdated check) and zero bitmap bytes over the wire.
- **`fetchAndPublishBitmap` writes to the cache on every successful decode**. Both `initialLoad()`'s cache-miss path and `load()`'s poll-cycle re-fetches route through it, so the cache stays current with the freshest server bitmap for the rest of the process's lifetime. The bitmap emitted to Main and the one stashed in the cache share a reference — safe because `PizarraView.setBackgroundBitmap` calls `.copy(Bitmap.Config.ARGB_8888, true)` before drawing new strokes onto its own canvas, so the cached original is never mutated.
- **`PizarraBitmapCache.clear()` exposed** for a future `SessionManager.logoutUser()` hook — prevents the next user on a shared device from ever seeing the previous user's drawings. Not wired yet but there when we need it.

**Net result on reopen**: the drawing appears **instantly** the moment the ExpandedPostIt renders — no network, no decode, no spinner. If the server has been modified in the meantime (e.g. a housemate drew a new stroke on their device), a silent isUpdated boolean check confirms the change and the fresh bitmap swaps in a moment later. The user perceives zero latency; peer edits materialize seamlessly in the background.

### Fixed (cache staleness after local save)

- **`PizarraBitmapCache` served stale pre-edit bitmap after a local draw + minimize (`PizarraViewModel.save`)**: The cache was only populated by `fetchAndPublishBitmap()` — the save path never touched it. So the sequence "draw → strokes rendered locally → save fires → user minimizes → reopens the same Post-It" hit the still-warm cache, which held the *pre-draw* snapshot. Silent refresh MIGHT have fixed it eventually, but two things could combine to make the stale state permanent:
  1. If the save coroutine was cancelled mid-flight by `viewModelScope` clearing on minimize (common when the user minimizes fast), the server-side POST might have completed but the client-side ack + cache write never ran.
  2. If `pendingSave` was still `true` at the moment the silent-refresh emission fired, `setBackgroundBitmap` would skip the update (correct behavior — protects newer local strokes), leaving `currentBitmap` stuck on the cached-stale bitmap indefinitely because no further emission would arrive.
  Also produced the "3-4 s lag on the main whiteboard layout" symptom: because nothing invalidated the cache on save, the ONLY way the fresh bitmap ever landed was the next 5-second poll tick.
  - **Fix**: rewrote `save()` with three moves:
    1. **Pessimistic cache invalidation** — `PizarraBitmapCache.remove(lienzoId)` runs SYNCHRONOUSLY before dispatching the network coroutine. Even if the coroutine is cancelled by ViewModel disposal, the stale entry is already gone. Worst case: next open misses the cache and pays a one-time spinner + fresh fetch — but never serves stale pixels.
    2. **Instant baseline update** — on `ApiResult.Success`, `lastLoaded = Instant.now()`. The next poll cycle's `isUpdated(lienzoId, now)` now compares against an accurate post-save reference instead of the pre-save timestamp.
    3. **Post-save cache warm** — also on success, calls the existing `fetchAndPublishBitmap()` which pulls the freshly-composited server bitmap, writes it back to the cache, and emits it to `_bitmapState`. The emission is safe: `setBackgroundBitmap`'s `pendingSave` guard either applies it (no newer local strokes → identical pixels, no visible change) or skips it (newer local strokes → preserved on the canvas). Either way the CACHE ends up warm with the correct bitmap, so the very next reopen hits the cache and stays zero-latency.
- **New `PizarraBitmapCache.remove(lienzoId)` method** — surgical single-key eviction, distinct from `clear()` (which is reserved for the eventual sign-out hook). Documented that the ordering matters: eviction happens *before* the async work so it survives coroutine cancellation.

**Net result**: draw → strokes appear instantly (optimistic UI, unchanged) → save fires in background → cache is evicted the moment save is called, re-warmed with the fresh server bitmap the moment save succeeds → next reopen shows the drawing WITH the new strokes at zero latency. No 5-second poll wait, no stale pre-draw snapshot, no permanent stuck state even under fast minimize.

### Added (Post-It customization — new bottom control panel)

- **`PostItControlPanel` — new bottom sheet on `ExpandedPostIt`** (`ui/pizarra/postits/Draggable.kt`). Matches the "Crear post it" design mockup: a white rounded-top sheet flush against the outer note, hosting four labeled sections in a tight 10dp vertical rhythm:
  1. **"Color de la nota"** — `NoteColorSelector` — horizontal row of 5 pastel dots (`NOTE_PASTELS`: Yellow / Green / Blue / Purple / Pink). Selecting instantly changes the outer note sheet's background color via hoisted `noteColor` state.
  2. **"Color del pincel"** — `BrushColorSelector` — horizontal row of 7 vibrant dots (`BRUSH_SWATCHES`: Yellow / Green / Blue / Purple / Fuchsia / Black / White). Each swatch carries a `byteCode` matching the wire protocol (`PointDeltaDTO.color: Byte`). Selection pushes the byte straight into `PizarraViewModel.color` via `LaunchedEffect(brushByte)` — the very next `ACTION_DOWN` `createPaint()` call reads the new byte and renders strokes in the chosen color.
  3. **"Enviar nota a"** — `AssigneeChips` — horizontal row of pill-shaped member chips (`⊕ Name`). Selection toggles: tapping the selected chip clears the assignment. In-memory only for this pass (backend assignee relation is a follow-up); shows "Cargando miembros…" placeholder when no member list is passed.
  4. **Action row** — `PostItActionRow` — three equal-weight OutlinedButtons with `ButtonShape` (12dp) corners:
     - **Borrar** (DeleteOutline icon, `TextoGris` accent) → calls the new `PizarraView.clearCanvas()` imperatively via a captured view ref → wipes local `currentBitmap` to a fresh white bitmap, evicts `PizarraBitmapCache` entry, marks `pendingSave` dirty so the next save flush picks up the cleared state.
     - **Dibujar** (Create/pencil icon, `LilaPrimary` accent) → currently a visual affordance; brush selection is already active in real time via the `LaunchedEffect(brushByte)` above. Reserved for future palette-mode toggles (eraser vs brush).
     - **Enviar** (Send icon, `Burgundy` accent) → stops the ViewModel's polling, clears `_bitmapState`, and calls `onMinimize()` to return to the pizarra board. Any in-flight strokes flush via the existing debounced save.
- **New palette + swatch data classes** in the same file — `NOTE_PASTELS: List<Color>`, `BrushSwatch(byteCode: Byte, color: Color)`, `BRUSH_SWATCHES: List<BrushSwatch>`, `Color.toHex()` extension for the future `PUT /postits/{id}/color-nota` persistence call.
- **`ColorSwatchDot` composable** — 28dp idle / 32dp selected circle with a burgundy-bordered "selected" state. Special-cased white-swatch border (gray at 47.4% instead of default 74%) so the white brush option doesn't disappear into the card background.

### Changed (ExpandedPostIt layout + brush palette)

- **`ExpandedPostIt` restructured to `Column [ TopBar(pills + smiley), Canvas, ControlPanel ]`** (`Draggable.kt`). Outer sheet dropped the fixed `requiredHeight(420dp)` and now wraps content so the sheet grows to fit the control panel. Width bumped `350 → 360dp` for the extra chip breathing room. Padding restructured (`top = 16, start = 16, end = 16, bottom = 4`) so the panel sits flush against the sheet's bottom edge — the panel itself owns its inner padding.
- **Pill header restyled** — `InputChip` colors swapped from the ad-hoc `Color(0xffb1395b)` / `Color(0xFFFFE9EF)` to the `Burgundy` / `Color.White` token pair from `AppColors.kt`, matching the mockup's dark-burgundy header. Smiley (`R.drawable.cararosa`) pinned to the top-right via `Modifier.weight(1f)` + `alignment = Alignment.CenterEnd` instead of the previous `.offset(0.dp, 5.dp).fillMaxWidth()` hack.
- **Sheet background dynamic** — the outer Column's `background(...)` now reads from the hoisted `noteColor` state (starts at `NOTE_PASTELS.first()` = pastel yellow) instead of the hardcoded `Color(0xffffcddb)`. Selecting a new pastel in "Color de la nota" recolors the entire note in real time.
- **`PizarraView.createPaint` brush palette expanded** — was 5 colors keyed on bytes 1-4 + 8 (Black / Red / Green / Blue / White). Now 7 colors keyed on bytes 1-7 matching `BRUSH_SWATCHES`: 1=Yellow, 2=Green, 3=Blue, 4=Purple, 5=Fuchsia, 6=Black, 7=White. Unknown bytes still fall back to Black — legacy strokes drawn with the old palette (e.g. old `2` = Red) will render Green now, an intentional migration trade-off since re-encoding historical strokes would require a canvas-level version bump.

### Added (PizarraView method)

- **`PizarraView.clearCanvas()`** — new public method invoked by the "Borrar" control-panel button. Creates a fresh white `Bitmap` of the current view size, resets `currentBitmap`, `canvasBitmap`, `backgroundBitmap`, `path`, and `lastPoint`, then calls `PizarraBitmapCache.remove(model.lienzoId)` + `model.markPending()` + `invalidate()`. The cache eviction guarantees no future reopen serves the pre-clear bitmap. Note: the current delta-only wire protocol has no explicit "clear" opcode — server-side stroke composition will still show old strokes until the server adds a `POST /lienzo/{id}/clear` endpoint (documented as follow-up).

### Notes

- **Deferred to a follow-up pass**: (a) backend `PostIt.asignadoA: Usuario?` relation — required for real "Enviar nota a" persistence (the chip is in-memory only right now); (b) server-side "clear canvas" endpoint so the Borrar button can truly reset both client and server state; (c) `PUT /postits/{id}/color-nota` client wire-up — the backend endpoint is in place but the frontend hasn't started calling it on `noteColor` change, so pastel choices are lost across sessions.
- **Members list wiring**: `ExpandedPostIt` accepts `members: List<Usuario> = emptyList()`; the chip section shows a "Cargando miembros…" placeholder when the list is empty. `PizarraScreen` (the caller) doesn't currently fetch member data — future work should surface `TareasViewModel.miembros` or a shared `RepositoryCasa.getPisoMiembros` result down to this composable.

### Fixed (post-release control-panel bugs)

- **Action-row buttons ellipsized "Bo…", "Di…", "En…" (`PostItActionRow`)**: default `OutlinedButton.contentPadding` is `PaddingValues(horizontal = 24.dp, vertical = 8.dp)`. With three equal-weight buttons on a ~324dp-wide row that leaves ≈5dp per button for icon + label after the 48dp of horizontal padding — labels have nowhere to render. Compressed the buttons: icon `18→16dp`, text `14→12sp`, inner spacer `6→4dp`, height `44→42dp`, row gap `10→8dp`, and — crucially — overrode `contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)`. Renamed middle label `"Dibujar" → "Pintar"` (shorter + closer to the mockup intent). Added `maxLines = 1` on every Text as belt-and-braces protection against overflow.
- **Control panel sat behind the system nav bar**: added `.navigationBarsPadding()` to `PostItControlPanel`'s outer Column so the action row always clears the Android gesture handle / 3-button chrome. Also trimmed panel padding `18/16 → 14/14` and vertical rhythm `10 → 8dp` to reclaim ~12dp for the panel content — everything now fits comfortably above the OS chrome on 5" and 6" devices.
- **"Enviar nota a" stuck on "Cargando miembros…" (`DraggableViewModel` + `PizarraScreen`)**: the placeholder rendered forever because `PizarraScreen` was passing the default `members = emptyList()` down to `ExpandedPostIt` — no code path was fetching the house's Usuario list for the Pizarra context. Fixed by (a) adding `miembros: StateFlow<List<Usuario>>` on `DraggableViewModel`, populated on init via a `loadMiembros()` coroutine that calls the existing `RepositoryCasa.getPisoMiembros(token, casaId)` (same endpoint TareasViewModel uses), and (b) collecting it in `PizarraScreen` and passing to `ExpandedPostIt(members = miembros)`. Chips now render the real house-member names (Daniel, Natalia, Raquel, Marta, …) the moment the Post-It expands.
- **"Borrar" ghost effect — deleted strokes reappeared on the next drawn stroke (`PizarraView.clearCanvas` + `PizarraViewModel.clearLocalBuffer`)**: the previous `clearCanvas` wiped the on-screen bitmap but left three sources of ghost state untouched:
  1. Any debounced `saveJob` waiting to fire — would still POST the pre-clear `puntos` list to the server 1 s later.
  2. `PizarraViewModel.puntos` — the queued delta buffer — still held every stroke drawn before the clear. Even if the debounce fired zero saves before the tap, the very next `ACTION_UP` would flush the accumulated puntos INCLUDING the pre-clear strokes.
  3. `_dirtyVersion` was advanced (via the old `markPending()` at the end of clearCanvas) but `_syncedVersion` wasn't — leaving `pendingSave = true` indefinitely, which locked `setBackgroundBitmap` into skip mode and prevented ANY future poll from applying a fresh bitmap (contributed to a stuck state on peer edits).
  - **Fix**:
    - New `PizarraViewModel.clearLocalBuffer()` — clears `puntos` AND `_syncedVersion.value = _dirtyVersion.value` so `pendingSave` returns to `false`.
    - `PizarraView.clearCanvas` now:
      1. Wipes `currentBitmap` / `canvasBitmap` / `backgroundBitmap` to blank white (unchanged).
      2. `path.reset()` + `lastPoint = null` — in-flight touch path erased so the next `ACTION_MOVE` doesn't append to a pre-clear path (unchanged).
      3. `model.saveJob?.cancel()` — **NEW** — cancels any debounced save so the stale puntos never post.
      4. `model.clearLocalBuffer()` — **NEW** — drops queued puntos + resyncs the pending-save version pair.
      5. `PizarraBitmapCache.remove(model.lienzoId)` — evicts the cache so a reopen doesn't rehydrate from the pre-clear snapshot (unchanged).
      6. Dropped the now-unnecessary trailing `model.markPending()` — it was leaving the ViewModel in a permanently-dirty state.
  - **Server-side ghost remaining**: the delta wire protocol has no "clear" opcode, so the server's composited bitmap still holds pre-clear strokes. If the user closes the Post-It and reopens later, they'll see the historical strokes (cache-miss → server fetch). Fixing this needs a new `POST /lienzo/{id}/clear` backend endpoint — flagged as follow-up. The "immediate reappearance on next stroke" symptom the user reported IS fully gone.

### Fixed (server-side Borrar — truly permanent clear)

- **`PizarraAPI.clearLienzo` — new Retrofit method** hitting the backend `PUT /lienzos/{id}/clear` endpoint. Mirrors the existing `postDelta` / `isUpdated` / `getLienzo` triad on the same base path.
- **`PizarraViewModel.isClearing: StateFlow<Boolean>` + `clearOnServer(localBlank: Bitmap)` method** — coordinated network + cache + state-flow update in a single suspending flow:
  1. Sets `_isClearing = true` immediately so `setBackgroundBitmap`'s guard blocks any in-flight poll from restoring the pre-clear bitmap onto our locally-blank canvas.
  2. Fires the `PUT /lienzos/{id}/clear` call via the shared `RemoteRepository`.
  3. On success: overwrites `PizarraBitmapCache[lienzoId]` with the `localBlank` bitmap the caller already computed (so reopen stays zero-latency and shows blank), advances `lastLoaded = Instant.now()` (silent-refresh baseline stays accurate — server bitmap now matches our blank as of NOW), emits the blank to `_bitmapState` so any observer is in sync with the visible canvas.
  4. On network failure: local canvas stays blank (the wipe already happened on the client), server will re-emit the old bitmap on next poll — user can retry Borrar.
  5. `finally` block always resets `_isClearing = false` so the guard doesn't lock the canvas permanently even on error.
- **`PizarraView.setBackgroundBitmap` — guard extended** — was previously `if (model.pendingSave.value) skip`. Now `if (model.pendingSave.value || model.isClearing.value) skip`. Same behavioral pattern: refresh `backgroundBitmap` reference as the baseline but leave `currentBitmap` alone. Once the clear round-trip completes and `isClearing` returns to false, the next poll can freely apply the fresh (blank) server bitmap.
- **`PizarraView.clearCanvas` now calls `model.clearOnServer(blank)`** at the end — passes the same locally-computed blank bitmap so the ViewModel seeds the cache with the exact pixels the user is looking at. No decode round-trip needed to warm the cache post-clear.

**Net result**: tapping "Borrar" now truly resets the drawing everywhere. Local canvas blanks instantly (unchanged). Cache is evicted synchronously, then re-warmed with the blank bitmap once the server acks. Any poll fired between the tap and the server ack is blocked by `isClearing` from restoring the old bitmap. Closing and reopening the Post-It shows a blank canvas from the cache. If a peer draws a new stroke after the clear, it lands on a white surface — no ghost strokes underneath.

### Fixed (fatal touch-during-recomposition crash)

- **`UninitializedPropertyAccessException: lateinit property canvasBitmap has not been initialized` (`PizarraView.kt:122`)**: crash reproduced when the user touched the canvas during the ~50-200ms window between (a) the AndroidView factory returning a fresh `PizarraView` and (b) the first `setBackgroundBitmap` call landing (either from the `PizarraBitmapCache` hit collector or the network fetch). Reproducible reliably on fast reopen from cache and on pastel background color changes (each pastel selection recomposed `ExpandedPostIt`, briefly leaving `canvasBitmap` unset while the composition re-attached the AndroidView). `ACTION_MOVE`'s `canvasBitmap.drawPath(path, paint)` hit the uninitialized lateinit → exception propagated straight to Android's input-event dispatcher → process death.
  - **Fix**: two-layer defense in `PizarraView`:
    1. **Crash guard in `onTouchEvent`** — checks `!::model.isInitialized || !::canvasBitmap.isInitialized || currentBitmap == null` right after the `activatedDraw` gate. If ANY of those hold, consume the event with `return true` silently. The next touch (a few frames later, after the bitmap has arrived) works normally. Also added a `lastPoint ?: run { moveTo(x, y); … }` fallback in `ACTION_MOVE` — protects against a `MotionEvent.ACTION_MOVE` arriving without a preceding `ACTION_DOWN` (which the previous `lastPoint!!` non-null assertion would NPE on).
    2. **Placeholder initialization in `onSizeChanged`** — the moment the view has real dimensions (`w > 0 && h > 0`), allocate a blank white `Bitmap` + `Canvas` and assign to `currentBitmap` / `canvasBitmap` if `canvasBitmap` isn't already set. This means the "empty window" between layout and network-bitmap arrival is now populated with a valid drawing surface — the crash guard rarely triggers, and when the real bitmap lands, `setBackgroundBitmap` overwrites the placeholder cleanly (its existing `pendingSave` guard protects any mid-stroke work).
- **`onDraw` was already null-safe** via `currentBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }` — no change needed, but added a comment explaining why it can't throw the same exception (`currentBitmap` is nullable, not lateinit).
- **`clearCanvas` was already safe** — its `width.coerceAtLeast(1)` fallback and unconditional `canvasBitmap = Canvas(blank)` assignment mean it can never leave the lateinit unset.

### Fixed (note background color reverted to yellow on reopen)

- **Pastel `colorNota` didn't survive reopen (`ExpandedPostIt` + `PostItState` + `PostItDTO`)**: user picked purple / pink / blue, minimized, reopened → sheet came back yellow. Root cause: `ExpandedPostIt.noteColor` was initialized as `mutableStateOf(NOTE_PASTELS.first())` unconditionally, ignoring the server-persisted `state.colorNota` — and the frontend model didn't even have a `colorNota` field to READ, so the data-model level had already lost the round-trip.
  - **Frontend `PostItDTO.colorNota: String? = null` added** so JSON body-mapping now carries the field on GET / POST responses.
  - **Frontend `PostItState.colorNota: String? = null` added** so the ViewModel's canonical UI-state carries the pastel hex.
  - **`DraggableViewModel.syncPostIts` now assigns `colorNota = dto.colorNota`** on every DTO → PostItState mapping. Every subsequent recomposition of `PizarraScreen` propagates the persisted hex through to `ExpandedPostIt`.
  - **`ExpandedPostIt.noteColor` initialization rewritten**:
    ```kotlin
    var noteColor by remember(state.id, state.colorNota) {
        mutableStateOf(parseNoteColor(state.colorNota) ?: NOTE_PASTELS.first())
    }
    ```
    Includes `state.colorNota` in the `remember` key so a background sync landing a fresh color also updates the local pick — no need to close and reopen to see peer edits.
  - **New `parseNoteColor(hex: String?): Color?` helper** in `Draggable.kt` — inverse of the existing `Color.toHex()`. Handles both `#RRGGBB` (adds `0xFF` alpha) and `#AARRGGBB` variants, `null` fallback on unparseable input. Round-trips exactly the values `Color.toHex` emits, so parsed colors match `NOTE_PASTELS` by ARGB and the `ColorSwatchDot`'s selected check lights up the correct dot.

### Added (colorNota persistence via `PUT /postits/{id}/color-nota`)

- **`PostItAPI.updateColorNota(id, colorHex)` — new Retrofit method** hitting the backend endpoint that's been in place since the earlier PostIt customization pass. Spring `@RequestBody String` accepts a JSON-encoded string body — Retrofit's GsonConverter serializes our `colorHex: String` argument as the JSON string `"#FFF9C4"`, which Spring's Jackson deserializes back to a Kotlin `String`.
- **`RepositoryPostIt.updateColorNota(id, colorHex)` — thin wrapper** that logs errors but returns `null` on any failure (non-fatal by design — the color is already applied locally).
- **`ExpandedPostIt.onNoteColorSelect` now fires the PUT** alongside the local `noteColor = newColor` update. Uses `rememberCoroutineScope()` so the request is scoped to the composable's lifetime — a fast minimize mid-request cancels cleanly, and any color that DID reach the server is picked up on next open. Network failure is non-fatal: user keeps seeing their pick until session end; server value takes over on next restart.

**Net result**: pastel choice fully round-trips. Open a Post-It → tap purple → minimize → reopen → sheet is purple. Close the app → reopen → sheet is still purple. Have a peer change the color from their device → wait for the 60s DraggableViewModel sync tick → local sheet re-tints to their pick without needing a manual close/reopen.

### Fixed (immediate close/reopen still reverted the pastel to yellow)

- **Optimistic local propagation was missing** — the initial fix persisted the pick to the backend, but the local `_postIts` StateFlow (the source of truth for `state.colorNota`) was only refreshed on the 60-second `syncPostIts` tick. If the user picked purple, minimized, and reopened within that window, `state.colorNota` was still `null` and `ExpandedPostIt`'s `remember(state.id, state.colorNota)` initializer fell through to `NOTE_PASTELS.first()` = yellow.
  - **New `DraggableViewModel.updatePostItColorLocal(postItId, newColorHex)`** — surgically `.copy(colorNota = newColorHex)` the matching PostItState in the `_postIts` StateFlow. Called from `ExpandedPostIt` the instant the user picks, before the network PUT even fires.
  - **`ExpandedPostIt.onColorNotaChanged: (String) -> Unit` param added** and wired from `PizarraScreen` to `viewModel.updatePostItColorLocal(expandedPostIt.id, hex)`.
  - **`onNoteColorSelect` handler restructured** into a clear three-step propagation (in order of increasing durability):
    1. Local `noteColor = newColor` — sheet re-tints in the same frame.
    2. `onColorNotaChanged(hex)` — parent VM's `_postIts` gets the new value so close/reopen sees the truth.
    3. `postItRepository.updateColorNota(state.id, hex)` — backend PUT for durable persistence across restart / other devices.
- **Sync-back semantics preserved**: the 60s `syncPostIts` tick still overrides the local optimistic value with whatever the server returns. If our PUT succeeded, the server value matches and nothing visible changes. If the PUT failed, the sync tick would eventually revert the sheet to the previous color — the correct behavior, since our local pick was never durable.

## Follow-ups still open

- **`PostIt.asignadoA: Usuario?` backend relation** — "Enviar nota a" chip is still in-memory-only.
- **Historical stroke color migration** — existing `Lienzo.bytes` re-decode with the new palette's ARGB values (acceptable pre-1.0).

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
