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
            @SerializedName("link_mpd")
            val linkMpd: String?,
            @SerializedName("name")
            val name: String?,
        ) : Parcelable
    }

    @Keep
    data class OMovieDetail(
        @SerializedName("actor")
        val actor: List<String>?,
        @SerializedName("category")
        val category: List<Category>?,
        @SerializedName("chieurap")
        val chieurap: Boolean?,
        @SerializedName("content")
        val content: String?,
        @SerializedName("director")
        val director: List<String>?,
        @SerializedName("episode_current")
        val episodeCurrent: String?,
        @SerializedName("episode_total")
        val episodeTotal: String?,
        @SerializedName("_id")
        val id: String?,
        @SerializedName("is_copyright")
        val isCopyright: Boolean?,
        @SerializedName("lang")
        val lang: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("notify")
        val notify: String?,
        @SerializedName("origin_name")
        val originName: String?,
        @SerializedName("poster_url")
        val posterUrl: String?,
        @SerializedName("quality")
        val quality: String?,
        @SerializedName("showtimes")
        val showtimes: String?,
        @SerializedName("slug")
        val slug: String?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("sub_docquyen")
        val subDocquyen: Boolean?,
        @SerializedName("thumb_url")
        val thumbUrl: String?,
        @SerializedName("time")
        val time: String?,
        @SerializedName("trailer_url")
        val trailerUrl: String?,
        @SerializedName("type")
        val type: String?,
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