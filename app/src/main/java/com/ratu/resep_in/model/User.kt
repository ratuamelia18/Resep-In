package com.ratu.resep_in.model

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class User(
    @DocumentId
    var id: String = "",
    var nama: String = "",
    var username: String = "",
    var bio: String = "",
    var profileImageUrl: String = "",
    var email: String = "",
    var skill: String = "",
    var followersCount: Int = 0,
    var followingCount: Int = 0,
    var kategori_disukai: List<String> = emptyList()
) : Serializable