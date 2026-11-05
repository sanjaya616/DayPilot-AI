package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScheduledTask
import com.example.ui.components.AddEditTaskDialog
import com.example.ui.components.AiInsightsCard
import com.example.ui.components.AnalyticsGridSection
import com.example.ui.components.ArcProgressGauge
import com.example.ui.components.DashboardActionGrid
import com.example.ui.components.GlowingAICard
import com.example.ui.components.MissedSessionAlertBanner
import com.example.ui.components.StreakBannerPill
import com.example.ui.components.TaskCardItem
import com.example.ui.components.TimeFilterTabs
import com.example.ui.components.TwinMetricPills
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavySurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DayPilotViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: DayPilotViewModel,
    onNavigateToAssistant: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val missedTasks by viewModel.missedTasks.collectAsStateWithLifecycle()
    val allPlans by viewModel.allPlans.collectAsStateWithLifecycle()
    val stats by viewModel.productivityStats.collectAsStateWithLifecycle()
    val aiInsight by viewModel.aiProductivityInsight.collectAsStateWithLifecycle()
    val upcomingTask by viewModel.nextUpcomingTask.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTimeFilter.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<ScheduledTask?>(null) }

    val todayStr = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top App Bar Greeting
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // User Avatar
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkNavySurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User profile",
                                tint = ElectricCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Hello ${profile.displayName}",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = todayStr,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkNavyCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkNavyCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Headline Title
            item {
                Column {
                    Text(
                        text = "Your Progress",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Today",
                        color = ElectricCyan,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Prominent Missed Session Alert Banner
            if (missedTasks.isNotEmpty()) {
                val primaryMissed = missedTasks.first()
                val plan = allPlans.firstOrNull { it.id == primaryMissed.planId }
                val remaining = plan?.let { it.totalLessons - it.completedLessons } ?: 14

                item {
                    MissedSessionAlertBanner(
                        missedTask = primaryMissed,
                        planName = plan?.name ?: "Personal Roadmap",
                        remainingDays = remaining,
                        onMoveForward = { viewModel.moveRemainingLessonsForward(primaryMissed) },
                        onKeepEndDate = { viewModel.rescheduleMissedToToday(primaryMissed) },
                        onSkipLesson = { viewModel.skipMissedLesson(primaryMissed) },
                        onAskAiToRebalance = {
                            viewModel.rebalanceMissedWithAi(primaryMissed)
                            onNavigateToAssistant()
                        }
                    )
                }
            }

            // Progress Arc Gauge
            item {
                ArcProgressGauge(
                    progressPercent = stats.dailyCompletionRate,
                    score = profile.overallScore,
                    label = "Score • ${stats.dailyCompletionRate.toInt()}% Today"
                )
            }

            // Streak Banner Pill
            item {
                StreakBannerPill(
                    currentStreak = stats.currentStreak,
                    longestStreak = stats.longestStreak
                )
            }

            // Comprehensive 4-Metric Analytics Grid
            item {
                AnalyticsGridSection(stats = stats)
            }

            // Featured AI Productivity Insights & Forecast Card
            item {
                AiInsightsCard(
                    insight = aiInsight,
                    onConsultAiClick = onNavigateToAssistant
                )
            }

            // Twin Metric Pills
            item {
                TwinMetricPills(
                    achievedCount = stats.completedLessonsCount,
                    targetScore = profile.overallScore,
                    onAchievedClick = { },
                    onScoreClick = { }
                )
            }

            // Time Filter Tabs
            item {
                TimeFilterTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.setTimeFilter(it) }
                )
            }

            // Upcoming Reminder Card
            if (upcomingTask != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1B2338))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF382915))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = SunsetAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Next Upcoming Activity",
                                    color = SunsetAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = upcomingTask!!.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${upcomingTask!!.startTime} • ${upcomingTask!!.durationMinutes} min",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Glowing AI Assistant Card
            item {
                GlowingAICard(
                    onStartChat = onNavigateToAssistant
                )
            }

            // Quick Actions Grid (Scheduling & AI Roadmaps)
            item {
                DashboardActionGrid(
                    onScheduleClick = onNavigateToCalendar,
                    onRoadmapClick = onNavigateToPlans
                )
            }

            // Today's Scheduled Tasks Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Today's Activities",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${todayTasks.count { it.isCompleted }} of ${todayTasks.size} done",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    // Quick Add Task Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkNavyCard)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.testTag("quick_add_task_btn")
                        ) {
                            IconButton(
                                onClick = { showAddTaskDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Task",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Add",
                                color = ElectricCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tasks List
            if (todayTasks.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkNavyCard)
                            .padding(32.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No tasks scheduled for today",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add' or ask AI Assistant to plan your day.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        onToggleCompleted = { viewModel.toggleTaskCompletion(task) },
                        onClick = { taskToEdit = task },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        // Floating Action Button for Quick Task creation
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = ElectricCyan,
            contentColor = DarkNavyBackground,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
                .testTag("home_fab_add_task")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task",
                modifier = Modifier.size(24.dp)
            )
        }

        if (showAddTaskDialog) {
            AddEditTaskDialog(
                defaultDate = LocalDate.now(),
                onDismiss = { showAddTaskDialog = false },
                onSaveTask = {
                    viewModel.saveTask(it)
                    showAddTaskDialog = false
                }
            )
        }

        if (taskToEdit != null) {
            AddEditTaskDialog(
                taskToEdit = taskToEdit,
                onDismiss = { taskToEdit = null },
                onSaveTask = {
                    viewModel.saveTask(it)
                    taskToEdit = null
                }
            )
        }
    }
}
