package com.android.movieapp.models.network


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PersonImageListResponse(
    @SerializedName("profiles")
    val profiles: List<ImageResponse>,
    @SerializedName("id")
    val id: Int
)
