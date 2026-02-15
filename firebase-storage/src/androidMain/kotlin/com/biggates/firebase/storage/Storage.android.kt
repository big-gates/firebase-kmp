package com.biggates.firebase.storage

import com.biggates.firebase.common.Firebase
import com.google.firebase.storage.FirebaseStorage as AndroidFirebaseStorage
import com.google.firebase.storage.StorageReference as AndroidStorageReference
import kotlinx.coroutines.tasks.await

actual val Firebase.storage: FirebaseStorage
    get() = FirebaseStorage(AndroidFirebaseStorage.getInstance())

actual fun Firebase.storage(url: String): FirebaseStorage {
    return FirebaseStorage(AndroidFirebaseStorage.getInstance(url))
}

actual class FirebaseStorage internal constructor(
    internal val android: AndroidFirebaseStorage,
) {
    actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
    }

    actual val reference: StorageReference
        get() = StorageReference(android.reference)

    actual fun reference(path: String): StorageReference {
        return StorageReference(android.getReference(path))
    }

    actual fun referenceFromUrl(url: String): StorageReference {
        return StorageReference(android.getReferenceFromUrl(url))
    }
}

actual class StorageReference internal constructor(
    internal val android: AndroidStorageReference,
) {
    actual val bucket: String
        get() = android.bucket

    actual val fullPath: String
        get() = android.path

    actual val name: String
        get() = android.name

    actual fun child(path: String): StorageReference {
        return StorageReference(android.child(path))
    }

    actual suspend fun putBytes(bytes: ByteArray) {
        android.putBytes(bytes).await()
    }

    actual suspend fun getBytes(maxDownloadSizeBytes: Long): ByteArray {
        return android.getBytes(maxDownloadSizeBytes).await()
    }

    actual suspend fun getDownloadUrl(): String {
        return android.downloadUrl.await().toString()
    }

    actual suspend fun delete() {
        android.delete().await()
    }
}
