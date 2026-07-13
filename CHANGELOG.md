# Changelog

All notable changes to the MiRumi Android Client will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
