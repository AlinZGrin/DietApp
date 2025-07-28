package com.dietapp.data.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            handleNotification(it)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // Send token to app server
        sendRegistrationToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // Handle data message
        when (data["type"]) {
            "goal_reminder" -> {
                // Handle goal reminder
                Log.d(TAG, "Goal reminder received")
            }
            "weight_reminder" -> {
                // Handle weight tracking reminder
                Log.d(TAG, "Weight reminder received")
            }
            "water_reminder" -> {
                // Handle water intake reminder
                Log.d(TAG, "Water reminder received")
            }
        }
    }

    private fun handleNotification(notification: RemoteMessage.Notification) {
        // Handle notification message
        Log.d(TAG, "Notification title: ${notification.title}")
        Log.d(TAG, "Notification body: ${notification.body}")
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement sending token to your app server
        Log.d(TAG, "Sending token to server: $token")
    }

    companion object {
        private const val TAG = "FirebaseMessaging"
    }
}