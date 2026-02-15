# firebase-kmp

Open-source Kotlin Multiplatform (Android/Apple/JVM) wrapper library for Firebase APIs.

Language:
- English (this file)
- Korean: [`README.ko.md`](README.ko.md)

## Why firebase-kmp?
- Use Firebase from shared KMP code with Firebase-like APIs.
- Keep Android, Apple, and JVM behavior aligned as much as possible.
- Support JVM server-side workflows with Firebase Admin SDK.

## Module Matrix

| Module | Firebase Product | Current Scope | Public API Coverage | Status |
| --- | --- | --- | --- | --- |
| `firebase-common` | Core | App init, app instance, options, app registry | `21 / 25 = 84.0%` | `partial` |
| `firebase-message` | Cloud Messaging | Topic/token APIs + JVM Pub/Sub consume/publish + JVM send APIs | `19 / 36 = 52.8%` | `partial` |
| `firebase-storage` | Cloud Storage | Storage reference APIs + bytes/download/delete (see notes) | `13 / 31 = 41.9%` | `partial` |

## Targets

| Target | Support |
| --- | --- |
| Android | Yes |
| Apple (`ios*`, `macos*`, `tvos*`, `watchos*`) | Yes |
| JVM | Yes |

Note:
- visionOS target is planned once Firebase SDK parity and KMP interop stability are verified.

## Compatibility Coverage (Updated: 2026-02-15)

Goal:
- `100%` Firebase API compatibility coverage for KMP.
- `100%` public API coverage for each supported Firebase product module.

Coverage model:
- Product-family coverage = implemented Firebase product families / target product families
- Public API coverage = implemented public APIs / target public APIs (per product, per platform)
- API-depth status = `not started`, `partial`, `near parity`, `parity`

Current snapshot:
- Product-family coverage: `3 / 18 = 16.7%`
- Public API catalog: [`docs/public-api/README.md`](docs/public-api/README.md)

| Module | Coverage | Catalog |
| --- | --- | --- |
| `firebase-common` | `21 / 25 = 84.0%` | [`docs/public-api/firebase-common.md`](docs/public-api/firebase-common.md) |
| `firebase-message` | `19 / 36 = 52.8%` | [`docs/public-api/firebase-message.md`](docs/public-api/firebase-message.md) |
| `firebase-storage` | `13 / 31 = 41.9%` | [`docs/public-api/firebase-storage.md`](docs/public-api/firebase-storage.md) |

Target product families (18):
- Core, Cloud Messaging, Cloud Storage, Authentication, Cloud Firestore, Realtime Database, Cloud Functions, Analytics, Crashlytics, Performance Monitoring, Remote Config, App Check, In-App Messaging, Dynamic Links, Installations, ML, Data Connect, Vertex AI in Firebase

## Installation

Maven coordinates (example version: `0.0.1`):

```kotlin
dependencies {
    implementation("io.github.big-gates:firebase-common:0.0.1")
    implementation("io.github.big-gates:firebase-message:0.0.1")
    implementation("io.github.big-gates:firebase-storage:0.0.1")
}
```

## Quick Start (JVM)

### 1) Initialize Firebase

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

### 2) Messaging (Topic + Token + FCM Send)

```kotlin
import com.biggates.firebase.common.Firebase
import com.biggates.firebase.message.FirebaseJvmFcmMessage
import com.biggates.firebase.message.messaging
import com.biggates.firebase.message.sendMessage
import com.biggates.firebase.message.setMessagingRegistrationToken

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
            data = mapOf("source" to "jvm", "message" to "hello")
        )
    )
}
```

### 3) Pub/Sub Consume + Publish

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
        config = FirebaseJvmPubSubPublishConfig(endpoint = null)
    )

    // subscription.stop()
}
```

### 4) Storage (Reference + Bytes)

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

## Important Notes

- On JVM, calling only `initializeApp(context)` fails. Use `initializeApp(context, options)`.
- If `serviceAccountPath` is omitted on JVM, ADC (Application Default Credentials) is used.
- `projectId` is required for JVM Pub/Sub APIs.
- `storageBucket` is required when using default `Firebase.storage` on JVM.
- `StorageReference.putBytes(...)` is currently pending on iOS target.

## Build Verification

```bash
./gradlew :firebase-common:check :firebase-message:check :firebase-storage:check
./gradlew :firebase-common:compileKotlinJvm :firebase-message:compileKotlinJvm :firebase-storage:compileKotlinJvm
```

## Project Docs

- Public API catalog: [`docs/public-api/README.md`](docs/public-api/README.md)
- Repository agent guide: [`AGENTS.md`](AGENTS.md)
- [`docs/agents/firebase-common.md`](docs/agents/firebase-common.md)
- [`docs/agents/firebase-message.md`](docs/agents/firebase-message.md)
- [`docs/agents/firebase-storage.md`](docs/agents/firebase-storage.md)
