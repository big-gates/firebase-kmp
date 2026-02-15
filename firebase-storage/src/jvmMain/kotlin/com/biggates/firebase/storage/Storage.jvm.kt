package com.biggates.firebase.storage

import com.biggates.firebase.common.Firebase
import com.biggates.firebase.common.FirebaseApp
import com.biggates.firebase.common.FirebaseOptions
import com.biggates.firebase.common.adminApp
import com.biggates.firebase.common.app
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.firebase.cloud.StorageClient
import java.io.FileInputStream
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private object FirebaseStorageJvmRuntime {
    private val clients = mutableMapOf<String, Storage>()

    fun storageService(app: FirebaseApp, bucketName: String, hostOverride: String?, portOverride: Int?): Storage {
        val host = hostOverride?.takeIf { it.isNotBlank() }
        val port = portOverride
        if (host == null) {
            return StorageClient.getInstance(app.adminApp()).bucket(bucketName).storage
        }

        val key = "${app.name}:${host}:${port ?: -1}"

        synchronized(this) {
            clients[key]?.let { return it }

            val client = buildEmulatorStorageClient(app.options, host, port)
            clients[key] = client
            return client
        }
    }

    private fun buildEmulatorStorageClient(
        options: FirebaseOptions,
        host: String?,
        port: Int?,
    ): Storage {
        val credentials = options.serviceAccountPath
            ?.takeIf { it.isNotBlank() }
            ?.let { path ->
            FileInputStream(path).use { stream -> GoogleCredentials.fromStream(stream) }
            }
            ?: GoogleCredentials.getApplicationDefault()

        val builder = StorageOptions.newBuilder()
            .setCredentials(credentials)

        options.projectId
            ?.takeIf { it.isNotBlank() }
            ?.let(builder::setProjectId)

        val endpointBase = if (host?.startsWith("http://") == true || host?.startsWith("https://") == true) {
            host
        } else {
            "http://$host"
        }
        val endpoint = port?.let { "$endpointBase:$it" } ?: endpointBase
        builder.setHost(endpoint)

        return builder.build().service
    }
}

private data class ParsedStorageUrl(val bucket: String, val path: String?)

private fun parseStorageUrl(url: String): ParsedStorageUrl {
    val trimmed = url.trim()
    require(trimmed.isNotBlank()) { "url must not be blank." }

    if (trimmed.startsWith("gs://")) {
        val rest = trimmed.removePrefix("gs://")
        val slash = rest.indexOf('/')
        return if (slash == -1) {
            ParsedStorageUrl(bucket = rest, path = null)
        } else {
            ParsedStorageUrl(
                bucket = rest.substring(0, slash),
                path = rest.substring(slash + 1).takeIf { it.isNotBlank() },
            )
        }
    }

    val uri = URI(trimmed)
    val host = uri.host.orEmpty()
    val path = uri.path.orEmpty()

    if (host.contains("firebasestorage.googleapis.com")) {
        val segments = path.trim('/').split('/')
        val bucketIndex = segments.indexOf("b")
        val objectIndex = segments.indexOf("o")
        if (bucketIndex != -1 && bucketIndex + 1 < segments.size) {
            val bucket = segments[bucketIndex + 1]
            val encodedObject = if (objectIndex != -1 && objectIndex + 1 < segments.size) {
                segments.subList(objectIndex + 1, segments.size).joinToString("/")
            } else {
                ""
            }
            return ParsedStorageUrl(
                bucket = bucket,
                path = encodedObject.takeIf { it.isNotBlank() }?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8)
                },
            )
        }
    }

    if (host == "storage.googleapis.com") {
        val segments = path.trim('/').split('/')
        require(segments.isNotEmpty() && segments[0].isNotBlank()) {
            "Cannot parse bucket from URL: $url"
        }
        val bucket = segments[0]
        val objectPath = segments.drop(1).joinToString("/").takeIf { it.isNotBlank() }
        return ParsedStorageUrl(bucket = bucket, path = objectPath)
    }

    error("Unsupported storage URL format: $url")
}

private fun combinePath(parent: String, child: String): String {
    val p = parent.trim('/')
    val c = child.trim('/')
    if (p.isEmpty()) return c
    if (c.isEmpty()) return p
    return "$p/$c"
}

private fun encodePath(path: String): String {
    return path.split('/').joinToString("/") { segment ->
        encodePathComponent(segment)
    }
}

private fun encodePathComponent(value: String): String {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}

actual val Firebase.storage: FirebaseStorage
    get() = FirebaseStorage(bucketName = requireDefaultBucket(Firebase.app), emulatorHost = null, emulatorPort = null)

actual fun Firebase.storage(url: String): FirebaseStorage {
    val parsed = parseStorageUrl(url)
    return FirebaseStorage(
        bucketName = parsed.bucket,
        emulatorHost = null,
        emulatorPort = null,
    )
}

actual class FirebaseStorage internal constructor(
    private val bucketName: String,
    private var emulatorHost: String?,
    private var emulatorPort: Int?,
) {
    actual fun useEmulator(host: String, port: Int) {
        require(host.isNotBlank()) { "host must not be blank." }
        emulatorHost = host
        emulatorPort = port
    }

    actual val reference: StorageReference
        get() = StorageReference(bucketName, "", emulatorHost, emulatorPort)

    actual fun reference(path: String): StorageReference {
        return StorageReference(bucketName, path.trim('/'), emulatorHost, emulatorPort)
    }

    actual fun referenceFromUrl(url: String): StorageReference {
        val parsed = parseStorageUrl(url)
        return StorageReference(parsed.bucket, parsed.path.orEmpty(), emulatorHost, emulatorPort)
    }
}

actual class StorageReference internal constructor(
    private val bucketName: String,
    private val objectPath: String,
    private val emulatorHost: String?,
    private val emulatorPort: Int?,
) {
    actual val bucket: String
        get() = bucketName

    actual val fullPath: String
        get() = objectPath

    actual val name: String
        get() = objectPath.substringAfterLast('/', "")

    actual fun child(path: String): StorageReference {
        return StorageReference(
            bucketName = bucketName,
            objectPath = combinePath(objectPath, path),
            emulatorHost = emulatorHost,
            emulatorPort = emulatorPort,
        )
    }

    actual suspend fun putBytes(bytes: ByteArray) {
        require(objectPath.isNotBlank()) { "Cannot upload bytes to root storage reference." }

        val service = FirebaseStorageJvmRuntime.storageService(Firebase.app, bucketName, emulatorHost, emulatorPort)
        service.create(BlobInfo.newBuilder(bucketName, objectPath).build(), bytes)
    }

    actual suspend fun getBytes(maxDownloadSizeBytes: Long): ByteArray {
        require(objectPath.isNotBlank()) { "Cannot download bytes for root storage reference." }
        require(maxDownloadSizeBytes >= 0) { "maxDownloadSizeBytes must be non-negative." }

        val service = FirebaseStorageJvmRuntime.storageService(Firebase.app, bucketName, emulatorHost, emulatorPort)
        val blob = service.get(bucketName, objectPath)
            ?: error("Storage object does not exist: gs://$bucketName/$objectPath")

        if (blob.size > maxDownloadSizeBytes) {
            error(
                "Storage object exceeds maxDownloadSizeBytes: " +
                    "size=${blob.size}, max=$maxDownloadSizeBytes, path=gs://$bucketName/$objectPath"
            )
        }

        return blob.getContent()
    }

    actual suspend fun getDownloadUrl(): String {
        require(objectPath.isNotBlank()) { "Cannot get download URL for root storage reference." }

        val service = FirebaseStorageJvmRuntime.storageService(Firebase.app, bucketName, emulatorHost, emulatorPort)
        val blob = service.get(bucketName, objectPath)
            ?: error("Storage object does not exist: gs://$bucketName/$objectPath")

        return blob.toDownloadUrl(bucketName, objectPath)
    }

    actual suspend fun delete() {
        require(objectPath.isNotBlank()) { "Cannot delete root storage reference." }

        val service = FirebaseStorageJvmRuntime.storageService(Firebase.app, bucketName, emulatorHost, emulatorPort)
        val deleted = service.delete(bucketName, objectPath)
        if (!deleted) {
            error("Storage object does not exist or could not be deleted: gs://$bucketName/$objectPath")
        }
    }
}

private fun requireDefaultBucket(app: FirebaseApp): String {
    return app.options.storageBucket?.takeIf { it.isNotBlank() }
        ?: error(
            "JVM Firebase Storage requires storageBucket in FirebaseOptions. " +
                "Set it when calling Firebase.initializeApp(context, options)."
        )
}

private fun Blob.toDownloadUrl(bucket: String, objectPath: String): String {
    val token = metadata
        ?.get("firebaseStorageDownloadTokens")
        ?.split(',')
        ?.firstOrNull { it.isNotBlank() }
        ?.trim()

    val encodedObject = encodePathComponent(objectPath)
    if (!token.isNullOrBlank()) {
        return "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedObject?alt=media&token=$token"
    }

    return "https://storage.googleapis.com/$bucket/${encodePath(objectPath)}"
}
