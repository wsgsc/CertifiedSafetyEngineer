package com.xiaogong.csestudy.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.xiaogong.csestudy.R
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "daily_reminder"
        private const val NOTIFICATION_ID = 1001
        private const val WORK_NAME = "daily_reminder"
        const val EXTRA_HOUR = "reminder_hour"
        const val EXTRA_MINUTE = "reminder_minute"
        const val EXTRA_SLOT = "reminder_slot"

        private val MESSAGES = listOf(
            "每天进步一点点，坚持就是胜利！",
            "不积跬步，无以至千里。",
            "今天的努力，是明天的底气。",
            "刷题一分钟，考场多一分。",
            "学习很苦，坚持很酷。",
            "宝剑锋从磨砺出，梅花香自苦寒来。",
            "别偷懒哦，对手正在刷题呢！",
            "温故而知新，可以为师矣。",
            "业精于勤，荒于嬉。",
            "今天不学习，明天变垃圾。"
        )

        private fun workName(slot: Int) = "${WORK_NAME}_slot_$slot"

        fun schedule(context: Context, hour: Int, minute: Int, slot: Int) {
            val delayMs = calculateDelayToNext(hour, minute)
            val inputData = androidx.work.Data.Builder()
                .putInt(EXTRA_HOUR, hour)
                .putInt(EXTRA_MINUTE, minute)
                .putInt(EXTRA_SLOT, slot)
                .build()

            val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                workName(slot),
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(workName(1))
            WorkManager.getInstance(context).cancelUniqueWork(workName(2))
        }

        fun scheduleBoth(context: Context, h1: Int, m1: Int, h2: Int, m2: Int) {
            schedule(context, h1, m1, 1)
            schedule(context, h2, m2, 2)
        }

        private fun calculateDelayToNext(hour: Int, minute: Int): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }

    override suspend fun doWork(): Result {
        showNotification()

        // Schedule the next occurrence for tomorrow
        val hour = inputData.getInt(EXTRA_HOUR, 9)
        val minute = inputData.getInt(EXTRA_MINUTE, 0)
        val slot = inputData.getInt(EXTRA_SLOT, 1)
        schedule(applicationContext, hour, minute, slot)

        return Result.success()
    }

    private fun showNotification() {
        createChannel()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("别忘了刷题哦")
            .setContentText(MESSAGES.random())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "每日提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "每日学习提醒通知" }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
