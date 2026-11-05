package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_tasks")
data class ScheduledTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long? = null,
    val lessonId: Long? = null,
    val title: String,
    val category: String,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val startTime: String,
    val endTime: String = "",
    val durationMinutes: Int,
    val reminderTimeEpochMillis: Long = 0L,
    val isCompleted: Boolean = false,
    val notificationScheduled: Boolean = false,
    val isSkipped: Boolean = false,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val notes: String = ""
)
