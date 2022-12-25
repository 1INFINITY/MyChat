package com.example.mychat.data.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

val TAG = "FCM"
class MessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CreatedCreatedCreatedCreatedCreatedCreated")
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message: ${remoteMessage.notification?.body ?: "null notification"}")
    }
}