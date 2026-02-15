# firebase-kmp

Open-source Kotlin Multiplatform (Android/iOS/JVM) wrapper library for Firebase APIs.

## Modules
- `firebase-common`: Firebase app initialization and app instance APIs
- `firebase-message`: FCM topic/token APIs
- `firebase-storage`: Cloud Storage reference APIs

## Targets
- Android
- iOS (`iosX64`, `iosArm64`, `iosSimulatorArm64`)
- JVM

## Installation

Maven coordinates (example version: `0.0.1`)

```kotlin
dependencies {
    implementation("io.github.big-gates:firebase-common:0.0.1")
    implementation("io.github.big-gates:firebase-message:0.0.1")
    implementation("io.github.big-gates:firebase-storage:0.0.1")
}
```

## Usage

### 1) Initialize Firebase (`firebase-common`)

On JVM, Admin SDK is initialized as part of `Firebase.initializeApp(context, options)`.

```kotlin
import com.biggates.firebase.common.Firebase
import com.biggates.firebase.common.FirebaseOptions
import com.biggates.firebase.common.PlatformContext

fun initFirebaseOnJvm() {
    Firebase.initializeApp(
        context = PlatformContext(),
        options = FirebaseOptions(
            applicationId = "your-app-id",
            apiKey = "your-api-key",
            projectId = "your-project-id",
            storageBucket = "your-project-id.appspot.com",
            serviceAccountPath = "/absolute/path/service-account.json" // JVM-only
        )
    )
}
```

Notes:
- Calling only `initializeApp(context)` on JVM fails. Use options-based initialization.
- If `serviceAccountPath` is omitted, Application Default Credentials (ADC) are used.
- `storageBucket` is required when using default `Firebase.storage`.

### 2) Messaging on JVM (`firebase-message`)

JVM cannot mint FCM registration tokens like mobile client SDKs, so set the token explicitly.

```kotlin
import com.biggates.firebase.common.Firebase
import com.biggates.firebase.message.messaging
import com.biggates.firebase.message.setMessagingRegistrationToken

suspend fun messagingJvmSample() {
    Firebase.setMessagingRegistrationToken("fcm_registration_token")

    Firebase.messaging.subscribeToTopic("news")
    val token = Firebase.messaging.getToken()
    Firebase.messaging.unsubscribeFromTopic("news")
    Firebase.messaging.deleteToken()
}
```

### 3) Storage on JVM (`firebase-storage`)

```kotlin
import com.biggates.firebase.common.Firebase
import com.biggates.firebase.storage.storage

suspend fun storageJvmSample() {
    val storage = Firebase.storage

    val ref = storage.reference("images/profile.png")
    val downloadUrl = ref.getDownloadUrl()
    ref.delete()

    val gsRef = storage.referenceFromUrl("gs://your-project-id.appspot.com/images/profile.png")
    val childRef = gsRef.child("thumb.png")

    // For Storage Emulator
    storage.useEmulator("127.0.0.1", 9199)
}
```

## Verify Build

```bash
./gradlew :firebase-common:check :firebase-message:check :firebase-storage:check
./gradlew :firebase-common:compileKotlinJvm :firebase-message:compileKotlinJvm :firebase-storage:compileKotlinJvm
```
