package com.example.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.data.StoryManiaRepository
import com.example.model.Story
import com.example.ui.screens.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object CreateStory : Screen("create_story")
    object Search : Screen("search")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object StoryArchive : Screen("story_archive")
    object Settings : Screen("settings")
    object Admin : Screen("admin")
    object StoryViewer : Screen("story_viewer/{storyId}") {
        fun createRoute(storyId: String) = "story_viewer/$storyId"
    }
}

@Composable
fun StoryManiaNavGraph() {
    val context = LocalContext.current
    val repository = remember { StoryManiaRepository(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var currentScreen by remember { mutableStateOf<String>(if (currentUser == null) Screen.Auth.route else Screen.Home.route) }
    var selectedStoryId by remember { mutableStateOf<String?>(null) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }

    // Observe active stories
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            repository.observeActiveStories().collect { list ->
                stories = list
            }
        }
    }

    // Auth state listener
    DisposableEffect(Unit) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (currentUser == null) {
                currentScreen = Screen.Auth.route
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    when {
        currentUser == null -> {
            AuthScreen(onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                currentScreen = Screen.Home.route
            })
        }
        currentScreen == Screen.Home.route -> {
            HomeScreen(
                stories = stories,
                onOpenStory = { id ->
                    selectedStoryId = id
                    currentScreen = Screen.StoryViewer.route
                },
                onCreateStoryClick = { currentScreen = Screen.CreateStory.route },
                onSearchClick = { currentScreen = Screen.Search.route },
                onNotificationsClick = { currentScreen = Screen.Notifications.route },
                onProfileClick = { currentScreen = Screen.Profile.route }
            )
        }
        currentScreen == Screen.CreateStory.route -> {
            CreateStoryScreen(
                onStoryCreated = { currentScreen = Screen.Home.route },
                onBack = { currentScreen = Screen.Home.route },
                repository = repository
            )
        }
        currentScreen == Screen.Search.route -> {
            SearchScreen(
                onBack = { currentScreen = Screen.Home.route },
                repository = repository
            )
        }
        currentScreen == Screen.Notifications.route -> {
            NotificationsScreen(
                onBack = { currentScreen = Screen.Home.route },
                repository = repository
            )
        }
        currentScreen == Screen.Profile.route -> {
            ProfileScreen(
                onEditProfile = { currentScreen = Screen.EditProfile.route },
                onArchiveClick = { currentScreen = Screen.StoryArchive.route },
                onSettingsClick = { currentScreen = Screen.Settings.route },
                onAdminClick = { currentScreen = Screen.Admin.route },
                onLogout = { currentScreen = Screen.Auth.route },
                repository = repository
            )
        }
        currentScreen == Screen.EditProfile.route -> {
            EditProfileScreen(
                onBack = { currentScreen = Screen.Profile.route },
                repository = repository
            )
        }
        currentScreen == Screen.StoryArchive.route -> {
            StoryArchiveScreen(
                onBack = { currentScreen = Screen.Profile.route },
                repository = repository
            )
        }
        currentScreen == Screen.Settings.route -> {
            SettingsScreen(
                onBack = { currentScreen = Screen.Profile.route },
                repository = repository
            )
        }
        currentScreen == Screen.Admin.route -> {
            AdminDashboardScreen(
                onBack = { currentScreen = Screen.Profile.route },
                repository = repository
            )
        }
        currentScreen == Screen.StoryViewer.route -> {
            StoryViewerScreen(
                initialStoryId = selectedStoryId ?: "",
                stories = stories,
                onClose = { currentScreen = Screen.Home.route },
                repository = repository
            )
        }
        else -> {
            HomeScreen(
                stories = stories,
                onOpenStory = { id ->
                    selectedStoryId = id
                    currentScreen = Screen.StoryViewer.route
                },
                onCreateStoryClick = { currentScreen = Screen.CreateStory.route },
                onSearchClick = { currentScreen = Screen.Search.route },
                onNotificationsClick = { currentScreen = Screen.Notifications.route },
                onProfileClick = { currentScreen = Screen.Profile.route }
            )
        }
    }
}
