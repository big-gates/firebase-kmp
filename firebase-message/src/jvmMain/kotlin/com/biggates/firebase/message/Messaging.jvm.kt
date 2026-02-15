package com.biggates.firebase.message

import com.biggates.firebase.common.Firebase
import com.biggates.firebase.common.FirebaseOptions
import com.biggates.firebase.common.adminApp
import com.biggates.firebase.common.app
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.Subscriber
import com.google.firebase.messaging.Message as AdminMessage
import com.google.firebase.messaging.FirebaseMessaging as AdminFirebaseMessaging
import com.google.firebase.messaging.Notification as AdminNotification
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import com.google.protobuf.ByteString
import java.io.FileInputStream
import java.time.Instant
import java.util.concurrent.TimeUnit

enum class FirebaseJvmAckDecision {
    ACK,
    NACK,
}

data class FirebaseJvmPubSubMessage(
    val messageId: String,
    val dataUtf8: String,
    val attributes: Map<String, String>,
    val orderingKey: String,
    val publishTime: Instant?,
)

data class FirebaseJvmPubSubSubscriberConfig(
    val endpoint: String? = null,
    val parallelPullCount: Int = 1,
)

data class FirebaseJvmPubSubPublishConfig(
    val endpoint: String? = null,
)

class FirebaseJvmPubSubSubscription internal constructor(
    private val subscriber: Subscriber,
) {
    val isRunning: Boolean
        get() = subscriber.isRunning

    fun stop(graceful: Boolean = true) {
        if (graceful) {
            subscriber.stopAsync().awaitTerminated()
        } else {
            subscriber.stopAsync()
        }
    }
}

private object FirebaseMessagingJvmState {
    @Volatile
    private var registrationToken: String? = null
    @Volatile
    private var autoInitEnabled: Boolean = true

    fun setRegistrationToken(token: String?) {
        registrationToken = token
    }

    fun requireRegistrationToken(): String {
        return registrationToken?.takeIf { it.isNotBlank() }
            ?: error(
                "JVM Firebase Messaging registration token is not set. " +
                    "Use Firebase.setMessagingRegistrationToken(token)."
            )
    }

    fun clearRegistrationToken() {
        registrationToken = null
    }

    fun isAutoInitEnabled(): Boolean = autoInitEnabled

    fun setAutoInitEnabled(enabled: Boolean) {
        autoInitEnabled = enabled
    }
}

/**
 * Updates JVM-side registration token used by message operations.
 */
fun Firebase.setMessagingRegistrationToken(token: String?) {
    FirebaseMessagingJvmState.setRegistrationToken(token)
}

data class FirebaseJvmFcmNotification(
    val title: String? = null,
    val body: String? = null,
)

data class FirebaseJvmFcmMessage(
    val token: String? = null,
    val topic: String? = null,
    val condition: String? = null,
    val data: Map<String, String> = emptyMap(),
    val notification: FirebaseJvmFcmNotification? = null,
)

data class FirebaseJvmFcmSendBatchResult(
    val successCount: Int,
    val failureCount: Int,
    val messageIds: List<String>,
    val failures: List<String>,
)

fun Firebase.sendMessage(message: FirebaseJvmFcmMessage, dryRun: Boolean = false): String {
    val messaging = AdminFirebaseMessaging.getInstance(Firebase.app.adminApp())
    return runCatching { messaging.send(message.toAdminMessage(), dryRun) }
        .getOrElse { throw IllegalStateException("Failed to send FCM message.", it) }
}

fun Firebase.sendMessages(
    messages: List<FirebaseJvmFcmMessage>,
    dryRun: Boolean = false,
): FirebaseJvmFcmSendBatchResult {
    require(messages.isNotEmpty()) { "messages must not be empty." }

    val messaging = AdminFirebaseMessaging.getInstance(Firebase.app.adminApp())
    val messageIds = mutableListOf<String>()
    val failures = mutableListOf<String>()

    messages.forEachIndexed { index, message ->
        runCatching { messaging.send(message.toAdminMessage(), dryRun) }
            .onSuccess(messageIds::add)
            .onFailure { failures += "[$index] ${it.message ?: it::class.simpleName.orEmpty()}" }
    }

    return FirebaseJvmFcmSendBatchResult(
        successCount = messageIds.size,
        failureCount = failures.size,
        messageIds = messageIds,
        failures = failures,
    )
}

fun Firebase.publishMessage(
    topicId: String,
    dataUtf8: String,
    attributes: Map<String, String> = emptyMap(),
    config: FirebaseJvmPubSubPublishConfig = FirebaseJvmPubSubPublishConfig(),
): String {
    require(topicId.isNotBlank()) { "topicId must not be blank." }

    val app = Firebase.app
    val projectId = app.options.projectId?.takeIf { it.isNotBlank() }
        ?: error(
            "JVM Pub/Sub publisher requires projectId in FirebaseOptions. " +
                "Set it when calling Firebase.initializeApp(context, options)."
        )

    val publisherBuilder = Publisher.newBuilder(TopicName.of(projectId, topicId))
        .setCredentialsProvider(FixedCredentialsProvider.create(app.options.toGoogleCredentials()))

    config.endpoint
        ?.takeIf { it.isNotBlank() }
        ?.let(publisherBuilder::setEndpoint)

    val publisher = publisherBuilder.build()
    try {
        val message = PubsubMessage.newBuilder()
            .setData(ByteString.copyFromUtf8(dataUtf8))
            .putAllAttributes(attributes)
            .build()

        return publisher.publish(message).get()
    } finally {
        publisher.shutdown()
        publisher.awaitTermination(30, TimeUnit.SECONDS)
    }
}

fun Firebase.subscribeMessages(
    subscriptionId: String,
    config: FirebaseJvmPubSubSubscriberConfig = FirebaseJvmPubSubSubscriberConfig(),
    onMessage: (FirebaseJvmPubSubMessage) -> FirebaseJvmAckDecision,
): FirebaseJvmPubSubSubscription {
    require(subscriptionId.isNotBlank()) { "subscriptionId must not be blank." }
    require(config.parallelPullCount > 0) { "parallelPullCount must be greater than zero." }

    val app = Firebase.app
    val projectId = app.options.projectId?.takeIf { it.isNotBlank() }
        ?: error(
            "JVM Pub/Sub subscriber requires projectId in FirebaseOptions. " +
                "Set it when calling Firebase.initializeApp(context, options)."
        )

    val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)
    val receiver = MessageReceiver { message, consumer ->
        handlePubSubMessage(message, consumer, onMessage)
    }

    val builder = Subscriber.newBuilder(subscriptionName, receiver)
        .setParallelPullCount(config.parallelPullCount)
        .setCredentialsProvider(FixedCredentialsProvider.create(app.options.toGoogleCredentials()))

    config.endpoint
        ?.takeIf { it.isNotBlank() }
        ?.let(builder::setEndpoint)

    val subscriber = builder.build()
    subscriber.startAsync().awaitRunning()
    return FirebaseJvmPubSubSubscription(subscriber)
}

private fun handlePubSubMessage(
    message: PubsubMessage,
    consumer: AckReplyConsumer,
    onMessage: (FirebaseJvmPubSubMessage) -> FirebaseJvmAckDecision,
) {
    val decision = runCatching { onMessage(message.toJvmMessage()) }
        .getOrElse { FirebaseJvmAckDecision.NACK }

    when (decision) {
        FirebaseJvmAckDecision.ACK -> consumer.ack()
        FirebaseJvmAckDecision.NACK -> consumer.nack()
    }
}

private fun PubsubMessage.toJvmMessage(): FirebaseJvmPubSubMessage {
    return FirebaseJvmPubSubMessage(
        messageId = messageId,
        dataUtf8 = data.toStringUtf8(),
        attributes = attributesMap,
        orderingKey = orderingKey,
        publishTime = publishTime?.let { Instant.ofEpochSecond(it.seconds, it.nanos.toLong()) },
    )
}

private fun FirebaseOptions.toGoogleCredentials(): GoogleCredentials {
    return serviceAccountPath
        ?.takeIf { it.isNotBlank() }
        ?.let { path ->
            FileInputStream(path).use { stream -> GoogleCredentials.fromStream(stream) }
        }
        ?: GoogleCredentials.getApplicationDefault()
}

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging()

actual class FirebaseMessaging internal constructor() {
    actual var autoInitEnabled: Boolean
        get() = FirebaseMessagingJvmState.isAutoInitEnabled()
        set(value) {
            FirebaseMessagingJvmState.setAutoInitEnabled(value)
        }

    actual fun subscribeToTopic(topic: String) {
        require(topic.isNotBlank()) { "topic must not be blank." }

        val token = FirebaseMessagingJvmState.requireRegistrationToken()
        val messaging = AdminFirebaseMessaging.getInstance(Firebase.app.adminApp())
        runCatching { messaging.subscribeToTopic(listOf(token), topic) }
            .getOrElse {
                throw IllegalStateException("Failed to subscribe token to topic '$topic'.", it)
            }
    }

    actual fun unsubscribeFromTopic(topic: String) {
        require(topic.isNotBlank()) { "topic must not be blank." }

        val token = FirebaseMessagingJvmState.requireRegistrationToken()
        val messaging = AdminFirebaseMessaging.getInstance(Firebase.app.adminApp())
        runCatching { messaging.unsubscribeFromTopic(listOf(token), topic) }
            .getOrElse {
                throw IllegalStateException("Failed to unsubscribe token from topic '$topic'.", it)
            }
    }

    actual suspend fun getToken(): String {
        return FirebaseMessagingJvmState.requireRegistrationToken()
    }

    actual suspend fun deleteToken() {
        FirebaseMessagingJvmState.clearRegistrationToken()
    }
}

private fun FirebaseJvmFcmMessage.toAdminMessage(): AdminMessage {
    val targetCount = listOf(token, topic, condition).count { !it.isNullOrBlank() }
    require(targetCount == 1) { "Exactly one of token, topic, or condition must be provided." }

    val builder = AdminMessage.builder()

    token
        ?.takeIf { it.isNotBlank() }
        ?.let(builder::setToken)

    topic
        ?.takeIf { it.isNotBlank() }
        ?.let(builder::setTopic)

    condition
        ?.takeIf { it.isNotBlank() }
        ?.let(builder::setCondition)

    data.forEach { (key, value) ->
        builder.putData(key, value)
    }

    notification?.let { payload ->
        val notificationBuilder = AdminNotification.builder()
        payload.title
            ?.takeIf { it.isNotBlank() }
            ?.let(notificationBuilder::setTitle)
        payload.body
            ?.takeIf { it.isNotBlank() }
            ?.let(notificationBuilder::setBody)
        builder.setNotification(notificationBuilder.build())
    }

    return builder.build()
}
