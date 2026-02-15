package com.biggates.firebase.common

object Firebase


expect class PlatformContext

expect val Firebase.app: FirebaseApp

expect fun Firebase.app(name: String): FirebaseApp

expect class FirebaseApp {
    val name: String
    val options: FirebaseOptions

    var dataCollectionDefaultEnabled: Boolean?
    var automaticResourceManagementEnabled: Boolean

    val persistenceKey: String?

    suspend fun delete(): Boolean
}

data class FirebaseOptions(
    /** The Google App ID that is used to uniquely identify an instance of an app. */
    val applicationId: String,

    /**
     * API key used for authenticating requests from your app, e.g.
     * AIzaSyDdVgKwhZl0sTTTLZ7iTmt1r3N2cJLnaDk, used to identify your app to Google servers.
     */
    val apiKey: String,

    /** The database root URL, e.g. http://abc-xyz-123.firebaseio.com. */
    val databaseUrl: String? = null,

    /**
     * The tracking ID for Google Analytics, e.g. UA-12345678-1, used to configure Google Analytics.
     */
    val gaTrackingId: String? = null,

    /** The Google Cloud Storage bucket name, e.g. abc-xyz-123.storage.firebase.com. */
    val storageBucket: String? = null,

    /** The Google Cloud project ID, e.g. my-project-1234 */
    val projectId: String? = null,

    /**
     * The Project Number from the Google Developer's console, for example 012345678901, used to
     * configure Google Cloud Messaging.
     */
    val gcmSenderId: String? = null,

    /** The auth domain. */
    val authDomain: String? = null,

    /**
     * JVM-only option. Absolute path to a Firebase Admin service account JSON.
     *
     * If null on JVM, Application Default Credentials are used.
     */
    val serviceAccountPath: String? = null,
)

expect fun Firebase.initializeApp(context: PlatformContext): FirebaseApp

expect fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions): FirebaseApp

expect fun Firebase.initializeApp(context: PlatformContext, options: FirebaseOptions, name: String): FirebaseApp

expect fun Firebase.getApps(context: PlatformContext): List<FirebaseApp>
