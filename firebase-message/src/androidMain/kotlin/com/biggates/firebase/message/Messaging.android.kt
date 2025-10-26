package com.biggates.firebase.message

import com.biggates.firebase.common.Firebase
import kotlinx.coroutines.tasks.await
import com.google.firebase.messaging.FirebaseMessaging as AndroidFirebaseMessaging

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(AndroidFirebaseMessaging.getInstance())

actual class FirebaseMessaging internal constructor(
    internal val android: AndroidFirebaseMessaging,
) {
    actual fun subscribeToTopic(topic: String) {
        android.subscribeToTopic(topic)
    }

    actual fun unsubscribeFromTopic(topic: String) {
        android.unsubscribeFromTopic(topic)
    }

    actual suspend fun getToken(): String = android.token.await()

    actual suspend fun deleteToken() {
        android.deleteToken().await()
    }
}
