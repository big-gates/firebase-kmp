package com.biggates.firebase.storage

import com.biggates.firebase.common.Firebase

/**
 * Returns the default [FirebaseStorage] instance.
 */
expect val Firebase.storage: FirebaseStorage

/**
 * Returns a [FirebaseStorage] instance bound to the given bucket URL.
 */
expect fun Firebase.storage(url: String): FirebaseStorage

/**
 * Entrypoint for Firebase Cloud Storage operations.
 */
expect class FirebaseStorage {
    /**
     * Points this instance to a local emulator instead of production backend.
     */
    fun useEmulator(host: String, port: Int)

    /**
     * Returns the root reference for this storage bucket.
     */
    val reference: StorageReference

    /**
     * Returns a reference for the given relative path.
     */
    fun reference(path: String): StorageReference

    /**
     * Returns a reference from a full `gs://` or HTTPS URL.
     */
    fun referenceFromUrl(url: String): StorageReference
}

/**
 * Storage object reference abstraction.
 */
expect class StorageReference {
    val bucket: String
    val fullPath: String
    val name: String

    fun child(path: String): StorageReference

    /**
     * Uploads raw bytes to this object path, replacing existing object if it exists.
     */
    suspend fun putBytes(bytes: ByteArray)

    /**
     * Downloads object bytes up to [maxDownloadSizeBytes].
     */
    suspend fun getBytes(maxDownloadSizeBytes: Long): ByteArray

    suspend fun getDownloadUrl(): String

    suspend fun delete()
}
