package com.biggates.firebase.storage

import com.biggates.firebase.common.Firebase

private fun unsupported(): Nothing {
    throw UnsupportedOperationException(
        "Firebase Storage JVM target is not backed by a Firebase client SDK. Use Android or iOS target."
    )
}

actual val Firebase.storage: FirebaseStorage
    get() = FirebaseStorage()

actual fun Firebase.storage(url: String): FirebaseStorage {
    return FirebaseStorage()
}

actual class FirebaseStorage internal constructor() {
    actual fun useEmulator(host: String, port: Int) {
        unsupported()
    }

    actual val reference: StorageReference
        get() = unsupported()

    actual fun reference(path: String): StorageReference {
        unsupported()
    }

    actual fun referenceFromUrl(url: String): StorageReference {
        unsupported()
    }
}

actual class StorageReference internal constructor() {
    actual val bucket: String
        get() = unsupported()

    actual val fullPath: String
        get() = unsupported()

    actual val name: String
        get() = unsupported()

    actual fun child(path: String): StorageReference {
        unsupported()
    }

    actual suspend fun getDownloadUrl(): String {
        unsupported()
    }

    actual suspend fun delete() {
        unsupported()
    }
}
