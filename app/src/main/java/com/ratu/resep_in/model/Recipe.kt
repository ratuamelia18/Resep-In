package com.ratu.resep_in.model

import java.io.Serializable

data class Recipe(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val rating: Double = 0.0,
    val duration: Int = 0,
    val imgurl: String = "",
    val videoUrl: String = "",
    val author: String = "",
    val timestamp: Long = 0L,
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList()
) : Serializable