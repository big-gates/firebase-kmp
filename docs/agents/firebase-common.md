# firebase-common agent guide

## Module Purpose
`firebase-common` provides multiplatform wrappers for Firebase app lifecycle and configuration (`Firebase`, `FirebaseApp`, `FirebaseOptions`).

## Coverage Target
1. This module is considered complete only when Firebase Core app/bootstrap API surface is near parity across Android/Apple/JVM.
2. JVM Admin initialization and option mapping must remain compatible with growth in downstream modules (`message`, `storage`, future products).
3. Public API coverage target for this module is 100% within Firebase Core scope, with explicit caveats only when official parity is impossible.
4. Update README coverage status whenever `firebase-common` public API expands.
5. Update `docs/public-api/firebase-common.md` in the same change.

## Key Files
- `firebase-common/src/commonMain/kotlin/com/biggates/firebase/common/Firebase.kt`
- `firebase-common/src/androidMain/kotlin/com/biggates/firebase/common/Firebase.android.kt`
- `firebase-common/src/appleMain/kotlin/com/biggates/firebase/common/Firebase.apple.kt`
- `firebase-common/src/jvmMain/kotlin/com/biggates/firebase/common/Firebase.jvm.kt`
- `firebase-common/build.gradle.kts`
- `docs/public-api/firebase-common.md`

## Ownership Boundary
This module should only contain Firebase Core/bootstrap abstractions:
1. App instance access.
2. App initialization.
3. App listing/deletion.
4. Shared options model and platform mapping.

Do not add service-specific APIs here (Messaging/Auth/Firestore/etc.).

## Change Workflow
1. Edit shared API first in `commonMain`.
2. Implement matching `actual` behavior for Android, Apple, and JVM in the same PR.
3. If you add fields to `FirebaseOptions`, update:
- Android `toAndroid()` mapping.
- Apple `toIos()` mapping.
- JVM app-state handling if needed.
- Any KDoc describing option semantics.

## Android Rules
1. `PlatformContext` is `android.app.Application`; keep it that way unless architecture changes repo-wide.
2. Use Firebase Android SDK types only inside `androidMain`.
3. Keep async bridge logic coroutine-friendly.
4. Preserve current semantics of `delete()` (boolean success signal).

## Apple Rules
1. `PlatformContext` is a placeholder type; do not assume app-level context exists.
2. Use CocoaPods `FirebaseCore` interop in Apple source sets.
3. Avoid adding new force unwraps (`!!`) for optional CocoaPods values; prefer defensive handling.
4. When Apple platforms lack a direct equivalent to Android behavior, document fallback behavior in KDoc.

## JVM Rules
1. Keep JVM implementation deterministic and explicit; do not imply unsupported Firebase client behavior works on JVM.
2. `initializeApp(context)` should not auto-discover config on JVM; require explicit options-based initialization.
3. Maintain app registry behavior (`initializeApp`, `app`, `getApps`, `delete`) for testability and API consistency.
4. Initialize Firebase Admin SDK in JVM path during `initializeApp(context, options)`.
5. Keep JVM-only options (`serviceAccountPath`) documented and backward compatible.

## API And Compatibility Rules
1. Keep public names stable (`Firebase`, `FirebaseApp`, `FirebaseOptions`, `initializeApp`, `getApps`).
2. If behavior changes, note migration impact in PR and docs.
3. Favor explicit, deterministic behavior over implicit side effects.

## Validation Commands
- `./gradlew :firebase-common:check`
- `./gradlew :firebase-common:assemble`
- `./gradlew :firebase-common:compileKotlinJvm`
- `./gradlew :firebase-common:publishToMavenLocal` (optional pre-release validation)
