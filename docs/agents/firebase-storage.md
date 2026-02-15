# firebase-storage agent guide

## Module Purpose
`firebase-storage` provides multiplatform wrappers for Firebase Cloud Storage operations (`Firebase.storage`, storage references, download URL, delete).

## Key Files
- `firebase-storage/src/commonMain/kotlin/com/biggates/firebase/storage/Storage.kt`
- `firebase-storage/src/androidMain/kotlin/com/biggates/firebase/storage/Storage.android.kt`
- `firebase-storage/src/iosMain/kotlin/com/biggates/firebase/storage/Storage.ios.kt`
- `firebase-storage/src/jvmMain/kotlin/com/biggates/firebase/storage/Storage.jvm.kt`
- `firebase-storage/build.gradle.kts`

## Dependency Boundary
1. This module depends on `firebase-common` for shared Firebase entrypoints.
2. Keep dependencies storage-focused and minimal.
3. Do not add unrelated Firebase products here.

## Change Workflow
1. Start in `commonMain` with shared API changes.
2. Implement Android, iOS, and JVM `actual` code in the same change.
3. Keep API parity for:
- `Firebase.storage`
- `Firebase.storage(url)`
- `StorageReference.child`
- `StorageReference.getDownloadUrl`
- `StorageReference.delete`

## Android Rules
1. Use `com.google.firebase.storage.FirebaseStorage`/`StorageReference` only in `androidMain`.
2. Bridge Task APIs with coroutines (`await`) for suspend functions.
3. Preserve predictable mapping between shared `StorageReference` and Android SDK references.

## iOS Rules
1. Use CocoaPods `FirebaseStorage` interop only in `iosMain`.
2. Bridge callback APIs with cancellable coroutines.
3. Propagate native errors as exceptions; avoid silent fallback for failure states.

## JVM Rules
1. JVM target currently has no official Firebase Storage client SDK parity for this wrapper.
2. Unsupported operations must fail clearly (`UnsupportedOperationException`) instead of no-op.
3. If JVM support scope expands, update this guide and shared API docs together.

## API And Compatibility Rules
1. Preserve `Firebase.storage` entry point and storage reference semantics.
2. Keep public API naming close to Firebase Storage terminology.
3. Document any platform behavior differences in KDoc and module docs.

## Validation Commands
- `./gradlew :firebase-storage:check`
- `./gradlew :firebase-storage:assemble`
- `./gradlew :firebase-storage:compileKotlinJvm`
- `./gradlew :firebase-storage:publishToMavenLocal` (optional pre-release validation)
