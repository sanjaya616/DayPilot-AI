package com.example.data.model

import java.time.LocalDate

data class ProductivityStats(
    val dailyCompletionRate: Float = 0f,
    val weeklyCompletionRate: Float = 0f,
    val totalLearningMinutes: Long = 0L,
    val totalLearningHoursFormatted: String = "0h 00m",
    val completedLessonsCount: Int = 0,
    val missedLessonsCount: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val overallPlanProgress: Float = 0f,
    val activePlansCount: Int = 0,
    val totalScheduledTasks: Int = 0
)

data class AiProductivityInsight(
    val weeklySummary: String = "",
    val learningRecommendation: String = "",
    val workloadAdjustment: String = "",
    val goalPrediction: String = "",
    val missedLessonTip: String? = null,
    val streakStatus: String = "",
    val paceVelocityPerDay: Float = 0f
)
