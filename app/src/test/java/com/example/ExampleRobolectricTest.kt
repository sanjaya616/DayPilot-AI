package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.GeneratedLesson
import com.example.data.ai.GeneratedRoadmap
import com.example.data.model.AiMemory
import com.example.data.model.DailyLesson
import com.example.data.model.LearningPlan
import com.example.data.model.ScheduledTask
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DayPilot AI", appName)
  }

  @Test
  fun `task model properties and toggle`() {
    val task = ScheduledTask(
      id = 1,
      title = "Study Java 45m",
      category = "Tech & Software",
      dateEpochDay = 20000,
      startTime = "07:00 PM",
      durationMinutes = 45,
      isCompleted = false
    )
    assertFalse(task.isCompleted)
    val completedTask = task.copy(isCompleted = true)
    assertTrue(completedTask.isCompleted)
  }

  @Test
  fun `learning plan progress and status`() {
    val plan = LearningPlan(
      id = 1,
      name = "Designing Seamless UX",
      description = "UI patterns",
      category = "Tech & Software",
      startDate = 1000L,
      endDate = 2000L,
      dailyMinutes = 45,
      progressPercent = 65f,
      status = "ACTIVE"
    )
    assertEquals("ACTIVE", plan.status)
    val paused = plan.copy(status = "PAUSED")
    assertEquals("PAUSED", paused.status)
  }

  @Test
  fun `daily lesson model and steps`() {
    val lesson = DailyLesson(
      id = 1,
      planId = 1,
      dayNumber = 1,
      title = "Photo Editing Fundamentals",
      durationMinutes = 60,
      objectives = "Layers; Exposure adjustment",
      steps = "Step 1: Open asset\nStep 2: Add curves layer",
      resources = "https://www.photopea.com/learn",
      assignment = "Edit portrait",
      completionCriteria = "Export PSD"
    )
    assertEquals(1, lesson.dayNumber)
    assertEquals(60, lesson.durationMinutes)
    assertFalse(lesson.isCompleted)
  }

  @Test
  fun `ai memory defaults and update`() {
    val memory = AiMemory()
    assertEquals("07:00 PM", memory.preferredStudyTime)
    assertTrue(memory.preferredLanguages.contains("Sinhala"))
    val updated = memory.copy(preferredStudyTime = "08:00 PM")
    assertEquals("08:00 PM", updated.preferredStudyTime)
  }

  @Test
  fun `roadmap generation data structure`() {
    val roadmap = GeneratedRoadmap(
      title = "Photo Editing Mastery",
      description = "30-day curriculum",
      category = "Design",
      totalDays = 30,
      dailyDurationMinutes = 60,
      targetSkillLevel = "Mastery",
      lessons = listOf(
        GeneratedLesson(
          dayNumber = 1,
          title = "Intro",
          durationMinutes = 60,
          objectives = listOf("Basics"),
          steps = listOf("Start"),
          resources = listOf("https://photopea.com"),
          assignment = "Do task",
          completionCriteria = "Done"
        )
      )
    )
    assertEquals(30, roadmap.totalDays)
    assertEquals(1, roadmap.lessons.size)
  }

  @Test
  fun `phase 3 scheduled task fields and properties`() {
    val task = ScheduledTask(
      id = 101L,
      planId = 12L,
      lessonId = 5L,
      title = "Day 05: Layer Masks",
      category = "Design",
      dateEpochDay = 20000L,
      startTime = "07:00 PM",
      endTime = "08:00 PM",
      durationMinutes = 60,
      reminderTimeEpochMillis = 1720000000000L,
      notificationScheduled = true,
      isCompleted = false,
      isSkipped = false
    )
    assertEquals(12L, task.planId)
    assertEquals(5L, task.lessonId)
    assertEquals("08:00 PM", task.endTime)
    assertTrue(task.notificationScheduled)
    assertFalse(task.isSkipped)

    val skipped = task.copy(isSkipped = true)
    assertTrue(skipped.isSkipped)
  }

  @Test
  fun `study alarm scheduler end time calculation`() {
    val calculatedEnd = com.example.notification.StudyAlarmScheduler.calculateEndTime("07:00 PM", 60)
    assertEquals("08:00 PM", calculatedEnd)

    val calculatedHalfHour = com.example.notification.StudyAlarmScheduler.calculateEndTime("09:30 AM", 45)
    assertEquals("10:15 AM", calculatedHalfHour)
  }

  @Test
  fun `study alarm scheduler trigger calculation advances 10 minutes`() {
    val epochDay = java.time.LocalDate.of(2026, 11, 10).toEpochDay()
    val trigger10MinBefore = com.example.notification.StudyAlarmScheduler.calculateTriggerMillis(epochDay, "07:00 PM", advanceMinutes = 10)
    val triggerExactTime = com.example.notification.StudyAlarmScheduler.calculateTriggerMillis(epochDay, "07:00 PM", advanceMinutes = 0)

    val differenceMillis = triggerExactTime - trigger10MinBefore
    // 10 minutes in milliseconds = 600,000 ms
    assertEquals(600_000L, differenceMillis)
  }

  @Test
  fun `study alarm scheduler creates notification channel`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.notification.StudyAlarmScheduler.createNotificationChannel(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val channel = notificationManager.getNotificationChannel(com.example.notification.StudyAlarmScheduler.CHANNEL_ID)
    org.junit.Assert.assertNotNull(channel)
    assertEquals("Study & Learning Reminders", channel.name)
  }

  @Test
  fun `analytics calculator stats and completion rates`() {
    val today = java.time.LocalDate.of(2026, 11, 10)
    val todayEpoch = today.toEpochDay()

    val task1 = ScheduledTask(
      id = 1,
      title = "Lesson 1",
      category = "Design",
      dateEpochDay = todayEpoch,
      startTime = "07:00 PM",
      durationMinutes = 60,
      isCompleted = true
    )
    val task2 = ScheduledTask(
      id = 2,
      title = "Lesson 2",
      category = "Design",
      dateEpochDay = todayEpoch,
      startTime = "08:00 PM",
      durationMinutes = 30,
      isCompleted = false
    )
    val pastTask = ScheduledTask(
      id = 3,
      title = "Lesson 3",
      category = "Design",
      dateEpochDay = todayEpoch - 1,
      startTime = "07:00 PM",
      durationMinutes = 45,
      isCompleted = true
    )
    val missedTask = ScheduledTask(
      id = 4,
      title = "Lesson 4",
      category = "Design",
      dateEpochDay = todayEpoch - 2,
      startTime = "07:00 PM",
      durationMinutes = 45,
      isCompleted = false
    )

    val plan = LearningPlan(
      id = 10,
      name = "Photo Editing",
      description = "Photo editing basics",
      category = "Design",
      startDate = 1000L,
      endDate = 2000L,
      dailyMinutes = 60,
      status = "ACTIVE",
      completedLessons = 2,
      totalLessons = 10,
      progressPercent = 20f
    )

    val stats = com.example.util.AnalyticsCalculator.calculateStats(
      tasks = listOf(task1, task2, pastTask, missedTask),
      plans = listOf(plan),
      today = today
    )

    // Today: 1 completed out of 2 = 50%
    assertEquals(50f, stats.dailyCompletionRate, 0.01f)
    // Completed hours: 60 + 45 = 105 mins = 1h 45m
    assertEquals(105L, stats.totalLearningMinutes)
    assertEquals("1h 45m", stats.totalLearningHoursFormatted)
    // Completed lessons: 2
    assertEquals(2, stats.completedLessonsCount)
    // Missed lessons: task 4 is overdue (epoch - 2) and incomplete
    assertEquals(1, stats.missedLessonsCount)
    // Streak: today and yesterday both have completed tasks -> current streak = 2
    assertEquals(2, stats.currentStreak)
    // Plan progress
    assertEquals(20f, stats.overallPlanProgress, 0.01f)
  }

  @Test
  fun `analytics calculator longest streak across multiple runs`() {
    val activeDays = setOf(100L, 101L, 102L, 110L, 111L, 112L, 113L, 114L, 120L)
    val longest = com.example.util.AnalyticsCalculator.calculateLongestStreak(activeDays)
    // Days 110..114 = 5 consecutive days
    assertEquals(5, longest)
  }

  @Test
  fun `analytics calculator ai insights and goal predictions`() {
    val today = java.time.LocalDate.of(2026, 11, 10)
    val plan = LearningPlan(
      id = 1,
      name = "Photo Editing Mastery",
      description = "Complete curriculum",
      category = "Design",
      startDate = 1000L,
      endDate = 5000L,
      dailyMinutes = 60,
      status = "ACTIVE",
      totalLessons = 30,
      completedLessons = 10
    )
    val stats = com.example.data.model.ProductivityStats(
      dailyCompletionRate = 100f,
      weeklyCompletionRate = 85f,
      totalLearningMinutes = 600L,
      totalLearningHoursFormatted = "10h 00m",
      completedLessonsCount = 10,
      currentStreak = 5,
      longestStreak = 5
    )

    val insight = com.example.util.AnalyticsCalculator.generateAiInsight(
      stats = stats,
      plans = listOf(plan),
      missedTasks = emptyList(),
      memory = null,
      today = today
    )

    assertTrue(insight.weeklySummary.contains("10 sessions"))
    assertTrue(insight.weeklySummary.contains("10h 00m"))
    assertTrue(insight.streakStatus.contains("5 Day Streak"))
    assertTrue(insight.goalPrediction.contains("Photo Editing Mastery"))
  }
}


