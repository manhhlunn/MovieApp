package com.android.movieapp.models.network


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
data class OMovieDetailResponse(
    @SerializedName("episodes")
    val episodes: List<Episode>?,
    @SerializedName("movie")
    val movie: OMovieDetail?,
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("status")
    val status: Boolean?
) {
    @Keep
    @Parcelize
    data class Episode(
        @SerializedName("server_data")
        val serverData: List<ServerData>?,
        @SerializedName("server_name")
        val serverName: String?
    ) : Parcelable {
        @Keep
        @Parcelize
        data class ServerData(
            @SerializedName("link_embed")
            val linkEmbed: String?,
            @SerializedName("link_m3u8")
            val linkM3u8: String?,
            @SerializedName("name")
            val name: String?,
        ) : Parcelable
    }

    @Keep
    data class OMovieDetail(
        @SerializedName("category")
        val category: List<Category>?,
        @SerializedName("content")
        val content: String?,
        @SerializedName("episode_current")
        val episodeCurrent: String?,
        @SerializedName("episode_total")
        val episodeTotal: String?,
        @SerializedName("_id")
        val id: String?,
        @SerializedName("lang")
        val lang: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("origin_name")
        val originName: String?,
        @SerializedName("poster_url")
        val posterUrl: String?,
        @SerializedName("quality")
        val quality: String?,
        @SerializedName("slug")
        val slug: String?,
        @SerializedName("thumb_url")
        val thumbUrl: String?,
        @SerializedName("time")
        val time: String?,
        @SerializedName("trailer_url")
        val trailerUrl: String?,
        @SerializedName("view")
        val view: Int?,
        @SerializedName("year")
        val year: Int?
    )
}

@Keep
data class Category(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("slug")
    val slug: String?
)