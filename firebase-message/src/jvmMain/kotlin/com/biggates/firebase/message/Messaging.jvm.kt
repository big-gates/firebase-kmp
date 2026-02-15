package com.biggates.firebase.message

import com.biggates.firebase.common.Firebase

private fun unsupported(): Nothing {
    throw UnsupportedOperationException(
        "Firebase Messaging JVM target is not backed by a Firebase client SDK. Use Android or iOS target."
    )
}

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging()

actual class FirebaseMessaging internal constructor() {
    actual fun subscribeToTopic(topic: String) {
        unsupported()
    }

    actual fun unsubscribeFromTopic(topic: String) {
        unsupported()
    }

    actual suspend fun getToken(): String {
        unsupported()
    }

    actual suspend fun deleteToken() {
        unsupported()
    }
}
