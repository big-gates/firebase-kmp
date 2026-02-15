# firebase-kmp

Open-source Kotlin Multiplatform (Android/iOS/JVM) wrapper library for Firebase APIs.

한국어 문서: [`README.ko.md`](README.ko.md)

## Modules
- `firebase-common`: Firebase app initialization and app instance APIs
- `firebase-message`: FCM topic/token APIs + Pub/Sub subscriber APIs (JVM-only)
- `firebase-storage`: Cloud Storage reference APIs

## Targets
- Android
- iOS (`iosX64`, `iosArm64`, `iosSimulatorArm64`)
- JVM

## API Compatibility Coverage

Goal: Firebase API compatibility coverage and public API coverage to 100% for KMP (Android/iOS/JVM).

Coverage model:
- Product-family coverage = implemented product families / target product families
- Public API coverage = implemented public APIs / target public APIs (per product, per platform)
- API-depth status per implemented module = `not started`, `partial`, `near parity`, `parity`

Target product families (18):
- Core
- Cloud Messaging
- Cloud Storage
- Authentication
- Cloud Firestore
- Realtime Database
- Cloud Functions
- Analytics
- Crashlytics
- Performance Monitoring
- Remote Config
- App Check
- In-App Messaging
- Dynamic Links
- Installations
- ML
- Data Connect
- Vertex AI in Firebase

Current status (updated: 2026-02-15):
- Product-family coverage: `3 / 18 = 16.7%`
- Public API catalog: `docs/public-api/README.md`
- Public API coverage (module-level):
- `firebase-common`: `21 / 25 = 84.0%` (`docs/public-api/firebase-common.md`)
- `firebase-message`: `19 / 36 = 52.8%` (`docs/public-api/firebase-message.md`)
- `firebase-storage`: `13 / 31 = 41.9%` (`docs/public-api/firebase-storage.md`)
- Implemented:
- `firebase-common` (Core): `partial`
- `firebase-message` (Cloud Messaging + JVM Pub/Sub consume path): `partial`
- `firebase-storage` (Cloud Storage): `partial`

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
import com.biggates.firebase.message.sendMessage
import com.biggates.firebase.message.FirebaseJvmFcmMessage

suspend fun messagingJvmSample() {
    Firebase.messaging.autoInitEnabled = true
    Firebase.setMessagingRegistrationToken("fcm_registration_token")

    Firebase.messaging.subscribeToTopic("news")
    val token = Firebase.messaging.getToken()
    Firebase.messaging.unsubscribeFromTopic("news")
    Firebase.messaging.deleteToken()

    Firebase.sendMessage(
        FirebaseJvmFcmMessage(
            topic = "news",
            data = mapOf("source" to "jvm", "message" to "hello"),
        )
    )
}
```

### 3) Subscribe and process data on JVM (`firebase-message`, Pub/Sub)

```kotlin
import com.biggates.firebase.common.Firebase
import com.biggates.firebase.message.FirebaseJvmAckDecision
import com.biggates.firebase.message.FirebaseJvmPubSubPublishConfig
import com.biggates.firebase.message.FirebaseJvmPubSubSubscriberConfig
import com.biggates.firebase.message.publishMessage
import com.biggates.firebase.message.subscribeMessages

fun startJvmPubSub() {
    val subscription = Firebase.subscribeMessages(
        subscriptionId = "news-subscription",
        config = FirebaseJvmPubSubSubscriberConfig(
            // Optional. For emulator: "localhost:8085"
            endpoint = null
        )
    ) { message ->
        println("messageId=${message.messageId}")
        println("data=${message.dataUtf8}")
        println("attributes=${message.attributes}")

        FirebaseJvmAckDecision.ACK
    }

    Firebase.publishMessage(
        topicId = "news-topic",
        dataUtf8 = """{"type":"news","title":"hello from jvm"}""",
        attributes = mapOf("channel" to "backend"),
        config = FirebaseJvmPubSubPublishConfig(endpoint = null),
    )

    // subscription.stop()
}
```

Notes:
- `projectId` must be set in `FirebaseOptions` during `Firebase.initializeApp(...)`.
- The returned `FirebaseJvmPubSubSubscription` should be stopped gracefully on shutdown:
  `subscription.stop()`.

### 4) Storage on JVM (`firebase-storage`)

```kotlin
import com.biggates.firebase.common.Firebase
import com.biggates.firebase.storage.storage

suspend fun storageJvmSample() {
    val storage = Firebase.storage

    val ref = storage.reference("images/profile.png")
    ref.putBytes("hello-storage".encodeToByteArray())
    val bytes = ref.getBytes(maxDownloadSizeBytes = 1024 * 1024)
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
