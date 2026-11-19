package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.User
import com.example.data.StoryManiaRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    repository: StoryManiaRepository
) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<User>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            scope.launch {
                                if (it.isNotBlank()) {
                                    searchResults = repository.searchUsers(it)
                                } else {
                                    searchResults = emptyList()
                                }
                            }
                        },
                        placeholder = { Text("Search users...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                },
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
            if (query.isBlank()) {
                Text("Recent Searches", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Search for usernames or friends to see their stories.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Users Found (${searchResults.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults) { user ->
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
                                if (user.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = user.photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp).clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(user.username.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(user.fullName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = { /* Follow action */ }) {
                                    Text("Follow")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
