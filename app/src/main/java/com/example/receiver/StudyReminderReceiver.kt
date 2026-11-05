package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.notification.StudyAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getLongExtra(StudyAlarmScheduler.EXTRA_TASK_ID, -1L)
        val planName = intent.getStringExtra(StudyAlarmScheduler.EXTRA_PLAN_NAME) ?: "Study Plan"
        val lessonTitle = intent.getStringExtra(StudyAlarmScheduler.EXTRA_LESSON_TITLE) ?: "Scheduled Lesson"
        val notificationId = if (taskId != -1L) (taskId % Int.MAX_VALUE).toInt() else 1001

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (action) {
            StudyAlarmScheduler.ACTION_TRIGGER -> {
                Log.d("StudyReminderReceiver", "Triggering notification for task $taskId: $lessonTitle")

                // 1. Action: Start Learning (opens app)
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    this.action = StudyAlarmScheduler.ACTION_START_LEARNING
                    putExtra(StudyAlarmScheduler.EXTRA_TASK_ID, taskId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 2. Action: Snooze 10 Minutes
                val snoozeIntent = Intent(context, StudyReminderReceiver::class.java).apply {
                    this.action = StudyAlarmScheduler.ACTION_SNOOZE
                    putExtra(StudyAlarmScheduler.EXTRA_TASK_ID, taskId)
                    putExtra(StudyAlarmScheduler.EXTRA_PLAN_NAME, planName)
                    putExtra(StudyAlarmScheduler.EXTRA_LESSON_TITLE, lessonTitle)
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + 100000,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 3. Action: Mark as Completed
                val completeIntent = Intent(context, StudyReminderReceiver::class.java).apply {
                    this.action = StudyAlarmScheduler.ACTION_MARK_COMPLETED
                    putExtra(StudyAlarmScheduler.EXTRA_TASK_ID, taskId)
                }
                val completePendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + 200000,
                    completeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, StudyAlarmScheduler.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Your learning time is here!")
                    .setContentText("$planName — $lessonTitle")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("$planName\n$lessonTitle\nYour scheduled study window starts in 10 minutes.")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(openAppPendingIntent)
                    .addAction(0, "Start Learning", openAppPendingIntent)
                    .addAction(0, "Snooze 10m", snoozePendingIntent)
                    .addAction(0, "Mark Done", completePendingIntent)
                    .build()

                notificationManager.notify(notificationId, notification)
            }

            StudyAlarmScheduler.ACTION_SNOOZE -> {
                Log.d("StudyReminderReceiver", "Snoozing reminder for task $taskId")
                notificationManager.cancel(notificationId)
                StudyAlarmScheduler.snoozeAlarm(context, taskId, planName, lessonTitle, 10)
            }

            StudyAlarmScheduler.ACTION_MARK_COMPLETED -> {
                Log.d("StudyReminderReceiver", "Marking task $taskId as completed from notification")
                notificationManager.cancel(notificationId)

                if (taskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            // Query and mark task as completed
                            val taskDao = db.taskDao()
                            val profileDao = db.profileDao()
                            // Update task status in database
                            val tasks = db.openHelper.readableDatabase
                            db.query("SELECT * FROM scheduled_tasks WHERE id = $taskId", null).use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val titleIdx = cursor.getColumnIndex("title")
                                    val catIdx = cursor.getColumnIndex("category")
                                    val dateIdx = cursor.getColumnIndex("dateEpochDay")
                                    val timeIdx = cursor.getColumnIndex("startTime")
                                    val durIdx = cursor.getColumnIndex("durationMinutes")
                                    val planIdIdx = cursor.getColumnIndex("planId")

                                    val updatedTask = com.example.data.model.ScheduledTask(
                                        id = taskId,
                                        planId = if (planIdIdx >= 0 && !cursor.isNull(planIdIdx)) cursor.getLong(planIdIdx) else null,
                                        title = if (titleIdx >= 0) cursor.getString(titleIdx) else "",
                                        category = if (catIdx >= 0) cursor.getString(catIdx) else "General",
                                        dateEpochDay = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L,
                                        startTime = if (timeIdx >= 0) cursor.getString(timeIdx) else "07:00 PM",
                                        durationMinutes = if (durIdx >= 0) cursor.getInt(durIdx) else 45,
                                        isCompleted = true
                                    )
                                    taskDao.updateTask(updatedTask)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("StudyReminderReceiver", "Failed to mark task completed", e)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }
}
