package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Archive
import com.example.data.StoryManiaRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryArchiveScreen(
    onBack: () -> Unit,
    repository: StoryManiaRepository
) {
    val currentUser = Firebase.auth.currentUser
    var archives by remember { mutableStateOf<List<Archive>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            repository.observeUserArchive(uid).collect { list ->
                archives = list
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Story Archive", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (archives.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No archived stories found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(archives) { archive ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (archive.mediaType == "text") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(archive.backgroundColor.ifBlank { "#1DA1F2" }))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = archive.caption,
                                        color = androidx.compose.ui.graphics.Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = archive.mediaUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
