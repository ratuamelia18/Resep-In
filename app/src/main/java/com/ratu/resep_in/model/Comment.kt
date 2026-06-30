package com.ratu.resep_in.model

import com.google.firebase.Timestamp
import java.io.Serializable
import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId
    var id: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhoto: String = "",
    val comment: String = "",
    val rating: Float = 0f,
    val recipeId: String = "",
    val timestamp: Timestamp? = null
) : Serializable