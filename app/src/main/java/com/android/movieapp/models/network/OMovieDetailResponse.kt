package com.android.movieapp.models.network


import android.os.Parcelable
import androidx.annotation.Keep
import com.android.movieapp.MovieApp
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
data class OMovieDetailResponse(
    @SerializedName("data")
    val data: Data?
) {
    @Keep
    data class Data(
        @SerializedName("item")
        val item: OMovieDetail?
    )
}


@Keep
data class OMovieDetail(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("slug")
    val slug: String?,
    @SerializedName("origin_name")
    val originName: String?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("thumb_url")
    val thumbUrl: String?,
    @SerializedName("poster_url")
    val posterUrl: String?,
    @SerializedName("trailer_url")
    val trailerUrl: String?,
    @SerializedName("episode_current")
    val episodeCurrent: String?,
    @SerializedName("episode_total")
    val episodeTotal: String?,
    @SerializedName("quality")
    val quality: String?,
    @SerializedName("lang")
    val lang: String?,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("view")
    val view: Int?,
    @SerializedName("time")
    val time: String?,
    @SerializedName("category")
    val category: List<Category>?,
    @SerializedName("episodes")
    val episodes: List<Episode>?
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

@Keep
data class OMovieDetailResponse2(
    @SerializedName("pageProps")
    val pageProps: PageProps?
) {
    @Keep
    data class PageProps(
        @SerializedName("data")
        val data: OMovieDetail?
    )
}

fun NetworkResponse<OMovieDetailResponse>.resDetail(): NetworkResponse<OMovieDetail> {
    when (this) {
        is NetworkResponse.Success -> {
            return NetworkResponse.Success(
                data = this.data.data?.item?.copy(
                    posterUrl = "${MovieApp.baseImageUrl}${this.data.data.item.posterUrl}",
                    thumbUrl = "${MovieApp.baseImageUrl}${this.data.data.item.thumbUrl}"
                )
                    ?: return NetworkResponse.Error(CustomException.Normal(Exception("Data not found")))
            )
        }

        is NetworkResponse.Error -> {
            return NetworkResponse.Error(
                error = this.error
            )
        }
    }
}

fun NetworkResponse<OMovieDetailResponse2>.res2Detail(): NetworkResponse<OMovieDetail> {
    when (this) {
        is NetworkResponse.Success -> {
            return NetworkResponse.Success(
                data = this.data.pageProps?.data?.copy(
                    posterUrl = "${MovieApp.baseImageUrl}${this.data.pageProps.data.posterUrl}",
                    thumbUrl = "${MovieApp.baseImageUrl}${this.data.pageProps.data.thumbUrl}"
                )
                    ?: return NetworkResponse.Error(CustomException.Normal(Exception("Data not found")))
            )
        }

        is NetworkResponse.Error -> {
            return NetworkResponse.Error(
                error = this.error
            )
        }
    }
}