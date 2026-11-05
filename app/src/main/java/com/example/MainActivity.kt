package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlansScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.DarkNavyBorder
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.DayPilotTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.DayPilotViewModel

enum class NavigationSection(
    val titleRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    HOME(R.string.title_home, Icons.Filled.Home, Icons.Outlined.Home),
    ASSISTANT(R.string.title_assistant, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    PLANS(R.string.title_plans, Icons.Filled.Explore, Icons.Outlined.Explore),
    CALENDAR(R.string.title_calendar, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    PROFILE(R.string.title_profile, Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {

    private val viewModel: DayPilotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.util.configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            DayPilotTheme {
                var currentSection by rememberSaveable { mutableStateOf(NavigationSection.HOME) }

                // Support Back button returning to HOME screen when on sub-screens
                BackHandler(enabled = currentSection != NavigationSection.HOME) {
                    currentSection = NavigationSection.HOME
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkNavyBackground),
                    color = DarkNavyBackground
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        Crossfade(
                            targetState = currentSection,
                            label = "ScreenTransition",
                            modifier = Modifier.fillMaxSize()
                        ) { section ->
                            when (section) {
                                NavigationSection.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToAssistant = { currentSection = NavigationSection.ASSISTANT },
                                    onNavigateToPlans = { currentSection = NavigationSection.PLANS },
                                    onNavigateToCalendar = { currentSection = NavigationSection.CALENDAR }
                                )
                                NavigationSection.ASSISTANT -> AssistantScreen(
                                    viewModel = viewModel
                                )
                                NavigationSection.PLANS -> PlansScreen(
                                    viewModel = viewModel
                                )
                                NavigationSection.CALENDAR -> CalendarScreen(
                                    viewModel = viewModel
                                )
                                NavigationSection.PROFILE -> ProfileScreen(
                                    viewModel = viewModel
                                )
                            }
                        }

                        // Modern Bottom Navigation Bar
                        DayPilotBottomNavigation(
                            currentSection = currentSection,
                            onSectionSelected = { currentSection = it },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayPilotBottomNavigation(
    currentSection: NavigationSection,
    onSectionSelected: (NavigationSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(DarkNavySurface.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("daypilot_bottom_navigation")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationSection.values().forEach { section ->
                val isSelected = section == currentSection
                val icon = if (isSelected) section.filledIcon else section.outlinedIcon
                val label = stringResource(id = section.titleRes)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSectionSelected(section) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_tab_${section.name.lowercase()}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF1F2B45) else Color.Transparent
                            )
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) ElectricCyan else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = label,
                        color = if (isSelected) ElectricCyan else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
