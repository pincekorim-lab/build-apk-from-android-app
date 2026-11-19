package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.StoryManiaRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    repository: StoryManiaRepository
) {
    val currentUser = Firebase.auth.currentUser
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            repository.observeUser(uid).collect { user ->
                if (user != null) {
                    username = user.username
                    fullName = user.fullName
                    bio = user.bio
                    photoUrl = user.photoUrl
                    isPrivate = user.isPrivate
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val uid = currentUser?.uid ?: return@Button
                            isLoading = true
                            scope.launch {
                                repository.updateUserProfile(uid, username, fullName, bio, photoUrl, isPrivate)
                                isLoading = false
                                onBack()
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Save")
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
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = photoUrl,
                onValueChange = { photoUrl = it },
                label = { Text("Profile Photo URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}
