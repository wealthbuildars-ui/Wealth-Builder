package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.PaymentVerificationScreen
import com.example.ui.auth.AdminDashboardScreen
import com.example.ui.dashboard.MemberDashboardScreen
import com.example.ui.discover.DiscoverScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.quiz.QuizScreen
import com.example.ui.toolbox.ToolboxScreen

@Composable
fun WealthBuilderApp(
    authViewModel: AuthViewModel = viewModel(),
    wealthViewModel: WealthViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = authState) {
            is AuthUiState.Authenticated -> {
                val user = state.user
                if (user.isAdmin) {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        wealthViewModel = wealthViewModel
                    )
                } else if (user.accountStatus == "Approved") {
                    MainAppLayout(
                        userProfile = user,
                        authViewModel = authViewModel,
                        wealthViewModel = wealthViewModel
                    )
                } else {
                    PaymentVerificationScreen(
                        userProfile = user,
                        authViewModel = authViewModel
                    )
                }
            }
            is AuthUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            else -> {
                AuthScreen(viewModel = authViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    userProfile: com.example.data.model.UserProfile,
    authViewModel: AuthViewModel,
    wealthViewModel: WealthViewModel
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Home, 1 = Discover, 2 = Toolbox, 3 = Quizzes, 4 = Profile

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Discover") },
                    label = { Text("Discover", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("nav_discover")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Toolbox") },
                    label = { Text("Toolbox", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("nav_toolbox")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "Quizzes") },
                    label = { Text("Quizzes", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("nav_quizzes")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    MemberDashboardScreen(
                        userProfile = userProfile,
                        authViewModel = authViewModel,
                        wealthViewModel = wealthViewModel,
                        onNavigateToCategory = { category ->
                            wealthViewModel.setCategoryFilter(category)
                            wealthViewModel.selectArticle(null)
                            selectedTab = 1 // Switch to Discover Tab
                        },
                        onNavigateToArticle = { article ->
                            wealthViewModel.selectArticle(article)
                            selectedTab = 1 // Switch to Discover Tab
                        }
                    )
                }
                1 -> {
                    DiscoverScreen(viewModel = wealthViewModel)
                }
                2 -> {
                    ToolboxScreen(viewModel = wealthViewModel)
                }
                3 -> {
                    QuizScreen(
                        viewModel = wealthViewModel,
                        onAwardBadge = { badge ->
                            authViewModel.addBadge(badge)
                        }
                    )
                }
                4 -> {
                    ProfileScreen(
                        userProfile = userProfile,
                        authViewModel = authViewModel,
                        wealthViewModel = wealthViewModel,
                        onNavigateToArticle = { article ->
                            wealthViewModel.selectArticle(article)
                            selectedTab = 1 // Switch to Discover Tab
                        }
                    )
                }
            }

            // Beautiful Floating AI Assistant Chat Overlay
            com.example.ui.assistant.FloatingAiAssistantChat(
                userDisplayName = userProfile.displayName,
                onNavigateToTab = { tab ->
                    selectedTab = tab
                }
            )
        }
    }
}
