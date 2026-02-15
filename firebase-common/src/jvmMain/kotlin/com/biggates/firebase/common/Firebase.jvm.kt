package com.biggates.firebase.common

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
        return FirebaseJvmRegistry.delete(name)
    }
}

actual fun Firebase.initializeApp(context: PlatformContext): FirebaseApp {
    return FirebaseJvmRegistry.get(DEFAULT_APP_NAME)
        ?.let(::FirebaseApp)
        ?: error("JVM target cannot auto-configure Firebase. Call Firebase.initializeApp(context, options) first.")
}

actual fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions): FirebaseApp {
    return FirebaseApp(FirebaseJvmRegistry.putIfAbsent(DEFAULT_APP_NAME, options))
}

actual fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions, name: String): FirebaseApp {
    return FirebaseApp(FirebaseJvmRegistry.putIfAbsent(name, options))
}

actual fun Firebase.getApps(context: PlatformContext): List<FirebaseApp> {
    return FirebaseJvmRegistry.getAll().map(::FirebaseApp)
}
