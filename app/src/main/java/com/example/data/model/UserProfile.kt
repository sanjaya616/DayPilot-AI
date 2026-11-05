package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val displayName: String = "Alex Rivera",
    val title: String = "Product Designer & Learner",
    val studyTargetHoursPerWeek: Int = 15,
    val preferredStudyWindow: String = "Evening (7:00 PM - 10:00 PM)",
    val remindersEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val streakDays: Int = 5,
    val totalCompletedTasks: Int = 12,
    val overallScore: Int = 200
)
