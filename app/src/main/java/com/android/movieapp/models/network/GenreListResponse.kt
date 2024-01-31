package com.android.movieapp.models.network


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GenreListResponse(val genres: List<GenreItemResponse>)

@Keep
data class GenreItemResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?
)

