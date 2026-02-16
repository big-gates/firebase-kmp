package com.biggates.firebase.storage

import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageReference
import cocoapods.FirebaseStorage.FIRStorageUploadTask
import com.biggates.firebase.common.Firebase
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy
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
    @OptIn(UnsafeNumber::class)
    actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host = host, port = port.convert())
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

    actual suspend fun putBytes(bytes: ByteArray) {
        suspendCancellableCoroutine { cont ->
            try {
                val data = bytes.toNSData()
                val task: FIRStorageUploadTask = ios.putData(data, null) { _, error ->
                    if (error == null) {
                        cont.resume(Unit)
                    } else {
                        cont.resumeWithException(Exception(error.toString()))
                    }
                }

                cont.invokeOnCancellation {
                    task.cancel()
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }

    actual suspend fun getBytes(maxDownloadSizeBytes: Long): ByteArray =
        suspendCancellableCoroutine { cont ->
            try {
                ios.dataWithMaxSize(maxDownloadSizeBytes) { data, error ->
                    if (error == null) {
                        cont.resume(data?.toByteArray() ?: ByteArray(0))
                    } else {
                        cont.resumeWithException(Exception(error.toString()))
                    }
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
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

@OptIn(UnsafeNumber::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)

    val out = ByteArray(size)
    out.usePinned { pinned ->
        memcpy(
            pinned.addressOf(0),
            bytes,
            size.convert(),
        )
    }
    return out
}

@OptIn(BetaInteropApi::class, UnsafeNumber::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()

    return usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = size.convert(),
        )
    }
}
