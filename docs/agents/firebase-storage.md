# firebase-storage agent guide

## Module Purpose
`firebase-storage` provides multiplatform wrappers for Firebase Cloud Storage operations (`Firebase.storage`, storage references, download URL, delete).

## Coverage Target
1. Long-term target is near parity with official Storage APIs per platform.
2. Coverage should expand beyond current reference/download/delete subset in incremental, documented steps.
3. Public API coverage target for this module is 100% within Storage scope, with explicit caveats only when official parity is impossible.
4. Update README coverage status whenever Storage public API surface grows.
5. Update `docs/public-api/firebase-storage.md` in the same change.

## Key Files
- `firebase-storage/src/commonMain/kotlin/com/biggates/firebase/storage/Storage.kt`
- `firebase-storage/src/androidMain/kotlin/com/biggates/firebase/storage/Storage.android.kt`
- `firebase-storage/src/appleMain/kotlin/com/biggates/firebase/storage/Storage.apple.kt`
- `firebase-storage/src/jvmMain/kotlin/com/biggates/firebase/storage/Storage.jvm.kt`
- `firebase-storage/build.gradle.kts`
- `docs/public-api/firebase-storage.md`

## Dependency Boundary
1. This module depends on `firebase-common` for shared Firebase entrypoints.
2. Keep dependencies storage-focused and minimal.
3. Do not add unrelated Firebase products here.

## Change Workflow
1. Start in `commonMain` with shared API changes.
2. Implement Android, Apple, and JVM `actual` code in the same change.
3. Keep API parity for:
- `Firebase.storage`
- `Firebase.storage(url)`
- `StorageReference.child`
- `StorageReference.putBytes`
- `StorageReference.getBytes`
- `StorageReference.getDownloadUrl`
- `StorageReference.delete`

## Android Rules
1. Use `com.google.firebase.storage.FirebaseStorage`/`StorageReference` only in `androidMain`.
2. Bridge Task APIs with coroutines (`await`) for suspend functions.
3. Preserve predictable mapping between shared `StorageReference` and Android SDK references.

## Apple Rules
1. Use CocoaPods `FirebaseStorage` interop in Apple source sets.
2. Bridge callback APIs with cancellable coroutines.
3. Propagate native errors as exceptions; avoid silent fallback for failure states.

## JVM Rules
1. JVM target is backed by Google Cloud Storage client via Firebase Admin dependency and requires explicit runtime configuration.
2. Configure Admin SDK through `firebase-common` initialization (`Firebase.initializeApp(...)`) and include `storageBucket` in `FirebaseOptions`.
3. Keep URL/path parsing and download URL behavior explicit, because JVM storage semantics differ from mobile client SDK internals.

## API And Compatibility Rules
1. Preserve `Firebase.storage` entry point and storage reference semantics.
2. Keep public API naming close to Firebase Storage terminology.
3. Document any platform behavior differences in KDoc and module docs.

## Validation Commands
- `./gradlew :firebase-storage:check`
- `./gradlew :firebase-storage:assemble`
- `./gradlew :firebase-storage:compileKotlinJvm`
- `./gradlew :firebase-storage:publishToMavenLocal` (optional pre-release validation)
