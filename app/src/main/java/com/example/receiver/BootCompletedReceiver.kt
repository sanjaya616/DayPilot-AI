package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.notification.StudyAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("BootCompletedReceiver", "Received action: $action, re-scheduling alarms")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val allPlans = db.planDao().getAllPlans().firstOrNull() ?: emptyList()
                val activePlansMap = allPlans.filter { it.status == "ACTIVE" }.associateBy { it.id }

                val allTasks = db.taskDao().getAllTasks().firstOrNull() ?: emptyList()
                val todayEpoch = LocalDate.now().toEpochDay()

                val pendingTasks = allTasks.filter { task ->
                    !task.isCompleted && !task.isSkipped && task.dateEpochDay >= todayEpoch
                }

                Log.d("BootCompletedReceiver", "Found ${pendingTasks.size} pending tasks to re-arm")
                for (task in pendingTasks) {
                    val planName = task.planId?.let { activePlansMap[it]?.name } ?: "Daily Study"
                    StudyAlarmScheduler.scheduleTaskAlarm(context, task, planName)
                }
            } catch (e: Exception) {
                Log.e("BootCompletedReceiver", "Failed to reschedule alarms on boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
