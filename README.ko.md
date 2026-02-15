# firebase-kmp

Firebase API를 Kotlin Multiplatform(Android/iOS/JVM) 공통 코드에서 사용할 수 있도록 래핑한 오픈소스 라이브러리입니다.

## Modules
- `firebase-common`: Firebase 앱 초기화/앱 인스턴스 API
- `firebase-message`: FCM topic/token API
- `firebase-storage`: Cloud Storage reference API

## Targets
- Android
- iOS (`iosX64`, `iosArm64`, `iosSimulatorArm64`)
- JVM

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

suspend fun messagingJvmSample() {
    Firebase.setMessagingRegistrationToken("fcm_registration_token")

    Firebase.messaging.subscribeToTopic("news")
    val token = Firebase.messaging.getToken()
    Firebase.messaging.unsubscribeFromTopic("news")
    Firebase.messaging.deleteToken()
}
```

### 3) JVM Storage (`firebase-storage`)

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

    // Storage Emulator 사용 시
    storage.useEmulator("127.0.0.1", 9199)
}
```

## Verify Build

```bash
./gradlew :firebase-common:check :firebase-message:check :firebase-storage:check
./gradlew :firebase-common:compileKotlinJvm :firebase-message:compileKotlinJvm :firebase-storage:compileKotlinJvm
```
