# firebase-kmp

Firebase API를 Kotlin Multiplatform(Android/iOS/JVM) 공통 코드에서 사용할 수 있도록 래핑한 오픈소스 라이브러리입니다.

## Modules
- `firebase-common`: Firebase 앱 초기화/앱 인스턴스 API
- `firebase-message`: FCM topic/token API + Pub/Sub 구독 API(JVM 전용)
- `firebase-storage`: Cloud Storage reference API

## Targets
- Android
- iOS (`iosX64`, `iosArm64`, `iosSimulatorArm64`)
- JVM

## API 호환 커버리지

목표: KMP(Android/iOS/JVM) 기준 Firebase API 호환 커버리지와 public API 커버리지를 100%까지 확대.

커버리지 산정 방식:
- 제품군 커버리지 = 구현된 Firebase 제품군 수 / 목표 제품군 수
- public API 커버리지 = 구현된 public API 수 / 목표 public API 수 (제품군별, 플랫폼별)
- 구현 모듈 깊이 상태 = `not started`, `partial`, `near parity`, `parity`

목표 제품군 (18개):
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

현재 상태 (업데이트: 2026-02-15):
- 제품군 커버리지: `3 / 18 = 16.7%`
- public API 카탈로그: `docs/public-api/README.md`
- public API 커버리지(모듈별):
- `firebase-common`: `21 / 25 = 84.0%` (`docs/public-api/firebase-common.md`)
- `firebase-message`: `19 / 36 = 52.8%` (`docs/public-api/firebase-message.md`)
- `firebase-storage`: `13 / 31 = 41.9%` (`docs/public-api/firebase-storage.md`)
- 구현 모듈:
- `firebase-common` (Core): `partial`
- `firebase-message` (Cloud Messaging + JVM Pub/Sub 소비 경로): `partial`
- `firebase-storage` (Cloud Storage): `partial`

## Installation

Maven 좌표 (버전 예시: `0.0.1`)

```kotlin
dependencies {
    implementation("io.github.big-gates:firebase-common:0.0.1")
    implementation("io.github.big-gates:firebase-message:0.0.1")
    implementation("io.github.big-gates:firebase-storage:0.0.1")
}
```

## Usage

### 1) Firebase 초기화 (`firebase-common`)

JVM에서는 `Firebase.initializeApp(context, options)` 호출 시 Admin SDK도 함께 초기화됩니다.

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
            serviceAccountPath = "/absolute/path/service-account.json" // JVM 전용
        )
    )
}
```

참고:
- JVM에서 `initializeApp(context)`만 호출하면 실패합니다. 옵션 기반 초기화가 필요합니다.
- `serviceAccountPath`를 생략하면 ADC(Application Default Credentials)를 사용합니다.
- 기본 `Firebase.storage` 사용 시 `storageBucket` 설정이 필요합니다.

### 2) JVM Messaging (`firebase-message`)

JVM은 모바일 클라이언트 SDK처럼 FCM 토큰을 직접 발급하지 않으므로, 토큰을 명시적으로 설정해야 합니다.

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

### 3) JVM에서 데이터 구독/처리 (`firebase-message`, Pub/Sub)

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
            // 선택 사항. 에뮬레이터 사용 시: "localhost:8085"
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

참고:
- `Firebase.initializeApp(...)` 시 `FirebaseOptions.projectId` 설정이 필요합니다.
- 반환되는 `FirebaseJvmPubSubSubscription`은 종료 시 `subscription.stop()`으로 정리하세요.

### 4) JVM Storage (`firebase-storage`)

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

    // Storage Emulator 사용 시
    storage.useEmulator("127.0.0.1", 9199)
}
```

## Verify Build

```bash
./gradlew :firebase-common:check :firebase-message:check :firebase-storage:check
./gradlew :firebase-common:compileKotlinJvm :firebase-message:compileKotlinJvm :firebase-storage:compileKotlinJvm
```
