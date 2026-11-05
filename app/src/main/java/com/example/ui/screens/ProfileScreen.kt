package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.notification.StudyAlarmScheduler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.DarkNavyBorder
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavySurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DayPilotViewModel

@Composable
fun ProfileScreen(
    viewModel: DayPilotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val stats by viewModel.productivityStats.collectAsStateWithLifecycle()

    var remindersEnabled by remember(profile.remindersEnabled) { mutableStateOf(profile.remindersEnabled) }
    var soundEnabled by remember(profile.soundEnabled) { mutableStateOf(profile.soundEnabled) }
    var preferredHours by remember(profile.preferredStudyWindow) { mutableStateOf(profile.preferredStudyWindow) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Profile & Settings",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // User Info Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkNavyCard)
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(DarkNavySurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile.title,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF382915))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = SunsetAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (stats.currentStreak > 0) "${stats.currentStreak} Day Streak • Best: ${stats.longestStreak}d" else "Best: ${stats.longestStreak} Day Streak",
                                    color = SunsetAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Study Habits Section
            item {
                Text(
                    text = "Study Preferences",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkNavyCard)
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Preferred Study Window",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = preferredHours,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Presets
                        val windowOptions = listOf(
                            "Morning (07:00 AM - 09:00 AM)",
                            "Afternoon (02:00 PM - 05:00 PM)",
                            "Evening (07:00 PM - 10:00 PM)"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            windowOptions.forEach { option ->
                                val isSelected = option == preferredHours
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF1E283D) else DarkNavySurfaceVariant)
                                        .clickable {
                                            preferredHours = option
                                            viewModel.updateProfile(profile.copy(preferredStudyWindow = option))
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = option,
                                        color = if (isSelected) ElectricCyan else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notifications & Reminders
            item {
                Text(
                    text = "Reminders & Alarm System",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else true

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Notifications disabled. Reminders will not be displayed.", Toast.LENGTH_LONG).show()
                    }
                }

                val canExactAlarm = StudyAlarmScheduler.canScheduleExact(context)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkNavyCard)
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Permission Banner if not granted on Android 13+
                        if (!hasNotificationPermission) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF381F26))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationImportant,
                                        contentDescription = null,
                                        tint = SunsetAmber,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Notifications Disabled",
                                            color = SunsetAmber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Grant notification permission to receive your scheduled lesson alerts.",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = SunsetAmber,
                                            contentColor = DarkNavyBackground
                                        )
                                    ) {
                                        Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Study Reminders Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Study Reminders",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Alerts 10 mins before sessions",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Switch(
                                checked = remindersEnabled,
                                onCheckedChange = {
                                    remindersEnabled = it
                                    viewModel.updateProfile(profile.copy(remindersEnabled = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0B0E17),
                                    checkedTrackColor = ElectricCyan
                                ),
                                modifier = Modifier.testTag("switch_reminders")
                            )
                        }

                        // Advance Lead Time & Snooze Info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavySurfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = SunsetAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trigger: 10m before", color = TextSecondary, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Snooze: 10m", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        // Exact Alarm Permission explanation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Exact Alarm Timing",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (canExactAlarm) "Precise wakeups permitted in Doze mode" else "Tap to grant exact alarm scheduling access",
                                    color = if (canExactAlarm) EmeraldSuccess else SunsetAmber,
                                    fontSize = 11.sp
                                )
                            }

                            if (!canExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Exact alarm setting unavailable", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Grant Access", fontSize = 11.sp)
                                }
                            }
                        }

                        // Notification Sounds Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = SunsetAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Notification Sounds",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Subtle vibration and alert chime",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    soundEnabled = it
                                    viewModel.updateProfile(profile.copy(soundEnabled = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0B0E17),
                                    checkedTrackColor = SunsetAmber
                                ),
                                modifier = Modifier.testTag("switch_sounds")
                            )
                        }

                        // Test Notification Trigger
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.triggerTestNotification()
                                Toast.makeText(context, "Test reminder scheduled: firing in 5 seconds...", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E283D),
                                contentColor = ElectricCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_notification_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Test Actual Notification (Fires in 5s)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // AI Memory & Personal Context Section
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "AI Memory & Learning Context",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gemini Memory",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                val memory by viewModel.aiMemory.collectAsStateWithLifecycle()
                var showEditMemoryDialog by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkNavyCard)
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Persistent Learner Profile",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gemini uses this saved context when planning lessons",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Memory attributes
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkNavySurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Languages: ", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(memory?.preferredLanguages ?: "English, Sinhala", color = TextPrimary, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Skills: ", color = SunsetAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(memory?.skillLevels ?: "Beginner in Photo Editing, Intermediate in Java", color = TextPrimary, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Difficulties: ", color = Color(0xFFC084FC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(memory?.difficulties ?: "Non-destructive layer workflows", color = TextPrimary, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Future Goals: ", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(memory?.futureGoals ?: "Full-stack mobile app development", color = TextPrimary, fontSize = 12.sp)
                            }
                        }

                        // Action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    viewModel.clearAiMemory()
                                    Toast.makeText(context, "AI Memory reset to default context.", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset Memory", fontSize = 12.sp)
                            }

                            androidx.compose.material3.Button(
                                onClick = { showEditMemoryDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = DarkNavyBackground
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit Context", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (showEditMemoryDialog) {
                    val currentMem = memory ?: com.example.data.model.AiMemory()
                    var editLanguages by remember { mutableStateOf(currentMem.preferredLanguages) }
                    var editSkills by remember { mutableStateOf(currentMem.skillLevels) }
                    var editDifficulties by remember { mutableStateOf(currentMem.difficulties) }
                    var editGoals by remember { mutableStateOf(currentMem.futureGoals) }

                    androidx.compose.ui.window.Dialog(onDismissRequest = { showEditMemoryDialog = false }) {
                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = DarkNavyCard,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Edit AI Learner Memory", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = editLanguages,
                                    onValueChange = { editLanguages = it },
                                    label = { Text("Preferred Languages") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricCyan,
                                        unfocusedBorderColor = DarkNavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editSkills,
                                    onValueChange = { editSkills = it },
                                    label = { Text("Current Skill Levels") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricCyan,
                                        unfocusedBorderColor = DarkNavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editDifficulties,
                                    onValueChange = { editDifficulties = it },
                                    label = { Text("Learning Difficulties") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricCyan,
                                        unfocusedBorderColor = DarkNavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editGoals,
                                    onValueChange = { editGoals = it },
                                    label = { Text("Future Goals") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricCyan,
                                        unfocusedBorderColor = DarkNavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    androidx.compose.material3.OutlinedButton(
                                        onClick = { showEditMemoryDialog = false },
                                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                                    ) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            viewModel.updateAiMemory(
                                                currentMem.copy(
                                                    preferredLanguages = editLanguages.trim(),
                                                    skillLevels = editSkills.trim(),
                                                    difficulties = editDifficulties.trim(),
                                                    futureGoals = editGoals.trim(),
                                                    lastUpdated = System.currentTimeMillis()
                                                )
                                            )
                                            showEditMemoryDialog = false
                                            Toast.makeText(context, "AI Memory context saved.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = ElectricCyan,
                                            contentColor = DarkNavyBackground
                                        )
                                    ) {
                                        Text("Save", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Data & System
            item {
                Text(
                    text = "Data & Storage",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkNavyCard)
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(
                                        context,
                                        "Local Room Database active: daypilot_database.db",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Local Storage & Backup",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "SQLite Room Database with reactive Flow",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "App Theme",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Deep Charcoal Navy & Neon Accents",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Version info
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "DayPilot AI v1.0 • Personal Assistant",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
