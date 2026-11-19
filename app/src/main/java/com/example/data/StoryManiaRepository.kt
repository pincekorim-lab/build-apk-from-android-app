package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class StoryManiaRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google.")
    }

    // --- USERS ---
    suspend fun getOrCreateUser(uid: String, email: String, username: String, fullName: String, photoUrl: String): Result<User> {
        return try {
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java) ?: User()
                Result.success(user)
            } else {
                val newUser = User(
                    uid = uid,
                    username = if (username.isBlank()) email.substringBefore("@") else username,
                    fullName = if (fullName.isBlank()) "StoryMania User" else fullName,
                    email = email,
                    photoUrl = photoUrl,
                    bio = "Welcome to my StoryMania profile!",
                    followersCount = 0L,
                    followingCount = 0L,
                    isPrivate = false,
                    isAdmin = email.contains("admin", ignoreCase = true),
                    createdAt = Timestamp.now()
                )
                docRef.set(newUser).await()
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Log.e("StoryRepo", "Error getOrCreateUser", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uid: String, username: String, fullName: String, bio: String, photoUrl: String, isPrivate: Boolean): Result<Unit> {
        return try {
            db.collection("users").document(uid).update(
                mapOf(
                    "username" to username,
                    "fullName" to fullName,
                    "bio" to bio,
                    "photoUrl" to photoUrl,
                    "isPrivate" to isPrivate
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val listener = db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(User::class.java))
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    // --- STORIES ---
    suspend fun createStory(mediaUrl: String, mediaType: String, caption: String, backgroundColor: String, privacy: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val userSnap = db.collection("users").document(uid).get().await()
            val user = userSnap.toObject(User::class.java)
            val username = user?.username ?: "user"
            val userPhoto = user?.photoUrl ?: ""

            val now = Timestamp.now()
            val calendar = Calendar.getInstance().apply {
                time = now.toDate()
                add(Calendar.HOUR_OF_DAY, 24)
            }
            val expiresAt = Timestamp(calendar.time)

            val storyRef = db.collection("stories").document()
            val storyMap = mapOf(
                "userId" to uid,
                "username" to username,
                "userPhotoUrl" to userPhoto,
                "mediaUrl" to mediaUrl,
                "mediaType" to mediaType,
                "caption" to caption,
                "backgroundColor" to backgroundColor,
                "privacy" to privacy,
                "viewsCount" to 0L,
                "likesCount" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "expiresAt" to expiresAt
            )
            storyRef.set(storyMap).await()

            // Also save to Archive
            val archiveRef = db.collection("archive").document()
            val archiveMap = mapOf(
                "userId" to uid,
                "mediaUrl" to mediaUrl,
                "mediaType" to mediaType,
                "caption" to caption,
                "backgroundColor" to backgroundColor,
                "createdAt" to FieldValue.serverTimestamp()
            )
            archiveRef.set(archiveMap).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StoryRepo", "Error creating story", e)
            Result.failure(e)
        }
    }

    fun observeActiveStories(): Flow<List<Story>> = callbackFlow {
        val now = Timestamp.now()
        val listener = db.collection("stories")
            .whereGreaterThan("expiresAt", now)
            .orderBy("expiresAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Story::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun deleteStory(storyId: String): Result<Unit> {
        return try {
            db.collection("stories").document(storyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- STORY VIEWS & REACTIONS ---
    suspend fun recordStoryView(storyId: String, userId: String, username: String, userPhotoUrl: String) {
        try {
            val viewId = "${storyId}_$userId"
            val viewRef = db.collection("story_views").document(viewId)
            val snap = viewRef.get().await()
            if (!snap.exists()) {
                viewRef.set(
                    mapOf(
                        "storyId" to storyId,
                        "userId" to userId,
                        "username" to username,
                        "userPhotoUrl" to userPhotoUrl,
                        "viewedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                db.collection("stories").document(storyId).update("viewsCount", FieldValue.increment(1)).await()
            }
        } catch (e: Exception) {
            Log.w("StoryRepo", "Error recording story view", e)
        }
    }

    fun observeStoryViews(storyId: String): Flow<List<StoryView>> = callbackFlow {
        val listener = db.collection("story_views")
            .whereEqualTo("storyId", storyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(StoryView::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addReaction(storyId: String, emoji: String, userId: String, username: String): Result<Unit> {
        return try {
            val reactionRef = db.collection("reactions").document()
            reactionRef.set(
                mapOf(
                    "storyId" to storyId,
                    "userId" to userId,
                    "username" to username,
                    "emoji" to emoji,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            db.collection("stories").document(storyId).update("likesCount", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ARCHIVE ---
    fun observeUserArchive(userId: String): Flow<List<Archive>> = callbackFlow {
        val listener = db.collection("archive")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Archive::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun deleteArchiveItem(archiveId: String): Result<Unit> {
        return try {
            db.collection("archive").document(archiveId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SEARCH ---
    suspend fun searchUsers(queryText: String): List<User> {
        return try {
            val snapshot = db.collection("users")
                .orderBy("username")
                .startAt(queryText)
                .endAt(queryText + "\uf8ff")
                .limit(20)
                .get()
                .await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            Log.e("StoryRepo", "Error searching users", e)
            emptyList()
        }
    }

    // --- NOTIFICATIONS ---
    fun observeNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Notification::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNotification(userId: String, actorId: String, actorUsername: String, actorPhotoUrl: String, type: String, targetId: String, message: String) {
        try {
            db.collection("notifications").document().set(
                mapOf(
                    "userId" to userId,
                    "actorId" to actorId,
                    "actorUsername" to actorUsername,
                    "actorPhotoUrl" to actorPhotoUrl,
                    "type" to type,
                    "targetId" to targetId,
                    "message" to message,
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w("StoryRepo", "Error creating notification", e)
        }
    }

    // --- ADMIN ---
    suspend fun getAllUsers(): List<User> {
        return try {
            db.collection("users").get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllStories(): List<Story> {
        return try {
            db.collection("stories").get().await().toObjects(Story::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun suspendUser(userId: String, suspend: Boolean): Result<Unit> {
        return try {
            db.collection("users").document(userId).update("isSuspended", suspend).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
