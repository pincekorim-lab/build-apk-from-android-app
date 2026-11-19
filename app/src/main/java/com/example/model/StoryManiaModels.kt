package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class User(
    @DocumentId val uid: String = "",
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val coverPhotoUrl: String = "",
    val bio: String = "",
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val isPrivate: Boolean = false,
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Story(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhotoUrl: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "image", // "image", "video", "text"
    val caption: String = "",
    val backgroundColor: String = "#1DA1F2",
    val privacy: String = "public", // "public", "followers", "close_friends"
    val viewsCount: Long = 0L,
    val likesCount: Long = 0L,
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Notification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actorId: String = "",
    val actorUsername: String = "",
    val actorPhotoUrl: String = "",
    val type: String = "like", // "like", "reply", "follow", "request"
    val targetId: String = "",
    val message: String = "",
    val read: Boolean = false,
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Reaction(
    @DocumentId val id: String = "",
    val storyId: String = "",
    val userId: String = "",
    val username: String = "",
    val emoji: String = "❤️",
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StoryView(
    @DocumentId val id: String = "",
    val storyId: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhotoUrl: String = "",
    val viewedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Archive(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "image",
    val caption: String = "",
    val backgroundColor: String = "#1DA1F2",
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Report(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetType: String = "story", // "story", "user"
    val targetId: String = "",
    val reason: String = "",
    val createdAt: Timestamp? = null
)
