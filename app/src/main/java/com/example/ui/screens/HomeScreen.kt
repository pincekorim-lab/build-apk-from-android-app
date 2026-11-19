package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Story
import com.example.ui.theme.StoryRingGradient1
import com.example.ui.theme.StoryRingGradient2
import com.example.ui.theme.StoryRingGradient3
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    stories: List<Story>,
    onOpenStory: (String) -> Unit,
    onCreateStoryClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    val myStories = stories.filter { it.userId == currentUser?.uid }
    val friendStories = stories.filter { it.userId != currentUser?.uid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "StoryMania",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* Messenger chat */ }) {
                        Icon(Icons.Default.Send, contentDescription = "Messenger")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    selected = false,
                    onClick = onSearchClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AddBox, contentDescription = "Create") },
                    label = { Text("Create") },
                    selected = false,
                    onClick = onCreateStoryClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Alerts") },
                    selected = false,
                    onClick = onNotificationsClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stories Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyRow(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Your Story item
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                if (myStories.isNotEmpty()) {
                                    onOpenStory(myStories.first().id)
                                } else {
                                    onCreateStoryClick()
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(
                                        width = if (myStories.isNotEmpty()) 3.dp else 1.dp,
                                        brush = if (myStories.isNotEmpty()) Brush.linearGradient(
                                            listOf(StoryRingGradient1, StoryRingGradient2, StoryRingGradient3)
                                        ) else Brush.linearGradient(listOf(Color.Gray, Color.Gray)),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (myStories.isNotEmpty() && myStories.first().mediaUrl.isNotBlank() && myStories.first().mediaType == "image") {
                                    AsyncImage(
                                        model = myStories.first().mediaUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                if (myStories.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(22.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Your Story", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Friends' Stories
                    items(friendStories) { story ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onOpenStory(story.id) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(
                                        width = 3.dp,
                                        brush = Brush.linearGradient(
                                            listOf(StoryRingGradient1, StoryRingGradient2, StoryRingGradient3)
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (story.userPhotoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = story.userPhotoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(story.username.take(1).uppercase(), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(story.username, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }

            // Feed Content / Welcome Banner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Welcome to StoryMania!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap on a friend's story above to view, or create your own story using the + button.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onCreateStoryClick) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Story")
                        }
                    }
                }
            }
        }
    }
}
