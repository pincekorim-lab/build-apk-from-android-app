package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Story
import com.example.model.User
import com.example.data.StoryManiaRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    repository: StoryManiaRepository
) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        users = repository.getAllUsers()
        stories = repository.getAllStories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Users", fontSize = 12.sp)
                        Text("${users.size}", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Stories", fontSize = 12.sp)
                        Text("${stories.size}", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                }
            }

            Text("Manage Users", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.username, fontWeight = FontWeight.Bold)
                                Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.suspendUser(user.uid, !user.isSuspended)
                                        users = repository.getAllUsers()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.isSuspended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(if (user.isSuspended) "Unsuspend" else "Suspend", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
