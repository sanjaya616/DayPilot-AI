package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_lessons",
    foreignKeys = [
        ForeignKey(
            entity = LearningPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"])]
)
data class DailyLesson(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,
    val dayNumber: Int,
    val title: String,
    val durationMinutes: Int,
    val objectives: String = "",
    val steps: String = "",
    val resources: String = "",
    val assignment: String = "",
    val completionCriteria: String = "",
    val isCompleted: Boolean = false
)
