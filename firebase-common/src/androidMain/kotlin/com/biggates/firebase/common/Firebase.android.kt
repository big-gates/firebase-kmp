package com.biggates.firebase.common

import android.app.Application
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.FirebaseApp as AndroidFirebaseApp
import com.google.firebase.FirebaseOptions as AndroidOptions

actual typealias PlatformContext = Application

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(AndroidFirebaseApp.getInstance())

actual fun Firebase.app(name: String): FirebaseApp {
    return FirebaseApp(AndroidFirebaseApp.getInstance(name))
}

actual class FirebaseApp internal constructor(private val delegate: AndroidFirebaseApp) {
    actual val name: String get() = delegate.name
    actual val options: FirebaseOptions = delegate.options.let { option ->
        FirebaseOptions(
            applicationId = option.applicationId,
            apiKey = option.apiKey,
            databaseUrl = option.databaseUrl,
            gaTrackingId = option.gaTrackingId,
            storageBucket = option.storageBucket,
            projectId = option.projectId,
            gcmSenderId = option.gcmSenderId,
        )
    }

    actual var dataCollectionDefaultEnabled: Boolean?
        get() = delegate.isDataCollectionDefaultEnabled
        set(v) { delegate.setDataCollectionDefaultEnabled(v) }

    actual var automaticResourceManagementEnabled: Boolean
        get() = false
        set(v) { delegate.setAutomaticResourceManagementEnabled(v) }

    actual val persistenceKey: String? get() = delegate.persistenceKey

    actual suspend fun delete(): Boolean = suspendCoroutine { continuation ->
        try {
            val appName = delegate.name
            delegate.delete()

            val stillExists = AndroidFirebaseApp.getApps(delegate.applicationContext)
                .any { it.name == appName }

            continuation.resume(!stillExists)
        } catch (e: Exception) {
            Log.w(this::class.java.simpleName, "FirebaseApp Delete Fail", e)
            continuation.resume(false)
        }
    }
}

actual fun Firebase.initializeApp(context: PlatformContext): FirebaseApp {
    val firebaseAndroid = AndroidFirebaseApp.initializeApp(context)
        ?: error("Default FirebaseApp is not initialized. Call Firebase.initializeApp(options) first.")
    return FirebaseApp(firebaseAndroid)
}

actual fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions): FirebaseApp {
    val firebaseAndroid = AndroidFirebaseApp.initializeApp(context, options.toAndroid())
    return FirebaseApp(firebaseAndroid)
}

actual fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions, name: String): FirebaseApp {
    val firebaseAndroid = AndroidFirebaseApp.initializeApp(context, options.toAndroid(), name)
    return FirebaseApp(firebaseAndroid)
}

actual fun Firebase.getApps(context: PlatformContext): List<FirebaseApp> = AndroidFirebaseApp.getApps(context).map { FirebaseApp(it) }

private fun FirebaseOptions.toAndroid(): AndroidOptions = AndroidOptions.Builder()
    .setApiKey(apiKey)
    .setApplicationId(applicationId)
    .setProjectId(projectId)
    .setGcmSenderId(gcmSenderId)
    .setStorageBucket(storageBucket)
    .build()