# Contributing to MiRumi Android

## Commit Convention

This project uses [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

### Format

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types

| Type       | Description                                           |
|------------|-------------------------------------------------------|
| `feat`     | A new feature or capability                           |
| `fix`      | A bug fix                                             |
| `refactor` | Code restructuring without behavior change            |
| `perf`     | Performance improvement                               |
| `security` | Security hardening or vulnerability fix               |
| `test`     | Adding or updating tests                              |
| `docs`     | Documentation changes                                 |
| `build`    | Build system or dependency changes                    |
| `ci`       | CI/CD pipeline changes                                |
| `chore`    | Maintenance tasks that don't fit other categories     |

### Scopes

| Scope        | Covers                                              |
|--------------|-----------------------------------------------------|
| `auth`       | Authentication, session, interceptor, token storage |
| `security`   | Encrypted prefs, biometric, access control          |
| `network`    | Retrofit, OkHttp, NetworkModule, API interfaces     |
| `ui`         | Composables, screens, navigation, theming           |
| `model`      | Data classes, DTOs, responses                       |
| `repo`       | Repository layer, data access                       |
| `firebase`   | FCM, push notifications                             |
| `nav`        | Navigation graph, routes, deep links                |
| `deps`       | Dependency updates                                  |

### Examples

```
feat(auth): add OkHttp interceptor for automatic token injection
fix(network): save JWT before FCM token registration call
security(auth): migrate SessionManager to EncryptedSharedPreferences
refactor(repo): centralize NetworkModule initialization
test(auth): update SessionManager tests for encrypted prefs
build(deps): add androidx.security:security-crypto dependency
```

### Rules

1. Use imperative mood in the description ("add feature" not "added feature").
2. Do not capitalize the first letter of the description.
3. No period at the end of the description.
4. Limit the subject line to 72 characters.
5. Use the body to explain *what* and *why*, not *how*.
6. Reference issue numbers in the footer: `Closes #42`.

## Code Style

- **Formatter**: Ktlint runs automatically on every build via the Gradle plugin (`org.jlleitschuh.gradle.ktlint`). The `ktlintFormat` task executes as a `preBuild` dependency.
- **Static Analysis**: Detekt runs during the `check` phase; fix all reported issues before pushing.
- **EditorConfig**: `.editorconfig` files at project root and `app/` enforce indentation, charset, and Ktlint rule overrides.

Run formatting manually:

```bash
./gradlew ktlintFormat
```

Run static analysis:

```bash
./gradlew detekt
```

## Branch Strategy

- `main` — stable, release-ready code.
- `develop` — integration branch for features.
- Feature branches: `feat/<scope>-<short-description>` (e.g., `feat/ui-dark-theme`).
- Fix branches: `fix/<scope>-<short-description>` (e.g., `fix/auth-token-refresh`).
