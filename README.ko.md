# firebase-kmp

Firebase API를 Kotlin Multiplatform(Android/Apple/JVM) 공통 코드에서 사용할 수 있도록 래핑한 오픈소스 라이브러리입니다.

언어:
- 영어: [`README.md`](README.md)
- 한국어 (이 문서)

## 왜 firebase-kmp인가?
- KMP 공유 코드에서 Firebase API를 Firebase 스타일로 사용할 수 있습니다.
- Android, Apple, JVM 동작을 가능한 범위에서 일관되게 맞춥니다.
- JVM 서버 사이드 워크플로우를 Firebase Admin SDK 기반으로 지원합니다.

## 모듈 매트릭스

| 모듈 | Firebase 제품군 | 현재 범위 | Public API 커버리지 | 상태 |
| --- | --- | --- | --- | --- |
| `firebase-common` | Core | 앱 초기화, 앱 인스턴스, 옵션, 앱 레지스트리 | `21 / 25 = 84.0%` | `partial` |
| `firebase-message` | Cloud Messaging | topic/token API + JVM Pub/Sub 구독/발행 + JVM 전송 API | `19 / 36 = 52.8%` | `partial` |
| `firebase-storage` | Cloud Storage | 스토리지 레퍼런스 API + 바이트/다운로드/삭제 (참고 사항 확인) | `13 / 31 = 41.9%` | `partial` |

## 타겟

| 타겟 | 지원 |
| --- | --- |
| Android | Yes |
| Apple (`ios*`, `macos*`, `tvos*`, `watchos*`) | Yes |
| JVM | Yes |

참고:
- visionOS 타겟은 Firebase SDK 호환성과 KMP interop 안정성 검증 후 확장 예정입니다.

## API 호환 커버리지 (업데이트: 2026-02-15)

목표:
- KMP 기준 Firebase API 호환 커버리지 `100%`
- 지원 모듈별 Public API 커버리지 `100%`

커버리지 산정 방식:
- 제품군 커버리지 = 구현된 Firebase 제품군 수 / 목표 제품군 수
- Public API 커버리지 = 구현된 public API 수 / 목표 public API 수 (제품군별, 플랫폼별)
- 구현 깊이 상태 = `not started`, `partial`, `near parity`, `parity`

현재 스냅샷:
- 제품군 커버리지: `3 / 18 = 16.7%`
- Public API 카탈로그: [`docs/public-api/README.md`](docs/public-api/README.md)

| 모듈 | 커버리지 | 카탈로그 |
| --- | --- | --- |
| `firebase-common` | `21 / 25 = 84.0%` | [`docs/public-api/firebase-common.md`](docs/public-api/firebase-common.md) |
| `firebase-message` | `19 / 36 = 52.8%` | [`docs/public-api/firebase-message.md`](docs/public-api/firebase-message.md) |
| `firebase-storage` | `13 / 31 = 41.9%` | [`docs/public-api/firebase-storage.md`](docs/public-api/firebase-storage.md) |

목표 제품군 (18개):
- Core, Cloud Messaging, Cloud Storage, Authentication, Cloud Firestore, Realtime Database, Cloud Functions, Analytics, Crashlytics, Performance Monitoring, Remote Config, App Check, In-App Messaging, Dynamic Links, Installations, ML, Data Connect, Vertex AI in Firebase

## 설치

Maven 좌표 (버전 예시: `0.0.1`):

```kotlin
dependencies {
    implementation("io.github.big-gates:firebase-common:0.0.1")
    implementation("io.github.big-gates:firebase-message:0.0.1")
    implementation("io.github.big-gates:firebase-storage:0.0.1")
}
```

## Quick Start (JVM)

### 1) Firebase 초기화

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

### 2) Messaging (Topic + Token + FCM 전송)

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

### 3) Pub/Sub 구독 + 발행

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
        config = FirebaseJvmPubSubPublishConfig(endpoint = null)
    )

    // subscription.stop()
}
```

### 4) Storage (레퍼런스 + 바이트)

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

    // Storage Emulator
    storage.useEmulator("127.0.0.1", 9199)
}
```

## 중요 참고사항

- JVM에서 `initializeApp(context)`만 호출하면 실패합니다. `initializeApp(context, options)`를 사용해야 합니다.
- JVM에서 `serviceAccountPath`를 생략하면 ADC(Application Default Credentials)를 사용합니다.
- JVM Pub/Sub API 사용 시 `projectId`가 필요합니다.
- JVM 기본 `Firebase.storage` 사용 시 `storageBucket` 설정이 필요합니다.
- `StorageReference.putBytes(...)`는 현재 iOS 타겟 구현이 진행 중입니다.

## 빌드 검증

```bash
./gradlew :firebase-common:check :firebase-message:check :firebase-storage:check
./gradlew :firebase-common:compileKotlinJvm :firebase-message:compileKotlinJvm :firebase-storage:compileKotlinJvm
```

## 프로젝트 문서

- Public API 카탈로그: [`docs/public-api/README.md`](docs/public-api/README.md)
- 저장소 에이전트 가이드: [`AGENTS.md`](AGENTS.md)
- [`docs/agents/firebase-common.md`](docs/agents/firebase-common.md)
- [`docs/agents/firebase-message.md`](docs/agents/firebase-message.md)
- [`docs/agents/firebase-storage.md`](docs/agents/firebase-storage.md)
