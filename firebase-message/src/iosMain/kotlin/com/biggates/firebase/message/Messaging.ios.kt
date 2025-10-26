package com.biggates.firebase.message

import cocoapods.FirebaseMessaging.FIRMessaging
import com.biggates.firebase.common.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(FIRMessaging.messaging())

actual class FirebaseMessaging internal constructor(
    internal val ios: FIRMessaging,
) {
    actual fun subscribeToTopic(topic: String) {
        ios.subscribeToTopic(topic)
    }

    actual fun unsubscribeFromTopic(topic: String) {
        ios.unsubscribeFromTopic(topic)
    }

    actual suspend fun getToken(): String =
        suspendCancellableCoroutine { cont ->
            try {
                ios.tokenWithCompletion { token, error ->
                    if (error == null) {
                        cont.resume(token ?: "")
                    } else {
                        cont.resumeWithException(Exception(error.toString()))
                    }
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }

    actual suspend fun deleteToken() =
        suspendCancellableCoroutine { cont ->
            try {
                ios.deleteTokenWithCompletion { error ->
                    if (error == null) {
                        cont.resume(Unit)
                    } else {
                        cont.resumeWithException(Exception(error.toString()))
                    }
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
}
