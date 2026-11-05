package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_plans")
data class LearningPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val category: String, // e.g. "Tech & Software", "Data Analysis", "Fitness", "Design"
    val startDate: Long,
    val endDate: Long,
    val dailyMinutes: Int,
    val progressPercent: Float = 0f, // 0..100
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val colorHex: String = "#00E5FF",
    val completedLessons: Int = 0,
    val totalLessons: Int = 30
)
