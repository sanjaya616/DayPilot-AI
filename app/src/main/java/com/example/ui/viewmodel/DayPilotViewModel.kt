package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAssistantResponse
import com.example.data.ai.GeminiAiService
import com.example.data.ai.GeneratedRoadmap
import com.example.data.db.AppDatabase
import com.example.data.model.AiMemory
import com.example.data.model.ChatMessage
import com.example.data.model.DailyLesson
import com.example.data.model.LearningPlan
import com.example.data.model.ScheduledTask
import com.example.data.model.AiProductivityInsight
import com.example.data.model.ProductivityStats
import com.example.data.model.UserProfile
import com.example.data.repository.DayPilotRepository
import com.example.notification.StudyAlarmScheduler
import com.example.util.AnalyticsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DayPilotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DayPilotRepository
    private val aiService = GeminiAiService()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DayPilotRepository(database)
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        StudyAlarmScheduler.createNotificationChannel(application)
    }

    val selectedDate = MutableStateFlow(LocalDate.now())
    val selectedTimeFilter = MutableStateFlow("Today")

    val isAiGenerating = MutableStateFlow(false)
    val aiRoadmapForPreview = MutableStateFlow<GeneratedRoadmap?>(null)

    val allPlans: StateFlow<List<LearningPlan>> = repository.allPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<ScheduledTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .combine(MutableStateFlow(UserProfile())) { profile, default ->
            profile ?: default
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiMemory: StateFlow<AiMemory?> = repository.aiMemory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filter tasks for the selected date
    val tasksForSelectedDate: StateFlow<List<ScheduledTask>> = combine(
        allTasks,
        selectedDate
    ) { tasks, date ->
        tasks.filter { it.dateEpochDay == date.toEpochDay() && !it.isSkipped }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's tasks for dashboard
    val todayTasks: StateFlow<List<ScheduledTask>> = allTasks
        .combine(MutableStateFlow(LocalDate.now())) { tasks, today ->
            tasks.filter { it.dateEpochDay == today.toEpochDay() && !it.isSkipped }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Missed tasks: scheduled in the past, not completed, not skipped
    val missedTasks: StateFlow<List<ScheduledTask>> = allTasks
        .combine(MutableStateFlow(LocalDate.now())) { tasks, today ->
            val todayEpoch = today.toEpochDay()
            tasks.filter { it.dateEpochDay < todayEpoch && !it.isCompleted && !it.isSkipped }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Set of dates that have tasks (used by calendar markers)
    val datesWithTasks: StateFlow<Set<LocalDate>> = allTasks
        .combine(MutableStateFlow(Unit)) { tasks, _ ->
            tasks.filter { !it.isSkipped }.map { LocalDate.ofEpochDay(it.dateEpochDay) }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Daily Completion calculation
    val todayCompletionPercentage: StateFlow<Float> = todayTasks.combine(MutableStateFlow(Unit)) { tasks, _ ->
        if (tasks.isEmpty()) 0f
        else {
            val completed = tasks.count { it.isCompleted }
            (completed.toFloat() / tasks.size.toFloat()) * 100f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Next upcoming task (first non-completed task for today)
    val nextUpcomingTask: StateFlow<ScheduledTask?> = todayTasks.combine(MutableStateFlow(Unit)) { tasks, _ ->
        tasks.firstOrNull { !it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Comprehensive real-data progress analytics
    val productivityStats: StateFlow<ProductivityStats> = combine(
        allTasks,
        allPlans
    ) { tasks, plans ->
        AnalyticsCalculator.calculateStats(tasks, plans)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductivityStats())

    // AI productivity insights, weekly summaries, and completion predictions
    val aiProductivityInsight: StateFlow<AiProductivityInsight> = combine(
        productivityStats,
        allPlans,
        missedTasks,
        aiMemory
    ) { stats, plans, missed, mem ->
        AnalyticsCalculator.generateAiInsight(stats, plans, missed, mem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiProductivityInsight())

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun setTimeFilter(filter: String) {
        selectedTimeFilter.value = filter
    }

    fun dismissRoadmapPreview() {
        aiRoadmapForPreview.value = null
    }

    fun toggleTaskCompletion(task: ScheduledTask) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updatedTask)

            if (updatedTask.isCompleted) {
                StudyAlarmScheduler.cancelTaskAlarm(getApplication(), task.id)
            }

            // Update user score & completed count in profile
            val currentProfile = userProfile.value
            val newCompletedCount = if (updatedTask.isCompleted) {
                currentProfile.totalCompletedTasks + 1
            } else {
                (currentProfile.totalCompletedTasks - 1).coerceAtLeast(0)
            }
            val newScore = if (updatedTask.isCompleted) {
                currentProfile.overallScore + 25
            } else {
                (currentProfile.overallScore - 25).coerceAtLeast(0)
            }

            repository.updateProfile(
                currentProfile.copy(
                    totalCompletedTasks = newCompletedCount,
                    overallScore = newScore
                )
            )

            // Update plan progress if task is linked to a plan
            task.planId?.let { planId ->
                val plan = allPlans.value.firstOrNull { it.id == planId }
                if (plan != null) {
                    val planTasks = allTasks.value.filter { it.planId == planId && !it.isSkipped }
                    val completedPlanTasks = planTasks.count {
                        if (it.id == task.id) updatedTask.isCompleted else it.isCompleted
                    }
                    val newProgress = if (planTasks.isNotEmpty()) {
                        (completedPlanTasks.toFloat() / planTasks.size.toFloat()) * 100f
                    } else plan.progressPercent

                    repository.updatePlan(
                        plan.copy(
                            progressPercent = newProgress,
                            completedLessons = completedPlanTasks
                        )
                    )
                }
            }
        }
    }

    fun saveTask(task: ScheduledTask) {
        viewModelScope.launch {
            val endTime = if (task.endTime.isBlank()) {
                StudyAlarmScheduler.calculateEndTime(task.startTime, task.durationMinutes)
            } else task.endTime

            val reminderMillis = StudyAlarmScheduler.calculateTriggerMillis(
                task.dateEpochDay,
                task.startTime,
                10
            )

            val taskWithTimes = task.copy(
                endTime = endTime,
                reminderTimeEpochMillis = reminderMillis
            )

            val taskId = if (task.id == 0L) {
                repository.insertTask(taskWithTimes)
            } else {
                repository.updateTask(taskWithTimes)
                task.id
            }

            val planName = task.planId?.let { pid -> allPlans.value.firstOrNull { it.id == pid }?.name } ?: "Study Plan"
            val scheduled = StudyAlarmScheduler.scheduleTaskAlarm(
                getApplication(),
                taskWithTimes.copy(id = taskId),
                planName
            )
            repository.updateTask(taskWithTimes.copy(id = taskId, notificationScheduled = scheduled))
        }
    }

    fun deleteTask(task: ScheduledTask) {
        viewModelScope.launch {
            StudyAlarmScheduler.cancelTaskAlarm(getApplication(), task.id)
            repository.deleteTask(task)
        }
    }

    fun savePlan(plan: LearningPlan) {
        viewModelScope.launch {
            if (plan.id == 0L) {
                repository.insertPlan(plan)
            } else {
                repository.updatePlan(plan)
            }
        }
    }

    fun togglePlanStatus(plan: LearningPlan) {
        viewModelScope.launch {
            val newStatus = when (plan.status) {
                "ACTIVE" -> "PAUSED"
                "PAUSED" -> "ACTIVE"
                else -> "ACTIVE"
            }
            repository.updatePlan(plan.copy(status = newStatus))

            val context = getApplication<Application>()
            val planTasks = allTasks.value.filter { it.planId == plan.id }

            if (newStatus == "PAUSED") {
                // Cancel alarms for paused plan
                planTasks.forEach { task ->
                    StudyAlarmScheduler.cancelTaskAlarm(context, task.id)
                }
            } else {
                // Re-arm alarms for resumed plan
                val todayEpoch = LocalDate.now().toEpochDay()
                planTasks.filter { !it.isCompleted && !it.isSkipped && it.dateEpochDay >= todayEpoch }
                    .forEach { task ->
                        StudyAlarmScheduler.scheduleTaskAlarm(context, task, plan.name)
                    }
            }
        }
    }

    fun deletePlan(plan: LearningPlan) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            allTasks.value.filter { it.planId == plan.id }.forEach {
                StudyAlarmScheduler.cancelTaskAlarm(context, it.id)
            }
            repository.deletePlan(plan)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun updateAiMemory(memory: AiMemory) {
        viewModelScope.launch {
            repository.updateAiMemory(memory)
        }
    }

    fun clearAiMemory() {
        viewModelScope.launch {
            repository.clearAiMemory()
        }
    }

    // Missed Session Resolutions
    fun moveRemainingLessonsForward(missedTask: ScheduledTask) {
        viewModelScope.launch {
            val planId = missedTask.planId ?: return@launch
            val plan = allPlans.value.firstOrNull { it.id == planId }
            val planTasks = allTasks.value.filter { it.planId == planId && !it.isCompleted && !it.isSkipped }
                .sortedBy { it.dateEpochDay }

            val todayEpoch = LocalDate.now().toEpochDay()
            val context = getApplication<Application>()

            planTasks.forEachIndexed { index, task ->
                val newDateEpoch = todayEpoch + index
                val newReminderMillis = StudyAlarmScheduler.calculateTriggerMillis(newDateEpoch, task.startTime, 10)
                val updatedTask = task.copy(
                    dateEpochDay = newDateEpoch,
                    reminderTimeEpochMillis = newReminderMillis
                )
                repository.updateTask(updatedTask)
                StudyAlarmScheduler.scheduleTaskAlarm(context, updatedTask, plan?.name ?: "Study Plan")
            }
        }
    }

    fun rescheduleMissedToToday(missedTask: ScheduledTask) {
        viewModelScope.launch {
            val todayEpoch = LocalDate.now().toEpochDay()
            val plan = missedTask.planId?.let { pid -> allPlans.value.firstOrNull { it.id == pid } }
            val newReminderMillis = StudyAlarmScheduler.calculateTriggerMillis(todayEpoch, missedTask.startTime, 10)
            val updatedTask = missedTask.copy(
                dateEpochDay = todayEpoch,
                reminderTimeEpochMillis = newReminderMillis
            )
            repository.updateTask(updatedTask)
            StudyAlarmScheduler.scheduleTaskAlarm(getApplication(), updatedTask, plan?.name ?: "Study Plan")
        }
    }

    fun skipMissedLesson(missedTask: ScheduledTask) {
        viewModelScope.launch {
            repository.updateTask(missedTask.copy(isSkipped = true))
            StudyAlarmScheduler.cancelTaskAlarm(getApplication(), missedTask.id)
        }
    }

    fun rebalanceMissedWithAi(missedTask: ScheduledTask) {
        sendUserChatMessage(
            "I missed my lesson '${missedTask.title}'. How should I adjust my remaining study sessions to stay on track?"
        )
    }

    fun triggerTestNotification() {
        val testTask = ScheduledTask(
            id = 999999L,
            title = "Introduction to Colour & Design Principles",
            category = "Design",
            dateEpochDay = LocalDate.now().toEpochDay(),
            startTime = "Just now",
            durationMinutes = 45,
            reminderTimeEpochMillis = System.currentTimeMillis() + 5000L
        )
        StudyAlarmScheduler.scheduleTaskAlarm(
            context = getApplication(),
            task = testTask,
            planName = "DayPilot AI Test Session",
            advanceMinutes = 0
        )
    }

    fun sendUserChatMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val userMsg = ChatMessage(
                sender = "USER",
                text = trimmed,
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(userMsg)

            isAiGenerating.value = true

            // Contextual adjustments check (e.g. "change study time to 8 PM")
            val lower = trimmed.lowercase()
            if (lower.contains("change") && lower.contains("study time") && lower.contains("to")) {
                val newTime = when {
                    lower.contains("8 pm") -> "08:00 PM"
                    lower.contains("9 pm") -> "09:00 PM"
                    lower.contains("7 pm") -> "07:00 PM"
                    lower.contains("6 pm") -> "06:00 PM"
                    lower.contains("8 am") -> "08:00 AM"
                    else -> "08:00 PM"
                }
                val currentMem = aiMemory.value ?: AiMemory()
                repository.updateAiMemory(currentMem.copy(preferredStudyTime = newTime))
                val currentProfile = userProfile.value
                repository.updateProfile(currentProfile.copy(preferredStudyWindow = "$newTime Window"))

                // Reschedule upcoming uncompleted tasks with the new time
                val todayEpoch = LocalDate.now().toEpochDay()
                val context = getApplication<Application>()
                allTasks.value.filter { !it.isCompleted && !it.isSkipped && it.dateEpochDay >= todayEpoch }.forEach { task ->
                    val endTime = StudyAlarmScheduler.calculateEndTime(newTime, task.durationMinutes)
                    val reminderMillis = StudyAlarmScheduler.calculateTriggerMillis(task.dateEpochDay, newTime, 10)
                    val updated = task.copy(startTime = newTime, endTime = endTime, reminderTimeEpochMillis = reminderMillis)
                    repository.updateTask(updated)
                    val planName = task.planId?.let { pid -> allPlans.value.firstOrNull { it.id == pid }?.name } ?: "Study Plan"
                    StudyAlarmScheduler.scheduleTaskAlarm(context, updated, planName)
                }

                val responseMsg = ChatMessage(
                    sender = "AI",
                    text = "I have updated your preferred study time to **$newTime** across your memory profile and rescheduled all future alarms.",
                    timestamp = System.currentTimeMillis()
                )
                repository.insertChatMessage(responseMsg)
                isAiGenerating.value = false
                return@launch
            }

            val result = aiService.processAssistantMessage(
                userMessage = trimmed,
                activePlans = allPlans.value,
                todayTasks = todayTasks.value,
                allTasks = allTasks.value,
                userProfile = userProfile.value,
                aiMemory = aiMemory.value
            )

            isAiGenerating.value = false

            when (result) {
                is AiAssistantResponse.RoadmapProposal -> {
                    aiRoadmapForPreview.value = result.roadmap
                    val aiMsg = ChatMessage(
                        sender = "AI",
                        text = result.explanation,
                        timestamp = System.currentTimeMillis(),
                        suggestedPlanTitle = result.roadmap.title,
                        suggestedCategory = result.roadmap.category,
                        suggestedDailyMinutes = result.roadmap.dailyDurationMinutes,
                        suggestedDurationDays = result.roadmap.totalDays
                    )
                    repository.insertChatMessage(aiMsg)
                }
                is AiAssistantResponse.ClarificationNeeded -> {
                    val aiMsg = ChatMessage(
                        sender = "AI",
                        text = result.question,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertChatMessage(aiMsg)
                }
                is AiAssistantResponse.ConversationalAnswer -> {
                    val aiMsg = ChatMessage(
                        sender = "AI",
                        text = result.text,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertChatMessage(aiMsg)
                }
                is AiAssistantResponse.Error -> {
                    val aiMsg = ChatMessage(
                        sender = "AI",
                        text = "Notice: ${result.message}",
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertChatMessage(aiMsg)
                }
            }
        }
    }

    fun approveAndScheduleRoadmap(roadmap: GeneratedRoadmap) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val endDate = now + (roadmap.totalDays * 86400000L)
            val startTime = aiMemory.value?.preferredStudyTime ?: "07:00 PM"
            val endTime = StudyAlarmScheduler.calculateEndTime(startTime, roadmap.dailyDurationMinutes)

            val learningPlan = LearningPlan(
                name = roadmap.title,
                description = roadmap.description,
                category = roadmap.category,
                startDate = now,
                endDate = endDate,
                dailyMinutes = roadmap.dailyDurationMinutes,
                progressPercent = 0f,
                status = "ACTIVE",
                colorHex = "#00E5FF",
                completedLessons = 0,
                totalLessons = roadmap.lessons.size
            )

            val dailyLessons = roadmap.lessons.map { l ->
                DailyLesson(
                    planId = 0,
                    dayNumber = l.dayNumber,
                    title = l.title,
                    durationMinutes = l.durationMinutes,
                    objectives = l.objectives.joinToString("; "),
                    steps = l.steps.joinToString("\n"),
                    resources = l.resources.joinToString("\n"),
                    assignment = l.assignment,
                    completionCriteria = l.completionCriteria,
                    isCompleted = false
                )
            }

            val newPlanId = repository.saveRoadmapWithLessons(
                plan = learningPlan,
                lessons = dailyLessons,
                preferredStartTime = startTime
            )

            // Schedule alarms for all sessions across the roadmap
            val context = getApplication<Application>()
            val todayEpoch = LocalDate.now().toEpochDay()
            val scheduledTasks = repository.allTasks.stateIn(viewModelScope).value.filter { it.planId == newPlanId }

            for (task in scheduledTasks) {
                StudyAlarmScheduler.scheduleTaskAlarm(
                    context = context,
                    task = task,
                    planName = roadmap.title,
                    advanceMinutes = 10
                )
            }

            aiRoadmapForPreview.value = null

            val confirmMsg = ChatMessage(
                sender = "AI",
                text = "Approved! **${roadmap.title}** has been automatically scheduled with ${roadmap.lessons.size} daily sessions at $startTime (reminders set for 10 minutes prior). All alarms are armed in native Android AlarmManager.",
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(confirmMsg)
        }
    }
}
