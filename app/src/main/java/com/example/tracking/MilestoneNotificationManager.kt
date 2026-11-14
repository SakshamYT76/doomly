package com.example.tracking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.BrainStatus

/**
 * Sends one-time-per-day local system notifications when combined daily estimated scrolls
 * cross the 200, 300, and 500 scroll milestones.
 */
class MilestoneNotificationManager(private val context: Context) {

    init {
        ensureNotificationChannel()
    }

    private fun ensureNotificationChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Scroll Milestone Warnings",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "One-time daily awareness notifications at 200, 300, and 500 estimated scrolls."
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendMilestoneNotification(threshold: Int, currentScrolls: Int) {
        if (!hasNotificationPermission()) return

        val brainStatus = BrainStatus.fromScrollCount(currentScrolls)
        val title = when (threshold) {
            200 -> "DOOMLY: 200 Estimated Scrolls Today"
            300 -> "DOOMLY: 300 Estimated Scrolls Today"
            500 -> "DOOMLY: 500 Estimated Scrolls Reached"
            else -> "DOOMLY: $threshold Estimated Scrolls Today"
        }

        val message = when (threshold) {
            200 -> "Brain Status is now '${brainStatus.label}'. You've crossed 200 estimated short-form scrolls today—consider taking a quick break."
            300 -> "Brain Status is now '${brainStatus.label}'. That's 300+ estimated scrolls today. Time to rest your eyes!"
            500 -> "You've reached 500 estimated scrolls today (${brainStatus.label}). Step away from the feed and recharge."
            else -> "You've reached $currentScrolls estimated scrolls today (${brainStatus.label})."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            threshold,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$message\n(Playful awareness indicator, not a medical assessment.)"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + threshold, notification)
        } catch (_: SecurityException) {
            // Permission was revoked at runtime; fail gracefully.
        }
    }

    companion object {
        const val CHANNEL_ID = "doomly_scroll_milestones"
        private const val NOTIFICATION_ID_BASE = 4000
    }
}
