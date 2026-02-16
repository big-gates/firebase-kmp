# AGENTS.md

## Scope
These instructions apply to the whole repository. Module-specific rules in `docs/agents/*.md` take precedence for files inside their module.

## Project Mission
`firebase-kmp` is an open-source Kotlin Multiplatform wrapper around official Firebase SDKs, so Firebase features can be used from shared KMP APIs.

## Long-Term Goal
1. Final goal: 100% Firebase API compatibility and 100% public API coverage for KMP (Android/Apple/JVM), with explicit platform caveats only when official SDK parity is impossible.
2. Work should be planned and implemented toward full Firebase product-family coverage, not only currently available modules.
3. Every public API expansion should move compatibility coverage forward in measurable terms.

## Coverage Tracking Policy
1. `README.md`, `README.en.md`, and `README.ko.md` must include current compatibility coverage status.
2. Coverage is tracked at three levels:
- Product-family coverage: implemented Firebase product modules / target product families.
- Public API coverage: implemented public APIs / target public APIs (per product and per platform).
- API-depth status per implemented module: `not started`, `partial`, `near parity`, or `parity`.
3. Public API inventories must be maintained in:
- `docs/public-api/firebase-common.md`
- `docs/public-api/firebase-message.md`
- `docs/public-api/firebase-storage.md`
4. Any PR that changes public APIs or adds modules must update both:
- README coverage sections.
- The relevant files in `docs/public-api/`.

## Active Modules
- `firebase-common`: core Firebase app/bootstrap abstractions.
- `firebase-message`: Firebase Cloud Messaging abstractions (Android/Apple only; JVM currently unsupported).
- `firebase-storage`: Firebase Storage abstractions.

Reference module guides:
- `docs/agents/firebase-common.md`
- `docs/agents/firebase-message.md`
- `docs/agents/firebase-storage.md`

## Core Engineering Rules
1. Keep wrappers thin and predictable.
2. Define shared API in `commonMain` first (`expect`, data models, extension properties/functions).
3. Implement platform behavior in every active target for that module (`androidMain`, Apple source sets, and `jvmMain` where supported).
4. Maintain target parity as much as possible. If parity is impossible, document the difference in KDoc and module docs.
5. Do not add app-level/business logic to this library.

## API Design Rules
1. Public API changes must start from `commonMain` and then be implemented in all active targets.
2. Prefer suspend APIs for async platform calls.
3. Avoid silent failures. If behavior differs by platform, make it explicit.
4. Keep naming and semantics Firebase-like unless there is a strong multiplatform reason to diverge.
5. Preserve backward compatibility for published artifacts when possible.

## Platform Interop Rules
1. Android code should use Firebase Android SDK with the module's BOM-based dependency setup.
2. Apple-native code should use CocoaPods interop APIs declared in each module.
3. For modules that support JVM, use explicit runtime configuration when backed by Admin/Cloud SDKs, and document behavior differences from mobile client SDKs.
4. Avoid introducing new force unwrap patterns in Apple source sets; handle nullable results defensively.
5. When adding option/config fields, update all relevant platform mappings in the same change.

## Dependency And Build Rules
1. Keep versions centralized in `gradle/libs.versions.toml`.
2. Keep Android BOM and Apple CocoaPods versions aligned to a compatible Firebase generation.
3. Maintain Kotlin Multiplatform target setup already present in each module; follow documented module exceptions (for example, `firebase-message` has no JVM target).
4. Keep Java/Kotlin target compatibility at JVM 17 unless the project intentionally upgrades.

## Testing And Verification
Run checks for touched modules before submitting:
- `./gradlew :firebase-common:check`
- `./gradlew :firebase-message:check`
- `./gradlew :firebase-storage:check`
- `./gradlew :firebase-common:compileKotlinJvm`
- `./gradlew :firebase-storage:compileKotlinJvm`

When API surface or platform mapping changes:
1. Add or update tests in relevant source sets.
2. Validate compile targets at minimum for the module's active targets (Android/Apple, plus JVM where supported).

## Publishing Rules
1. Artifacts are published via `com.vanniktech.maven.publish`.
2. Keep `groupId`, `artifactId`, and POM metadata consistent with module identity.
3. Bump versions intentionally when behavior or API changes.
4. Use local publish for validation when needed:
- `./gradlew :firebase-common:publishToMavenLocal`
- `./gradlew :firebase-message:publishToMavenLocal`
- `./gradlew :firebase-storage:publishToMavenLocal`

## Documentation Maintenance
1. Update `README.md` and affected files in `docs/agents/` whenever public APIs or setup flow changes.
2. Update corresponding files in `docs/public-api/` whenever public API coverage changes.
3. Add brief KDoc to new public APIs and document platform caveats.
