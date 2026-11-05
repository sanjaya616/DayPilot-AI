package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AiMemory
import com.example.data.model.ChatMessage
import com.example.data.model.DailyLesson
import com.example.data.model.LearningPlan
import com.example.data.model.ScheduledTask
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DayPilotRepository(private val database: AppDatabase) {

    private val planDao = database.planDao()
    private val taskDao = database.taskDao()
    private val profileDao = database.profileDao()
    private val chatDao = database.chatDao()
    private val lessonDao = database.lessonDao()
    private val aiMemoryDao = database.aiMemoryDao()

    val allPlans: Flow<List<LearningPlan>> = planDao.getAllPlans()
    val allTasks: Flow<List<ScheduledTask>> = taskDao.getAllTasks()
    val userProfile: Flow<UserProfile?> = profileDao.getProfile()
    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()
    val aiMemory: Flow<AiMemory?> = aiMemoryDao.getMemory()

    fun getTasksForDate(epochDay: Long): Flow<List<ScheduledTask>> =
        taskDao.getTasksForDate(epochDay)

    fun getLessonsForPlan(planId: Long): Flow<List<DailyLesson>> =
        lessonDao.getLessonsForPlan(planId)

    suspend fun insertTask(task: ScheduledTask): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: ScheduledTask) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: ScheduledTask) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }

    suspend fun insertPlan(plan: LearningPlan): Long = withContext(Dispatchers.IO) {
        planDao.insertPlan(plan)
    }

    suspend fun updatePlan(plan: LearningPlan) = withContext(Dispatchers.IO) {
        planDao.updatePlan(plan)
    }

    suspend fun deletePlan(plan: LearningPlan) = withContext(Dispatchers.IO) {
        planDao.deletePlan(plan)
        lessonDao.deleteLessonsForPlan(plan.id)
    }

    suspend fun deletePlanById(id: Long) = withContext(Dispatchers.IO) {
        planDao.deletePlanById(id)
        lessonDao.deleteLessonsForPlan(id)
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.updateProfile(profile)
    }

    suspend fun insertChatMessage(message: ChatMessage): Long = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        chatDao.clearMessages()
    }

    suspend fun updateAiMemory(memory: AiMemory) = withContext(Dispatchers.IO) {
        aiMemoryDao.insertOrUpdate(memory)
    }

    suspend fun clearAiMemory() = withContext(Dispatchers.IO) {
        aiMemoryDao.clearMemory()
    }

    suspend fun saveRoadmapWithLessons(
        plan: LearningPlan,
        lessons: List<DailyLesson>,
        preferredStartTime: String = "07:00 PM"
    ): Long = withContext(Dispatchers.IO) {
        val newPlanId = planDao.insertPlan(plan)
        val todayEpoch = LocalDate.now().toEpochDay()

        val lessonsWithPlanId = lessons.map {
            it.copy(id = 0, planId = newPlanId)
        }
        lessonDao.insertLessons(lessonsWithPlanId)

        // Schedule corresponding daily tasks in Room
        val scheduledTasks = lessons.mapIndexed { index, lesson ->
            ScheduledTask(
                planId = newPlanId,
                title = lesson.title,
                category = plan.category,
                dateEpochDay = todayEpoch + index,
                startTime = preferredStartTime,
                durationMinutes = lesson.durationMinutes,
                isCompleted = false,
                priority = if (index == 0) "HIGH" else "MEDIUM",
                notes = lesson.assignment.ifBlank { "Day ${lesson.dayNumber} learning session" }
            )
        }
        taskDao.insertTasks(scheduledTasks)

        newPlanId
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val planCount = planDao.getPlanCount()
        val taskCount = taskDao.getTaskCount()
        val profileCount = profileDao.getProfileCount()
        val memoryCount = aiMemoryDao.getCount()

        if (profileCount == 0) {
            profileDao.insertProfile(
                UserProfile(
                    id = 1,
                    displayName = "Alex",
                    title = "Senior Learner & Creator",
                    studyTargetHoursPerWeek = 15,
                    preferredStudyWindow = "Evening (7:00 PM - 10:00 PM)",
                    remindersEnabled = true,
                    soundEnabled = true,
                    streakDays = 5,
                    totalCompletedTasks = 12,
                    overallScore = 200
                )
            )
        }

        if (memoryCount == 0) {
            aiMemoryDao.insertOrUpdate(
                AiMemory(
                    id = 1,
                    preferredStudyTime = "07:00 PM",
                    preferredLanguages = "English, Sinhala",
                    skillLevels = "Beginner in Photo Editing, Intermediate in Java",
                    difficulties = "Needs extra practice on non-destructive layers and coroutine debugging",
                    notes = "Prefers step-by-step practical assignments with downloadable starter assets",
                    futureGoals = "Full-stack mobile app development, professional video color grading",
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val nowMillis = System.currentTimeMillis()
        val dayMillis = 86400000L

        if (planCount == 0) {
            val defaultPlans = listOf(
                LearningPlan(
                    id = 1,
                    name = "Designing Seamless User Experiences",
                    description = "Master mobile design systems, accessibility guidelines, and spatial interaction patterns.",
                    category = "Tech & Software",
                    startDate = nowMillis - (12 * dayMillis),
                    endDate = nowMillis + (18 * dayMillis),
                    dailyMinutes = 45,
                    progressPercent = 65f,
                    status = "ACTIVE",
                    colorHex = "#00E5FF",
                    completedLessons = 18,
                    totalLessons = 30
                ),
                LearningPlan(
                    id = 2,
                    name = "Effective Analytics Software Solutions",
                    description = "Learn statistical data pipelines, query optimization, and dynamic telemetry dashboards.",
                    category = "Data Analysis",
                    startDate = nowMillis - (5 * dayMillis),
                    endDate = nowMillis + (25 * dayMillis),
                    dailyMinutes = 60,
                    progressPercent = 35f,
                    status = "ACTIVE",
                    colorHex = "#A855F7",
                    completedLessons = 10,
                    totalLessons = 30
                ),
                LearningPlan(
                    id = 3,
                    name = "Client Talk Tactics & Presentation",
                    description = "Communicate technical roadmaps with stakeholders and refine product pitches.",
                    category = "Leadership",
                    startDate = nowMillis - (2 * dayMillis),
                    endDate = nowMillis + (12 * dayMillis),
                    dailyMinutes = 30,
                    progressPercent = 20f,
                    status = "PAUSED",
                    colorHex = "#FBBF24",
                    completedLessons = 3,
                    totalLessons = 14
                )
            )
            planDao.insertPlans(defaultPlans)
        }

        if (taskCount == 0) {
            val sampleTasks = listOf(
                ScheduledTask(
                    id = 1,
                    planId = 1,
                    title = "Designing Seamless User Experiences",
                    category = "Tech & Software",
                    dateEpochDay = todayEpoch,
                    startTime = "09:30 AM",
                    durationMinutes = 45,
                    isCompleted = true,
                    priority = "HIGH",
                    notes = "Cover component variants and adaptive layout hierarchy"
                ),
                ScheduledTask(
                    id = 2,
                    planId = 1,
                    title = "Effective Web Flow",
                    category = "Tech & Software",
                    dateEpochDay = todayEpoch,
                    startTime = "02:00 PM",
                    durationMinutes = 60,
                    isCompleted = false,
                    priority = "MEDIUM",
                    notes = "Prototype responsive screen transitions"
                ),
                ScheduledTask(
                    id = 3,
                    planId = 3,
                    title = "Client Talk Tactics",
                    category = "Leadership",
                    dateEpochDay = todayEpoch,
                    startTime = "05:30 PM",
                    durationMinutes = 30,
                    isCompleted = false,
                    priority = "MEDIUM",
                    notes = "Prepare weekly sprint deck"
                ),
                ScheduledTask(
                    id = 4,
                    planId = 2,
                    title = "Data Query Optimization",
                    category = "Data Analysis",
                    dateEpochDay = todayEpoch + 1,
                    startTime = "10:00 AM",
                    durationMinutes = 45,
                    isCompleted = false,
                    priority = "HIGH",
                    notes = "Index tuning and execution plans"
                ),
                ScheduledTask(
                    id = 5,
                    planId = 1,
                    title = "Design System Review",
                    category = "Tech & Software",
                    dateEpochDay = todayEpoch + 1,
                    startTime = "03:00 PM",
                    durationMinutes = 40,
                    isCompleted = false,
                    priority = "LOW",
                    notes = "Color token contrast checks"
                ),
                ScheduledTask(
                    id = 6,
                    planId = 2,
                    title = "Pipeline Telemetry Integration",
                    category = "Data Analysis",
                    dateEpochDay = todayEpoch + 2,
                    startTime = "01:30 PM",
                    durationMinutes = 60,
                    isCompleted = false,
                    priority = "HIGH",
                    notes = "Stream processing metrics"
                )
            )
            taskDao.insertTasks(sampleTasks)
        }

        val messageCount = chatDao.getMessageCount()
        if (messageCount == 0) {
            val welcomeMessages = listOf(
                ChatMessage(
                    id = 1,
                    sender = "AI",
                    text = "Welcome to DayPilot AI! I am your intelligent study and scheduling assistant powered by Gemini. You can describe any goal in natural language (e.g., 'I want to learn photo editing for 30 days at 7 PM'), and I will draft a day-by-day lesson curriculum for you to preview and edit before scheduling.",
                    timestamp = nowMillis - 3600000L
                )
            )
            chatDao.insertMessages(welcomeMessages)
        }
    }
}
