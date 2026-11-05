package com.example.util

import com.example.data.model.AiMemory
import com.example.data.model.AiProductivityInsight
import com.example.data.model.LearningPlan
import com.example.data.model.ProductivityStats
import com.example.data.model.ScheduledTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object AnalyticsCalculator {

    fun calculateStats(
        tasks: List<ScheduledTask>,
        plans: List<LearningPlan>,
        today: LocalDate = LocalDate.now()
    ): ProductivityStats {
        val validTasks = tasks.filter { !it.isSkipped }
        val todayEpoch = today.toEpochDay()

        val todayTasks = validTasks.filter { it.dateEpochDay == todayEpoch }
        val dailyRate = if (todayTasks.isEmpty()) 0f
        else {
            val completed = todayTasks.count { it.isCompleted }
            (completed.toFloat() / todayTasks.size.toFloat()) * 100f
        }

        val weekStartEpoch = todayEpoch - 6
        val weeklyTasks = validTasks.filter { it.dateEpochDay in weekStartEpoch..todayEpoch }
        val weeklyRate = if (weeklyTasks.isEmpty()) 0f
        else {
            val completed = weeklyTasks.count { it.isCompleted }
            (completed.toFloat() / weeklyTasks.size.toFloat()) * 100f
        }

        val completedTasks = validTasks.filter { it.isCompleted }
        val totalMinutes = completedTasks.sumOf { it.durationMinutes.toLong() }
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        val formattedHours = String.format(Locale.getDefault(), "%dh %02dm", hours, mins)

        val missedCount = validTasks.count { it.dateEpochDay < todayEpoch && !it.isCompleted }

        // Active days: distinct epoch days with at least 1 completed task
        val activeDays = completedTasks.map { it.dateEpochDay }.toSet()

        // Current Streak
        val currentStreak = calculateCurrentStreak(activeDays, todayEpoch)

        // Longest Streak
        val longestStreak = calculateLongestStreak(activeDays)

        // Overall Plan Progress
        val activePlans = plans.filter { it.status == "ACTIVE" }
        val overallProgress = if (plans.isEmpty()) 0f
        else {
            val plansToAverage = if (activePlans.isNotEmpty()) activePlans else plans
            plansToAverage.map { it.progressPercent }.average().toFloat()
        }

        return ProductivityStats(
            dailyCompletionRate = dailyRate,
            weeklyCompletionRate = weeklyRate,
            totalLearningMinutes = totalMinutes,
            totalLearningHoursFormatted = formattedHours,
            completedLessonsCount = completedTasks.size,
            missedLessonsCount = missedCount,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            overallPlanProgress = overallProgress,
            activePlansCount = activePlans.size,
            totalScheduledTasks = validTasks.size
        )
    }

    fun calculateCurrentStreak(activeDays: Set<Long>, todayEpoch: Long): Int {
        if (activeDays.isEmpty()) return 0

        var streak = 0
        var checkDay = if (activeDays.contains(todayEpoch)) {
            todayEpoch
        } else if (activeDays.contains(todayEpoch - 1)) {
            todayEpoch - 1
        } else {
            return 0
        }

        while (activeDays.contains(checkDay)) {
            streak++
            checkDay--
        }

        return streak
    }

    fun calculateLongestStreak(activeDays: Set<Long>): Int {
        if (activeDays.isEmpty()) return 0

        val sortedDays = activeDays.sorted()
        var maxStreak = 1
        var currentRun = 1

        for (i in 1 until sortedDays.size) {
            if (sortedDays[i] == sortedDays[i - 1] + 1) {
                currentRun++
                if (currentRun > maxStreak) {
                    maxStreak = currentRun
                }
            } else if (sortedDays[i] != sortedDays[i - 1]) {
                currentRun = 1
            }
        }

        return maxStreak
    }

    fun generateAiInsight(
        stats: ProductivityStats,
        plans: List<LearningPlan>,
        missedTasks: List<ScheduledTask>,
        memory: AiMemory?,
        today: LocalDate = LocalDate.now()
    ): AiProductivityInsight {
        val weeklySummary = if (stats.completedLessonsCount == 0) {
            "Welcome to DayPilot AI! Complete your first lesson to kickstart your weekly consistency streak."
        } else {
            "This week you completed ${stats.completedLessonsCount} sessions totalling ${stats.totalLearningHoursFormatted} of focused study (${stats.weeklyCompletionRate.toInt()}% weekly completion rate)."
        }

        val activePlan = plans.firstOrNull { it.status == "ACTIVE" } ?: plans.firstOrNull()
        val learningRecommendation = if (activePlan != null) {
            val prefTime = memory?.preferredStudyTime ?: "7:00 PM"
            "Stay consistent with ${activePlan.name}. Research shows studying at $prefTime daily boosts memory retention by up to 35%."
        } else {
            "Ask the AI Assistant to generate a structured roadmap tailored to your preferred learning pace and daily schedule."
        }

        val workloadAdjustment = if (stats.missedLessonsCount > 0) {
            "You have ${stats.missedLessonsCount} missed session(s). Consider splitting sessions into 30m blocks or using the 1-tap reschedule banner on your dashboard."
        } else if (stats.weeklyCompletionRate >= 80f && stats.completedLessonsCount >= 3) {
            "Your consistency is outstanding (${stats.weeklyCompletionRate.toInt()}%). You can comfortably add 15 minutes to your sessions to accelerate completion."
        } else {
            "Optimal study pace: 45–60 minutes per session maximizes focus while avoiding cognitive fatigue."
        }

        val velocity = if (stats.currentStreak > 0) {
            (stats.completedLessonsCount.toFloat() / stats.currentStreak.toFloat()).coerceIn(0.5f, 3.0f)
        } else {
            1.0f
        }

        val goalPrediction = if (activePlan != null && activePlan.totalLessons > 0) {
            val remainingLessons = (activePlan.totalLessons - activePlan.completedLessons).coerceAtLeast(0)
            if (remainingLessons == 0) {
                "Goal achieved! You have completed all lessons for ${activePlan.name}."
            } else {
                val daysNeeded = Math.ceil((remainingLessons / velocity).toDouble()).toLong().coerceAtLeast(1)
                val predictedDate = today.plusDays(daysNeeded)
                val dateFormatted = predictedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
                "At your pace of ${String.format(Locale.getDefault(), "%.1f", velocity)} lessons/day, you'll complete ${activePlan.name} by $dateFormatted."
            }
        } else {
            "Set an active learning plan to receive AI goal completion predictions."
        }

        val missedTip = if (missedTasks.isNotEmpty()) {
            "Tip: Rather than skipping missed lessons, tap 'Move Remaining Forward' to keep your sequential curriculum intact."
        } else null

        val streakStatus = when {
            stats.currentStreak >= 5 -> "${stats.currentStreak} Day Streak 🔥 Unstoppable momentum!"
            stats.currentStreak in 2..4 -> "${stats.currentStreak} Day Streak ⚡ Great consistency!"
            stats.currentStreak == 1 -> "1 Day Streak 🎯 Keep it alive today!"
            else -> "Start your study streak today! 🚀"
        }

        return AiProductivityInsight(
            weeklySummary = weeklySummary,
            learningRecommendation = learningRecommendation,
            workloadAdjustment = workloadAdjustment,
            goalPrediction = goalPrediction,
            missedLessonTip = missedTip,
            streakStatus = streakStatus,
            paceVelocityPerDay = velocity
        )
    }
}
