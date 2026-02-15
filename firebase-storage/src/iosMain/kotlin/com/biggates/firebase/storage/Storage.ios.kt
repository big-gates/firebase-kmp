package com.biggates.firebase.storage

import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageReference
import com.biggates.firebase.common.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual val Firebase.storage: FirebaseStorage
    get() = FirebaseStorage(FIRStorage.storage())

actual fun Firebase.storage(url: String): FirebaseStorage {
    return FirebaseStorage(FIRStorage.storageWithURL(url))
}

actual class FirebaseStorage internal constructor(
    internal val ios: FIRStorage,
) {
    actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host = host, port = port.toLong())
    }

    actual val reference: StorageReference
        get() = StorageReference(ios.reference())

    actual fun reference(path: String): StorageReference {
        return StorageReference(ios.referenceWithPath(path))
    }

    actual fun referenceFromUrl(url: String): StorageReference {
        return StorageReference(ios.referenceForURL(url))
    }
}

actual class StorageReference internal constructor(
    internal val ios: FIRStorageReference,
) {
    actual val bucket: String
        get() = ios.bucket()

    actual val fullPath: String
        get() = ios.fullPath()

    actual val name: String
        get() = ios.name()

    actual fun child(path: String): StorageReference {
        return StorageReference(ios.child(path))
    }

    actual suspend fun getDownloadUrl(): String =
        suspendCancellableCoroutine { cont ->
            try {
                ios.downloadURLWithCompletion { url, error ->
                    if (error == null) {
                        cont.resume(url?.absoluteString.orEmpty())
                    } else {
                        cont.resumeWithException(Exception(error.toString()))
                    }
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }

    actual suspend fun delete() =
        suspendCancellableCoroutine { cont ->
            try {
                ios.deleteWithCompletion { error ->
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
