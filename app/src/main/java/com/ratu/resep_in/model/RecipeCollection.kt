package com.ratu.resep_in.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class RecipeCollection(
    val id: String = "",
    val name: String = "",
    val count: Int = 0,
    val coverImageUrl: String = "",
    val userId: String = ""
) : Parcelable