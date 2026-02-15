package com.biggates.firebase.message

import com.biggates.firebase.common.Firebase
import com.biggates.firebase.common.FirebaseApp

/**
 * 기본 [FirebaseApp]의 [FirebaseMessaging] 인스턴스를 반환합니다.
 */
expect val Firebase.messaging: FirebaseMessaging

/**
 * 최상위 [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) 싱글톤으로
 * topic 구독 및 업스트림 메시지 전송 기능을 제공합니다.
 */
expect class FirebaseMessaging {
    /**
     * Whether Firebase Messaging auto-init is enabled for this app.
     *
     * On JVM this controls local wrapper behavior only.
     */
    var autoInitEnabled: Boolean

    /**
     * topic을 구독합니다.
     *
     * @param topic 구독할 topic 이름
     */
    fun subscribeToTopic(topic: String)

    /**
     * topic의 구독을 취소합니다.
     *
     * @param topic 구독 취소할 topic 이름
     */
    fun unsubscribeFromTopic(topic: String)

    /**
     * 클라이언트의 FCM 토큰을 가져옵니다.
     *
     * @return FCM 토큰 문자열
     */
    suspend fun getToken(): String

    /**
     * 클라이언트의 FCM 토큰을 삭제합니다.
     */
    suspend fun deleteToken()
}
