package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_memory")
data class AiMemory(
    @PrimaryKey
    val id: Int = 1,
    val preferredStudyTime: String = "07:00 PM",
    val preferredLanguages: String = "English, Sinhala",
    val skillLevels: String = "Beginner in Photo Editing, Intermediate in Java",
    val difficulties: String = "Needs extra practice on non-destructive layers and coroutine debugging",
    val notes: String = "Prefers hands-on exercises with downloadable starter assets",
    val futureGoals: String = "Full-stack mobile app development, professional video color grading",
    val lastUpdated: Long = System.currentTimeMillis()
)
