package com.android.movieapp.models.network


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TvDetail(
    @SerializedName("genres")
    val genres: List<GenreItemResponse>?,
    @SerializedName("production_companies")
    val productionCompanies: List<ProductionCompany>?,
    @SerializedName("seasons")
    val seasons: List<Season>?,
    @SerializedName("episode_run_time")
    val episodeRunTime: List<Int>?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("type")
    val type: String?
) {

    @Keep
    data class Season(
        @SerializedName("id")
        val id: Int?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("air_date")
        val airDate: String?,
        @SerializedName("episode_count")
        val episodeCount: Int?,
        @SerializedName("overview")
        val overview: String?,
        @SerializedName("poster_path")
        val posterPath: String?
    )
}

@Keep
data class ProductionCompany(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("logo_path")
    val logoPath: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("origin_country")
    val originCountry: String?
)

