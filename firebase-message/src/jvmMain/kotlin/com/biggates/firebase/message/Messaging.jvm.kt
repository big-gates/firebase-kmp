package com.biggates.firebase.message

import com.biggates.firebase.common.Firebase
import com.biggates.firebase.common.adminApp
import com.biggates.firebase.common.app
import com.google.firebase.messaging.FirebaseMessaging as AdminFirebaseMessaging

private object FirebaseMessagingJvmState {
    @Volatile
    private var registrationToken: String? = null

    fun setRegistrationToken(token: String?) {
        registrationToken = token
    }

    fun requireRegistrationToken(): String {
        return registrationToken?.takeIf { it.isNotBlank() }
            ?: error(
                "JVM Firebase Messaging registration token is not set. " +
                    "Use Firebase.setMessagingRegistrationToken(token)."
            )
    }

    fun clearRegistrationToken() {
        registrationToken = null
    }
}

/**
 * Updates JVM-side registration token used by message operations.
 */
fun Firebase.setMessagingRegistrationToken(token: String?) {
    FirebaseMessagingJvmState.setRegistrationToken(token)
}

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging()

actual class FirebaseMessaging internal constructor() {
    actual fun subscribeToTopic(topic: String) {
        require(topic.isNotBlank()) { "topic must not be blank." }

        val token = FirebaseMessagingJvmState.requireRegistrationToken()
        val messaging = AdminFirebaseMessaging.getInstance(Firebase.app.adminApp())
        runCatching { messaging.subscribeToTopic(listOf(token), topic) }
            .getOrElse {
                throw IllegalStateException("Failed to subscribe token to topic '$topic'.", it)
            }
    }

    actual fun unsubscribeFromTopic(topic: String) {
        require(topic.isNotBlank()) { "topic must not be blank." }

        val token = FirebaseMessagingJvmState.requireRegistrationToken()
        val messaging = AdminFirebaseMessaging.getInstance(Firebase.app.adminApp())
        runCatching { messaging.unsubscribeFromTopic(listOf(token), topic) }
            .getOrElse {
                throw IllegalStateException("Failed to unsubscribe token from topic '$topic'.", it)
            }
    }

    actual suspend fun getToken(): String {
        return FirebaseMessagingJvmState.requireRegistrationToken()
    }

    actual suspend fun deleteToken() {
        FirebaseMessagingJvmState.clearRegistrationToken()
    }
}
