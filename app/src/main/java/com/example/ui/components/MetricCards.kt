package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductivityStats
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavySurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TwinMetricPills(
    achievedCount: Int,
    targetScore: Int,
    modifier: Modifier = Modifier,
    onAchievedClick: () -> Unit = {},
    onScoreClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Achieved Card (Mint / Cyan tint)
        MetricPillCard(
            title = "Achieved",
            value = "$achievedCount",
            icon = Icons.Default.CheckCircle,
            badgeColor = Color(0xFF133636),
            accentColor = ElectricCyan,
            modifier = Modifier.weight(1f),
            onClick = onAchievedClick
        )

        // Target / Score Card (Amber / Gold tint)
        MetricPillCard(
            title = "Final Score",
            value = "$targetScore",
            icon = Icons.Default.EmojiEvents,
            badgeColor = Color(0xFF382D19),
            accentColor = SunsetAmber,
            modifier = Modifier.weight(1f),
            onClick = onScoreClick
        )
    }
}

@Composable
fun MetricPillCard(
    title: String,
    value: String,
    icon: ImageVector,
    badgeColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DarkNavyCard)
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("metric_card_${title.lowercase()}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkNavySurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.NorthEast,
                        contentDescription = "Inspect metric",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeFilterTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Today", "Weekly", "Month", "Year")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkNavyCard)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color(0xFF1E283D) else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp)
                    .testTag("filter_tab_${tab.lowercase()}")
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) TextPrimary else TextMuted,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun StreakBannerPill(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkNavyCard)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("streak_banner_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SunsetAmber.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = SunsetAmber,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = if (currentStreak > 0) "$currentStreak Day Streak!" else "Start Your Streak!",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Daily learning momentum",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E283D))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Best: $longestStreak Days",
                color = ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AnalyticsGridSection(
    stats: ProductivityStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Total Study Hours & Weekly Completion Rate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactStatTile(
                title = "Total Study Time",
                value = stats.totalLearningHoursFormatted,
                icon = Icons.Default.AccessTime,
                accentColor = ElectricCyan,
                modifier = Modifier.weight(1f)
            )

            CompactStatTile(
                title = "Weekly Rate",
                value = "${stats.weeklyCompletionRate.toInt()}%",
                icon = Icons.Default.TrendingUp,
                accentColor = SunsetAmber,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Completed Sessions & Missed Sessions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactStatTile(
                title = "Completed Lessons",
                value = "${stats.completedLessonsCount}",
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF4ECCA3),
                modifier = Modifier.weight(1f)
            )

            CompactStatTile(
                title = "Missed Sessions",
                value = "${stats.missedLessonsCount}",
                icon = Icons.Default.WarningAmber,
                accentColor = if (stats.missedLessonsCount > 0) Color(0xFFFF6B6B) else TextMuted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactStatTile(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(DarkNavyCard)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(17.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

