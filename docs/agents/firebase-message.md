# firebase-message agent guide

## Module Purpose
`firebase-message` provides multiplatform wrappers for Firebase Cloud Messaging operations (`Firebase.messaging`, topic subscription, token lifecycle).

## Coverage Target
1. Long-term target is near parity with official Messaging APIs per platform.
2. Coverage should expand beyond current topic/token subset (and JVM Pub/Sub consume path) in incremental, documented steps.
3. Public API coverage target for this module is 100% within Messaging scope, with explicit caveats only when official parity is impossible.
4. Update README coverage status whenever Messaging public API surface grows.
5. Update `docs/public-api/firebase-message.md` in the same change.

## Key Files
- `firebase-message/src/commonMain/kotlin/com/biggates/firebase/message/Messaging.kt`
- `firebase-message/src/androidMain/kotlin/com/biggates/firebase/message/Messaging.android.kt`
- `firebase-message/src/iosMain/kotlin/com/biggates/firebase/message/Messaging.ios.kt`
- `firebase-message/src/jvmMain/kotlin/com/biggates/firebase/message/Messaging.jvm.kt`
- `firebase-message/build.gradle.kts`
- `docs/public-api/firebase-message.md`

## Dependency Boundary
1. This module depends on `firebase-common` for shared Firebase app access.
2. Keep dependencies minimal and messaging-focused.
3. Do not add unrelated Firebase products here.

## Change Workflow
1. Start in `commonMain` with shared API changes.
2. Implement Android, iOS, and JVM `actual` code in the same change.
3. Keep function parity for:
- `autoInitEnabled`
- `subscribeToTopic`
- `unsubscribeFromTopic`
- `getToken`
- `deleteToken`

## Android Rules
1. Use `com.google.firebase.messaging.FirebaseMessaging` only in `androidMain`.
2. Bridge Task APIs with coroutines (`await`) for suspend functions.
3. Keep topic APIs lightweight and non-blocking.

## iOS Rules
1. Use CocoaPods `FirebaseMessaging` interop only in `iosMain`.
2. Bridge callback APIs with cancellable coroutines.
3. Propagate errors with clear exceptions rather than swallowing them.
4. Keep token nullability behavior explicit and documented.

## JVM Rules
1. JVM target is backed by Firebase Admin SDK and requires explicit runtime configuration.
2. Configure Admin SDK through `firebase-common` initialization (`Firebase.initializeApp(...)`) before using Messaging APIs.
3. Provide registration token via `Firebase.setMessagingRegistrationToken(...)` because JVM cannot mint client FCM tokens.
4. JVM data consume path should use Pub/Sub subscriber APIs (`Firebase.subscribeMessages(...)`) rather than pretending mobile push callbacks exist.
5. Keep JVM behavior explicit about server-side limitations versus mobile client SDK semantics.

## Manifest And Tests
1. Keep `firebase-message/src/androidMain/AndroidManifest.xml` minimal unless messaging integration requires declarations.
2. Replace template tests with behavior-focused tests as API surface grows.
3. If platform-specific behavior is added, add platform-specific tests.

## API And Compatibility Rules
1. Preserve `Firebase.messaging` entry point for discoverability.
2. Keep public API naming consistent with Firebase Messaging terminology.
3. Any breaking change must be intentional and documented in release notes/docs.

## Validation Commands
- `./gradlew :firebase-message:check`
- `./gradlew :firebase-message:assemble`
- `./gradlew :firebase-message:compileKotlinJvm`
- `./gradlew :firebase-message:publishToMavenLocal` (optional pre-release validation)
