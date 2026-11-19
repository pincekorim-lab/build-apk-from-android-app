package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.StoryManiaRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { StoryManiaRepository(context) }
    val credentialManager = remember { CredentialManager.create(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun handleAuthSuccess(uid: String, userEmail: String, name: String, photo: String) {
        scope.launch {
            repository.getOrCreateUser(uid, userEmail, name, name, photo)
            isLoading = false
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1DA1F2), Color(0xFF0F172A))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "StoryMania",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Share your moments with the world",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Google Sign-In Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        val clientId = try {
                            context.getString(R.string.default_web_client_id)
                        } catch (e: Exception) {
                            errorMessage = "Google Sign-In not configured: default_web_client_id missing"
                            isLoading = false
                            return@Button
                        }
                        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
                        scope.launch {
                            try {
                                val result = credentialManager.getCredential(context as Activity, request)
                                val cred = result.credential
                                if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
                                    val authResult = Firebase.auth.signInWithCredential(
                                        GoogleAuthProvider.getCredential(googleIdToken, null)
                                    ).await()
                                    val firebaseUser = authResult.user
                                    if (firebaseUser != null) {
                                        handleAuthSuccess(
                                            firebaseUser.uid,
                                            firebaseUser.email ?: "",
                                            firebaseUser.displayName ?: "User",
                                            firebaseUser.photoUrl?.toString() ?: ""
                                        )
                                    } else {
                                        isLoading = false
                                        errorMessage = "Authentication failed"
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = "Unexpected credential type"
                                }
                            } catch (e: GetCredentialCancellationException) {
                                Log.w("Auth", "Google sign-in cancelled")
                                isLoading = false
                            } catch (e: Exception) {
                                Log.e("Auth", "Google sign-in failed", e)
                                isLoading = false
                                errorMessage = e.localizedMessage ?: "Google sign-in failed"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(
                        text = "  or email  ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter email and password"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val authResult = if (isSignUp) {
                                    Firebase.auth.createUserWithEmailAndPassword(email, password).await()
                                } else {
                                    Firebase.auth.signInWithEmailAndPassword(email, password).await()
                                }
                                val fUser = authResult.user
                                if (fUser != null) {
                                    handleAuthSuccess(fUser.uid, fUser.email ?: email, fUser.email?.substringBefore("@") ?: "user", "")
                                } else {
                                    isLoading = false
                                    errorMessage = "Authentication failed"
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.localizedMessage ?: "Authentication error"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(if (isSignUp) "Sign Up" else "Log In", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(
                        text = if (isSignUp) "Already have an account? Log In" else "Don't have an account? Sign Up",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
