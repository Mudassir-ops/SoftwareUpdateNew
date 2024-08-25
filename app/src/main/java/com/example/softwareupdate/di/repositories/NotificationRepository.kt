package com.example.softwareupdate.di.repositories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationBuilder: NotificationCompat.Builder,
    private val notificationManager: NotificationManagerCompat,
    private val context: Context
) {
    fun buildNotification(): NotificationCompat.Builder {
        return notificationBuilder
    }

    fun updateNotification(updateCount: Int) {
        val notification = buildNotificationWithCount(updateCount)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(1, notification.build())
    }

    private fun buildNotificationWithCount(updateCount: Int): NotificationCompat.Builder {
        val contentText = "You have $updateCount updates available"
        return notificationBuilder.setContentText(contentText).setSound(null)
    }
}