# firebase-message Public API Coverage

Updated: 2026-02-15  
Coverage (v1): **19 / 36 = 52.8%**

## Scope
This catalog tracks Cloud Messaging APIs exposed by `firebase-message`, including JVM server-side extensions.

## Implemented (19)
1. `Firebase.messaging`
2. `FirebaseMessaging.autoInitEnabled`
3. `FirebaseMessaging.subscribeToTopic(topic)`
4. `FirebaseMessaging.unsubscribeFromTopic(topic)`
5. `FirebaseMessaging.getToken()`
6. `FirebaseMessaging.deleteToken()`
7. `Firebase.setMessagingRegistrationToken(token)` (JVM-only)
8. `Firebase.subscribeMessages(subscriptionId, config, onMessage)` (JVM-only)
9. `Firebase.publishMessage(topicId, dataUtf8, attributes, config)` (JVM-only)
10. `Firebase.sendMessage(message, dryRun)` (JVM-only)
11. `Firebase.sendMessages(messages, dryRun)` (JVM-only)
12. `FirebaseJvmAckDecision`
13. `FirebaseJvmPubSubMessage`
14. `FirebaseJvmPubSubSubscriberConfig`
15. `FirebaseJvmPubSubPublishConfig`
16. `FirebaseJvmPubSubSubscription`
17. `FirebaseJvmFcmNotification`
18. `FirebaseJvmFcmMessage`
19. `FirebaseJvmFcmSendBatchResult`

## Missing / Planned (17)
1. Platform callback/listener abstraction for foreground/background message delivery.
2. Notification permission state abstraction (platform-specific behavior caveat required).
3. APNs token set/get abstraction (iOS-focused parity).
4. Android notification delegation API.
5. Android direct boot handling API.
6. Delivery metrics export toggles parity APIs.
7. Topic management batch APIs.
8. Device-group send semantics wrapper (server side).
9. Condition/topic/token validation helper APIs.
10. Message priority/ttl/collapse key shared model.
11. Message analytics label API.
12. Platform-specific channel options model (Android).
13. Platform-specific APNs config model (iOS/JVM send).
14. Platform-specific WebPush config model (JVM send).
15. Multicast send response detail model.
16. Canonical token/invalid token reconciliation helper APIs.
17. Emulator-specific diagnostics API for Messaging.
