package com.android.movieapp.models.network


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
data class MyMovieResponse(
    @SerializedName("items")
    val items: List<MyMovie>?,
    @SerializedName("has_next")
    val hasNext: Boolean?
)

@Parcelize
@Keep
data class MyMovie(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("origin_name")
    val originName: String?,
    @SerializedName("thumb_url")
    val thumbUrl: String?,
    @SerializedName("poster_url")
    val posterUrl: String?,
    @SerializedName("episodes")
    val episodes: OMovieDetailResponse.Episode?
) : Parcelable
