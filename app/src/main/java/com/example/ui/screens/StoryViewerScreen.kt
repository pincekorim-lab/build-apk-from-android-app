package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Story
import com.example.model.StoryView
import com.example.data.StoryManiaRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoryViewerScreen(
    initialStoryId: String,
    stories: List<Story>,
    onClose: () -> Unit,
    repository: StoryManiaRepository
) {
    val scope = rememberCoroutineScope()
    val currentUser = Firebase.auth.currentUser
    val activeStories = stories.filter { it.expiresAt != null }

    var currentIndex by remember {
        mutableStateOf(
            activeStories.indexOfFirst { it.id == initialStoryId }.coerceAtLeast(0)
        )
    }

    if (activeStories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("No active stories", color = Color.White)
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
        return
    }

    val currentStory = activeStories.getOrNull(currentIndex) ?: activeStories.first()
    var progress by remember(currentStory.id) { mutableStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var showViewersList by remember { mutableStateOf(false) }
    var viewers by remember { mutableStateOf<List<StoryView>>(emptyList()) }

    // Record view & load viewers if owner
    LaunchedEffect(currentStory.id) {
        if (currentUser != null) {
            repository.recordStoryView(currentStory.id, currentUser.uid, currentUser.displayName ?: "User", currentUser.photoUrl?.toString() ?: "")
        }
    }

    LaunchedEffect(currentStory.id, isPaused) {
        if (!isPaused) {
            val duration = 5000L // 5 seconds per story
            val step = 50L
            val increment = step.toFloat() / duration
            while (progress < 1f && !isPaused) {
                delay(step)
                progress += increment
            }
            if (progress >= 1f) {
                if (currentIndex < activeStories.size - 1) {
                    currentIndex++
                    progress = 0f
                } else {
                    onClose()
                }
            }
        }
    }

    // Load viewers list
    LaunchedEffect(currentStory.id) {
        repository.observeStoryViews(currentStory.id).collect { list ->
            viewers = list
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        if (offset.x < size.width / 3) {
                            // Left tap -> previous
                            if (currentIndex > 0) {
                                currentIndex--
                                progress = 0f
                            }
                        } else if (offset.x > 2 * size.width / 3) {
                            // Right tap -> next
                            if (currentIndex < activeStories.size - 1) {
                                currentIndex++
                                progress = 0f
                            } else {
                                onClose()
                            }
                        }
                    },
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    }
                )
            }
    ) {
        // Story Content
        if (currentStory.mediaType == "text") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(android.graphics.Color.parseColor(currentStory.backgroundColor.ifBlank { "#1DA1F2" }))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentStory.caption,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else if (currentStory.mediaUrl.isNotBlank()) {
            AsyncImage(
                model = currentStory.mediaUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Top Bar (Progress bars + Profile Info + Close)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                activeStories.forEachIndexed { index, _ ->
                    LinearProgressIndicator(
                        progress = {
                            when {
                                index < currentIndex -> 1f
                                index == currentIndex -> progress
                                else -> 0f
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStory.userPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = currentStory.userPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(36.dp).background(Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentStory.username.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentStory.username,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        // Caption at bottom
        if (currentStory.caption.isNotBlank() && currentStory.mediaType != "text") {
            Text(
                text = currentStory.caption,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp, start = 24.dp, end = 24.dp)
            )
        }

        // Bottom Action Bar (Reply + Reactions + Viewers)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Reply to story...", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            IconButton(onClick = {
                scope.launch {
                    repository.addReaction(currentStory.id, "❤️", currentUser?.uid ?: "", currentUser?.displayName ?: "User")
                    replyText = ""
                }
            }) {
                Text("❤️", fontSize = 24.sp)
            }

            IconButton(onClick = {
                scope.launch {
                    repository.addReaction(currentStory.id, "🔥", currentUser?.uid ?: "", currentUser?.displayName ?: "User")
                }
            }) {
                Text("🔥", fontSize = 24.sp)
            }

            if (currentStory.userId == currentUser?.uid) {
                Button(
                    onClick = { showViewersList = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${viewers.size}", color = Color.White)
                }
            }
        }

        // Viewers Sheet
        if (showViewersList) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { showViewersList = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text("Story Viewers (${viewers.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(viewers) { viewer ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (viewer.userPhotoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = viewer.userPhotoUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(viewer.username.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(viewer.username, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
