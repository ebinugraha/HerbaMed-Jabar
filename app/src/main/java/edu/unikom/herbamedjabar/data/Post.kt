package edu.unikom.herbamedjabar.data

import com.google.firebase.firestore.FieldValue

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePictureUrl: String? = null,
    val imageUrl: String = "",
    val plantName: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList()
)
