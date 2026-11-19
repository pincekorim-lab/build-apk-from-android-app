package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoryManiaRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryScreen(
    onStoryCreated: () -> Unit,
    onBack: () -> Unit,
    repository: StoryManiaRepository
) {
    val scope = rememberCoroutineScope()
    var mediaType by remember { mutableStateOf("text") } // "text" or "image"
    var caption by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var backgroundColor by remember { mutableStateOf("#1DA1F2") }
    var privacy by remember { mutableStateOf("public") }
    var isLoading by remember { mutableStateOf(false) }

    val colors = listOf("#1DA1F2", "#2563EB", "#7C3AED", "#DB2777", "#DC2626", "#059669", "#0F172A")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Story", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                val url = if (mediaType == "text") "" else mediaUrl.ifBlank { "https://picsum.photos/800/1200" }
                                repository.createStory(
                                    mediaUrl = url,
                                    mediaType = mediaType,
                                    caption = caption,
                                    backgroundColor = backgroundColor,
                                    privacy = privacy
                                )
                                isLoading = false
                                onStoryCreated()
                            }
                        },
                        enabled = !isLoading && (caption.isNotBlank() || mediaUrl.isNotBlank())
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Share Story")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = mediaType == "text",
                    onClick = { mediaType = "text" },
                    label = { Text("Text Story") },
                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                )
                FilterChip(
                    selected = mediaType == "image",
                    onClick = { mediaType = "image" },
                    label = { Text("Photo Story") },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                )
            }

            if (mediaType == "text") {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("What's on your mind?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp)
                )

                Text("Background Color", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (backgroundColor == hex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { backgroundColor = hex }
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text("Photo Image URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Add a caption...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Story Privacy", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("public" to "Public", "followers" to "Followers", "close_friends" to "Close Friends").forEach { (key, label) ->
                    FilterChip(
                        selected = privacy == key,
                        onClick = { privacy = key },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}
