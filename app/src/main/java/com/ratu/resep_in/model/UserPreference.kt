package com.ratu.resep_in.model

data class UserPreference(
    val email: String = "",
    val nama: String = "",
    val kategori: List<String> = listOf(),
    val skill: String = ""
)