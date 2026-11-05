package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.ScheduledTask
import com.example.receiver.StudyReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object StudyAlarmScheduler {

    private const val TAG = "StudyAlarmScheduler"
    const val CHANNEL_ID = "daypilot_study_reminders"
    const val CHANNEL_NAME = "Study & Learning Reminders"

    const val ACTION_TRIGGER = "com.example.daypilot.ACTION_TRIGGER_REMINDER"
    const val ACTION_START_LEARNING = "com.example.daypilot.ACTION_START_LEARNING"
    const val ACTION_SNOOZE = "com.example.daypilot.ACTION_SNOOZE"
    const val ACTION_MARK_COMPLETED = "com.example.daypilot.ACTION_MARK_COMPLETED"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_PLAN_NAME = "extra_plan_name"
    const val EXTRA_LESSON_TITLE = "extra_lesson_title"
    const val EXTRA_DAY_NUMBER = "extra_day_number"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily study sessions and roadmap lesson reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 200, 250)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canScheduleExact(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    fun calculateTriggerMillis(
        dateEpochDay: Long,
        startTimeStr: String,
        advanceMinutes: Int = 10
    ): Long {
        val date = LocalDate.ofEpochDay(dateEpochDay)
        val time = parseTime(startTimeStr)
        val sessionDateTime = LocalDateTime.of(date, time)
        val triggerDateTime = sessionDateTime.minusMinutes(advanceMinutes.toLong())

        val zoneId = ZoneId.systemDefault()
        return triggerDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun calculateEndTime(startTimeStr: String, durationMinutes: Int): String {
        val startLocalTime = parseTime(startTimeStr)
        val endLocalTime = startLocalTime.plusMinutes(durationMinutes.toLong())
        return endLocalTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
    }

    private fun parseTime(timeStr: String): LocalTime {
        val cleaned = timeStr.trim().uppercase()
        val patterns = listOf("hh:mm a", "h:mm a", "HH:mm", "H:mm")
        for (pattern in patterns) {
            try {
                return LocalTime.parse(cleaned, DateTimeFormatter.ofPattern(pattern, Locale.US))
            } catch (_: Exception) {
            }
        }
        return LocalTime.of(19, 0) // Default 7:00 PM
    }

    fun scheduleTaskAlarm(
        context: Context,
        task: ScheduledTask,
        planName: String,
        advanceMinutes: Int = 10
    ): Boolean {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false

        val triggerMillis = if (task.reminderTimeEpochMillis > 0) {
            task.reminderTimeEpochMillis
        } else {
            calculateTriggerMillis(task.dateEpochDay, task.startTime, advanceMinutes)
        }

        // If the calculated reminder is already in the past, do not trigger stale alarm
        if (triggerMillis < System.currentTimeMillis()) {
            Log.d(TAG, "Task ${task.id} reminder time is in the past, skipping alarm.")
            return false
        }

        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_PLAN_NAME, planName)
            putExtra(EXTRA_LESSON_TITLE, task.title)
        }

        val requestCode = (task.id % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Alarm armed for task ${task.id} at $triggerMillis")
            return true
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission denied, falling back", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for task ${task.id}", e)
            return false
        }
    }

    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER
        }
        val requestCode = (taskId % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Alarm cancelled for task $taskId")
        }
    }

    fun snoozeAlarm(
        context: Context,
        taskId: Long,
        planName: String,
        lessonTitle: String,
        snoozeMinutes: Int = 10
    ) {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_PLAN_NAME, planName)
            putExtra(EXTRA_LESSON_TITLE, lessonTitle)
        }

        val requestCode = (taskId % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Snoozed alarm armed for $snoozeMinutes minutes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to snooze alarm", e)
        }
    }
}
