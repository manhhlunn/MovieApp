package com.android.movieapp.models.network


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ImageListResponse(
    @SerializedName("backdrops")
    val backdrops: List<ImageResponse>,
    @SerializedName("id")
    val id: Int,
    @SerializedName("logos")
    val logos: List<ImageResponse>,
    @SerializedName("posters")
    val posters: List<ImageResponse>
)
