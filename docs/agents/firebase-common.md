# firebase-common agent guide

## Module Purpose
`firebase-common` provides multiplatform wrappers for Firebase app lifecycle and configuration (`Firebase`, `FirebaseApp`, `FirebaseOptions`).

## Key Files
- `firebase-common/src/commonMain/kotlin/com/biggates/firebase/common/Firebase.kt`
- `firebase-common/src/androidMain/kotlin/com/biggates/firebase/common/Firebase.android.kt`
- `firebase-common/src/iosMain/kotlin/com/biggates/firebase/common/Firebase.ios.kt`
- `firebase-common/src/jvmMain/kotlin/com/biggates/firebase/common/Firebase.jvm.kt`
- `firebase-common/build.gradle.kts`

## Ownership Boundary
This module should only contain Firebase Core/bootstrap abstractions:
1. App instance access.
2. App initialization.
3. App listing/deletion.
4. Shared options model and platform mapping.

Do not add service-specific APIs here (Messaging/Auth/Firestore/etc.).

## Change Workflow
1. Edit shared API first in `commonMain`.
2. Implement matching `actual` behavior for Android, iOS, and JVM in the same PR.
3. If you add fields to `FirebaseOptions`, update:
- Android `toAndroid()` mapping.
- iOS `toIos()` mapping.
- JVM app-state handling if needed.
- Any KDoc describing option semantics.

## Android Rules
1. `PlatformContext` is `android.app.Application`; keep it that way unless architecture changes repo-wide.
2. Use Firebase Android SDK types only inside `androidMain`.
3. Keep async bridge logic coroutine-friendly.
4. Preserve current semantics of `delete()` (boolean success signal).

## iOS Rules
1. `PlatformContext` is a placeholder type; do not assume app-level context exists.
2. Use CocoaPods `FirebaseCore` interop only inside `iosMain`.
3. Avoid adding new force unwraps (`!!`) for optional CocoaPods values; prefer defensive handling.
4. When iOS lacks a direct equivalent to Android behavior, document fallback behavior in KDoc.

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
