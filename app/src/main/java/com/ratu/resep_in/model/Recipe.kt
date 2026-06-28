package com.ratu.resep_in.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Recipe(
    var id: String = "",

    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("kategori") @set:PropertyName("kategori")
    var category: String = "",

    @get:PropertyName("foto") @set:PropertyName("foto")
    var imgurl: String = "",

    @get:PropertyName("videoUrl") @set:PropertyName("videoUrl")
    var videoUrl: String = "",

    @get:PropertyName("bahan") @set:PropertyName("bahan")
    var ingredients: List<String> = emptyList(),

    @get:PropertyName("langkah") @set:PropertyName("langkah")
    var steps: List<String> = emptyList(),

    @get:PropertyName("author") @set:PropertyName("author")
    var author: String = "",

    @get:PropertyName("authorId") @set:PropertyName("authorId")
    var authorId: String = "",

    @get:PropertyName("averageRating") @set:PropertyName("averageRating")
    var averageRating: Double = 0.0,

    @get:PropertyName("ratingCount") @set:PropertyName("ratingCount")
    var ratingCount: Int = 0,

    @get:PropertyName("duration") @set:PropertyName("duration")
    var duration: Int = 0,

    @get:PropertyName("__IGNORE__") @set:PropertyName("__IGNORE__")
    var timestamp: Long = 0L,

    @get:PropertyName("isArchived") @set:PropertyName("isArchived")
    var isArchived: Boolean? = false

) : Serializable