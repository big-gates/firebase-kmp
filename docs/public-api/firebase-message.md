# firebase-message Public API Coverage

Updated: 2026-02-16  
Coverage (v1, Android/Apple): **6 / 18 = 33.3%**

## Scope
This catalog tracks Cloud Messaging APIs exposed by `firebase-message` for Android and Apple targets.

JVM target is currently not supported in this module.

## Implemented (6)
1. `Firebase.messaging`
2. `FirebaseMessaging.autoInitEnabled`
3. `FirebaseMessaging.subscribeToTopic(topic)`
4. `FirebaseMessaging.unsubscribeFromTopic(topic)`
5. `FirebaseMessaging.getToken()`
6. `FirebaseMessaging.deleteToken()`

## Missing / Planned (12)
1. Platform callback/listener abstraction for foreground/background message delivery.
2. Notification permission state abstraction (platform-specific behavior caveat required).
3. APNs token set/get abstraction (iOS-focused parity).
4. Android notification delegation API.
5. Android direct boot handling API.
6. Delivery metrics export toggles parity APIs.
7. Topic management batch APIs.
8. Token refresh stream/listener abstraction.
9. Message received metadata model (sent time/ttl/priority).
10. Platform-specific channel options model (Android).
11. Platform-specific APNs config model (iOS).
12. Emulator-specific diagnostics API for Messaging.

## Removed JVM APIs (2026-02-16)
1. `Firebase.setMessagingRegistrationToken(token)`
2. `Firebase.subscribeMessages(subscriptionId, config, onMessage)`
3. `Firebase.publishMessage(topicId, dataUtf8, attributes, config)`
4. `Firebase.sendMessage(message, dryRun)`
5. `Firebase.sendMessages(messages, dryRun)`
6. `FirebaseJvmAckDecision`
7. `FirebaseJvmPubSubMessage`
8. `FirebaseJvmPubSubSubscriberConfig`
9. `FirebaseJvmPubSubPublishConfig`
10. `FirebaseJvmPubSubSubscription`
11. `FirebaseJvmFcmNotification`
12. `FirebaseJvmFcmMessage`
13. `FirebaseJvmFcmSendBatchResult`
