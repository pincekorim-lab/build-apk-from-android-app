package com.example.ui.screens

import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import com.example.model.User
import com.example.data.StoryManiaRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onArchiveClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogout: () -> Unit,
    repository: StoryManiaRepository
) {
    val currentUser = Firebase.auth.currentUser
    var userProfile by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            repository.observeUser(uid).collect { u ->
                userProfile = u
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userProfile?.username ?: "Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    if (userProfile?.isAdmin == true) {
                        IconButton(onClick = onAdminClick) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Panel", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            if (userProfile?.photoUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = userProfile!!.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier.size(96.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (userProfile?.username?.take(1) ?: "U").uppercase(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userProfile?.fullName?.ifBlank { "StoryMania User" } ?: "StoryMania User",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = "@${userProfile?.username ?: "username"}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userProfile?.bio?.ifBlank { "No bio yet." } ?: "No bio yet.",
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("0", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Stories", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${userProfile?.followersCount ?: 0}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Followers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${userProfile?.followingCount ?: 0}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Following", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Edit Profile")
                }
                OutlinedButton(
                    onClick = onArchiveClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Archive")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    Firebase.auth.signOut()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }
        }
    }
}
