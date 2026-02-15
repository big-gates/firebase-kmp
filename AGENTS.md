# AGENTS.md

## Scope
These instructions apply to the whole repository. Module-specific rules in `docs/agents/*.md` take precedence for files inside their module.

## Project Mission
`firebase-kmp` is an open-source Kotlin Multiplatform wrapper around official Firebase SDKs, so Firebase features can be used from shared KMP APIs.

## Active Modules
- `firebase-common`: core Firebase app/bootstrap abstractions.
- `firebase-message`: Firebase Cloud Messaging abstractions.

Reference module guides:
- `docs/agents/firebase-common.md`
- `docs/agents/firebase-message.md`

## Core Engineering Rules
1. Keep wrappers thin and predictable.
2. Define shared API in `commonMain` first (`expect`, data models, extension properties/functions).
3. Implement platform behavior in `androidMain`, `iosMain`, and `jvmMain`.
4. Maintain Android/iOS/JVM behavior parity as much as possible. If parity is impossible, document the difference in KDoc and module docs.
5. Do not add app-level/business logic to this library.

## API Design Rules
1. Public API changes must start from `commonMain` and then be implemented in all active targets.
2. Prefer suspend APIs for async platform calls.
3. Avoid silent failures. If behavior differs by platform, make it explicit.
4. Keep naming and semantics Firebase-like unless there is a strong multiplatform reason to diverge.
5. Preserve backward compatibility for published artifacts when possible.

## Platform Interop Rules
1. Android code should use Firebase Android SDK with the module's BOM-based dependency setup.
2. iOS code should use CocoaPods interop APIs declared in each module.
3. JVM code should be explicit about capability limits where Firebase client SDK parity does not exist.
4. Avoid introducing new force unwrap patterns on iOS; handle nullable results defensively.
5. When adding option/config fields, update all relevant platform mappings in the same change.

## Dependency And Build Rules
1. Keep versions centralized in `gradle/libs.versions.toml`.
2. Keep Android BOM and iOS CocoaPods versions aligned to a compatible Firebase generation.
3. Maintain Kotlin Multiplatform target setup already present in each module (`android`, `jvm`, `iosX64`, `iosArm64`, `iosSimulatorArm64`).
4. Keep Java/Kotlin target compatibility at JVM 17 unless the project intentionally upgrades.

## Testing And Verification
Run checks for touched modules before submitting:
- `./gradlew :firebase-common:check`
- `./gradlew :firebase-message:check`
- `./gradlew :firebase-common:compileKotlinJvm`
- `./gradlew :firebase-message:compileKotlinJvm`

When API surface or platform mapping changes:
1. Add or update tests in relevant source sets.
2. Validate compile targets at minimum (Android + iOS + JVM source sets).

## Publishing Rules
1. Artifacts are published via `com.vanniktech.maven.publish`.
2. Keep `groupId`, `artifactId`, and POM metadata consistent with module identity.
3. Bump versions intentionally when behavior or API changes.
4. Use local publish for validation when needed:
- `./gradlew :firebase-common:publishToMavenLocal`
- `./gradlew :firebase-message:publishToMavenLocal`

## Documentation Maintenance
1. Update `README.md` and affected files in `docs/agents/` whenever public APIs or setup flow changes.
2. Add brief KDoc to new public APIs and document platform caveats.
