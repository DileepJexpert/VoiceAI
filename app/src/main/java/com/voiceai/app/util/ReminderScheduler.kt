package com.voiceai.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val WORK_TAG_PREFIX = "reminder_"
        private const val KEY_REMINDER_ID = "reminder_id"
        private const val KEY_TITLE = "title"
        private const val CHANNEL_ID = "voiceai_reminders"
        private const val CHANNEL_NAME = "Reminders"
    }

    fun scheduleReminder(reminderId: Long, triggerTime: Long, title: String) {
        val delay = triggerTime - System.currentTimeMillis()
        if (delay <= 0) return

        val inputData = Data.Builder()
            .putLong(KEY_REMINDER_ID, reminderId)
            .putString(KEY_TITLE, title)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("$WORK_TAG_PREFIX$reminderId")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun cancelReminder(reminderId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("$WORK_TAG_PREFIX$reminderId")
    }
}

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "voiceai_reminders"
        private const val CHANNEL_NAME = "Reminders"
        private const val KEY_REMINDER_ID = "reminder_id"
        private const val KEY_TITLE = "title"
    }

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1)
        val title = inputData.getString(KEY_TITLE) ?: "Reminder"

        createNotificationChannel()
        showNotification(reminderId, title)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "VoiceAI reminder notifications"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(reminderId: Long, title: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("VoiceAI Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reminderId.toInt(), notification)
    }
}
