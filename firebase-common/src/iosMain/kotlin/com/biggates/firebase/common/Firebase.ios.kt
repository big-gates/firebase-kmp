package com.biggates.firebase.common

import cocoapods.FirebaseCore.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class PlatformContext // iOS에서는 의미 없음

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(FIRApp.defaultApp()!!)

actual fun Firebase.app(name: String): FirebaseApp {
    return FirebaseApp(FIRApp.appNamed(name)!!)
}
actual class FirebaseApp internal constructor(
    private val ios: FIRApp
) {
    actual val name: String
        get() = ios.name

    actual val options: FirebaseOptions
        get() = ios.options.let { option ->
            FirebaseOptions(
                applicationId = option.bundleID,
                apiKey = option.APIKey!!,
                databaseUrl = option.databaseURL!!,
                gaTrackingId = option.trackingID,
                storageBucket = option.storageBucket,
                projectId = option.projectID,
                gcmSenderId = option.GCMSenderID
            )
        }

    // iOS FirebaseCore에 전역 토글이 없으므로 로컬 속성으로 제공(선택)
    private var _dataCollectionDefaultEnabled: Boolean? = null
    private var _automaticResourceManagementEnabled: Boolean = false

    actual var dataCollectionDefaultEnabled: Boolean?
        get() = _dataCollectionDefaultEnabled
        set(value) {
            _dataCollectionDefaultEnabled = value
        }

    actual var automaticResourceManagementEnabled: Boolean
        get() = _automaticResourceManagementEnabled
        set(value) { _automaticResourceManagementEnabled = value }

    actual val persistenceKey: String?
        get() = "$name+${options.applicationId}"

    actual suspend fun delete(): Boolean = suspendCoroutine { continuation ->
        ios.deleteApp { success ->
            continuation.resume(success)
        }
    }
}

actual fun Firebase.initializeApp(context: PlatformContext): FirebaseApp {
    val firebaseIos = FIRApp.defaultApp()
        ?: error("Default FirebaseApp is not initialized. Call Firebase.initializeApp(options) first.")
    return FirebaseApp(firebaseIos)
}

actual fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions): FirebaseApp {
    return FIRApp.configureWithOptions(options.toIos()).let { app }
}

actual fun Firebase.initializeApp(
    context: PlatformContext,
    options: FirebaseOptions,
    name: String
): FirebaseApp {
    return FIRApp.configureWithName(name, options.toIos()).let { app(name) }
}

actual fun Firebase.getApps(context: PlatformContext): List<FirebaseApp> = FIRApp.allApps()
    .orEmpty()
    .values
    .map { FirebaseApp(it as FIRApp) }

private fun FirebaseOptions.toIos() =
    FIROptions(this@toIos.applicationId, this@toIos.gcmSenderId ?: "").apply {
        APIKey = this@toIos.apiKey
        databaseURL = this@toIos.databaseUrl
        trackingID = this@toIos.gaTrackingId
        storageBucket = this@toIos.storageBucket
        projectID = this@toIos.projectId
    }