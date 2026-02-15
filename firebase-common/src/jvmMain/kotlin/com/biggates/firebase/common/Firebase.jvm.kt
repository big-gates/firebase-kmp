package com.biggates.firebase.common

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp as AdminFirebaseApp
import com.google.firebase.FirebaseOptions as AdminFirebaseOptions
import java.io.FileInputStream

actual class PlatformContext

private const val DEFAULT_APP_NAME = "[DEFAULT]"

internal data class FirebaseAppState(
    val name: String,
    val options: FirebaseOptions,
    var dataCollectionDefaultEnabled: Boolean? = null,
    var automaticResourceManagementEnabled: Boolean = false,
)

private object FirebaseJvmRegistry {
    private val apps = LinkedHashMap<String, FirebaseAppState>()

    fun get(name: String): FirebaseAppState? = synchronized(this) { apps[name] }

    fun getAll(): List<FirebaseAppState> = synchronized(this) { apps.values.toList() }

    fun putIfAbsent(name: String, options: FirebaseOptions): FirebaseAppState = synchronized(this) {
        apps.getOrPut(name) { FirebaseAppState(name = name, options = options) }
    }

    fun delete(name: String): Boolean = synchronized(this) { apps.remove(name) != null }
}

private object FirebaseAdminJvmRegistry {
    fun ensureApp(name: String, options: FirebaseOptions): AdminFirebaseApp {
        AdminFirebaseApp.getApps().firstOrNull { it.name == name }?.let { return it }
        return AdminFirebaseApp.initializeApp(options.toAdminOptions(), name)
    }

    fun get(name: String): AdminFirebaseApp {
        return AdminFirebaseApp.getApps().firstOrNull { it.name == name }
            ?: error("Firebase Admin app($name) is not initialized. Call Firebase.initializeApp(context, options) first.")
    }

    fun delete(name: String) {
        AdminFirebaseApp.getApps().firstOrNull { it.name == name }?.delete()
    }
}

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(
        FirebaseJvmRegistry.get(DEFAULT_APP_NAME)
            ?: error("Default FirebaseApp is not initialized. Call Firebase.initializeApp(options) first.")
    )

actual fun Firebase.app(name: String): FirebaseApp {
    return FirebaseApp(
        FirebaseJvmRegistry.get(name)
            ?: error("FirebaseApp($name) is not initialized.")
    )
}

actual class FirebaseApp internal constructor(
    private val state: FirebaseAppState,
) {
    actual val name: String
        get() = state.name

    actual val options: FirebaseOptions
        get() = state.options

    actual var dataCollectionDefaultEnabled: Boolean?
        get() = state.dataCollectionDefaultEnabled
        set(value) {
            state.dataCollectionDefaultEnabled = value
        }

    actual var automaticResourceManagementEnabled: Boolean
        get() = state.automaticResourceManagementEnabled
        set(value) {
            state.automaticResourceManagementEnabled = value
        }

    actual val persistenceKey: String?
        get() = "$name+${options.applicationId}"

    actual suspend fun delete(): Boolean {
        val deleted = FirebaseJvmRegistry.delete(name)
        FirebaseAdminJvmRegistry.delete(name)
        return deleted
    }
}

/**
 * JVM-only bridge to Firebase Admin app.
 */
fun FirebaseApp.adminApp(): AdminFirebaseApp {
    return FirebaseAdminJvmRegistry.get(name)
}

/**
 * JVM-only bridge to Firebase Admin app.
 */
fun Firebase.adminApp(name: String = DEFAULT_APP_NAME): AdminFirebaseApp {
    return app(name).adminApp()
}

actual fun Firebase.initializeApp(context: PlatformContext): FirebaseApp {
    return FirebaseJvmRegistry.get(DEFAULT_APP_NAME)
        ?.let(::FirebaseApp)
        ?: error("JVM target cannot auto-configure Firebase. Call Firebase.initializeApp(context, options) first.")
}

actual fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions): FirebaseApp {
    FirebaseAdminJvmRegistry.ensureApp(DEFAULT_APP_NAME, options)
    return FirebaseApp(FirebaseJvmRegistry.putIfAbsent(DEFAULT_APP_NAME, options))
}

actual fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions, name: String): FirebaseApp {
    FirebaseAdminJvmRegistry.ensureApp(name, options)
    return FirebaseApp(FirebaseJvmRegistry.putIfAbsent(name, options))
}

actual fun Firebase.getApps(context: PlatformContext): List<FirebaseApp> {
    return FirebaseJvmRegistry.getAll().map(::FirebaseApp)
}

private fun FirebaseOptions.toAdminOptions(): AdminFirebaseOptions {
    val credentials = serviceAccountPath
        ?.takeIf { it.isNotBlank() }
        ?.let { path ->
            FileInputStream(path).use { stream -> GoogleCredentials.fromStream(stream) }
        }
        ?: GoogleCredentials.getApplicationDefault()

    val builder = AdminFirebaseOptions.builder()
        .setCredentials(credentials)

    projectId
        ?.takeIf { it.isNotBlank() }
        ?.let(builder::setProjectId)

    databaseUrl
        ?.takeIf { it.isNotBlank() }
        ?.let(builder::setDatabaseUrl)

    storageBucket
        ?.takeIf { it.isNotBlank() }
        ?.let(builder::setStorageBucket)

    return builder.build()
}
