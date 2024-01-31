package com.android.movieapp.models.network


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class OMovieResponse(
    @SerializedName("pageProps")
    val pageProps: PageProps?
) {
    @Keep
    data class PageProps(
        @SerializedName("data")
        val data: Data?
    ) {
        @Keep
        data class Data(
            @SerializedName("items")
            val items: List<OMovie>?,
            @SerializedName("params")
            val params: Params?
        )
    }
}

@Keep
data class OMovie(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("origin_name")
    val originName: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("thumb_url")
    val thumbUrl: String?,
    @SerializedName("poster_url")
    val posterUrl: String?,
    @SerializedName("sub_docquyen")
    val subDocquyen: Boolean?,
    @SerializedName("chieurap")
    val chieurap: Boolean?,
    @SerializedName("time")
    val time: String?,
    @SerializedName("episode_current")
    val episodeCurrent: String?,
    @SerializedName("quality")
    val quality: String?,
    @SerializedName("lang")
    val lang: String?,
    @SerializedName("slug")
    val slug: String?,
    @SerializedName("year")
    val year: Int?
)

@Keep
data class Params(
    @SerializedName("pagination")
    val pagination: Pagination?
) {
    @Keep
    data class Pagination(
        @SerializedName("currentPage")
        val currentPage: Int?,
        @SerializedName("pageRanges")
        val pageRanges: Int?,
        @SerializedName("totalItems")
        val totalItems: Int?,
        @SerializedName("totalItemsPerPage")
        val totalItemsPerPage: Int?
    )
}