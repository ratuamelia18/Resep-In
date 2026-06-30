package com.ratu.resep_in.model

data class Category(
    val id: String = "",
    val name: String = "",
    val imgurl: String = "",
    var isSelected: Boolean = false
)