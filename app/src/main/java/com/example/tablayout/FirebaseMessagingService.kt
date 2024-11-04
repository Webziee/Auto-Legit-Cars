package com.example.tablayout

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Log the incoming message for debugging
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            remoteMessage.notification?.let {
                val builder = NotificationCompat.Builder(this, "car_listing_channel")
                    .setSmallIcon(R.drawable.success)
                    .setContentTitle(it.title ?: "Car Listed Successfully")
                    .setContentText(it.body ?: "Your car has been listed successfully.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                with(NotificationManagerCompat.from(this)) {
                    notify(NOTIFICATION_ID, builder.build())
                }
            }
        } else {
            Log.d("FCM", "Notification permission not granted; notification not shown.")
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
