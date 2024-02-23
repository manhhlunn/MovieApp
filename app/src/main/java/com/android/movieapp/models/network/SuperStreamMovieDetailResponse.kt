package com.android.movieapp.models.network

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

sealed class SuperStreamResponse {

    @Keep
    data class SuperStreamMovieDetail(
        val code: Int? = null,
        val msg: String? = null,
        val data: MovieDetail? = null
    ) : SuperStreamResponse() {

        @Keep
        data class MovieDetail(
            val id: Int? = null,
            val title: String? = null,
            val runtime: Int? = null,
            val poster: String? = null,
            val description: String? = null,
            val cats: String? = null,
            val year: Int? = null,
            @SerializedName("imdb_rating") val imdbRating: String? = null,
            val view: Int? = null,
            @SerializedName("imdb_id") val imdbId: String? = null,
            @SerializedName("quality_tag") val qualityTag: String? = null,
            @SerializedName("trailer_url") val trailerUrl: String? = null,
            @SerializedName("country_list") val countryList: List<String>? = null
        )
    }

    @Keep
    data class SuperStreamTvDetail(
        val code: Int? = null,
        val msg: String? = null,
        val data: TvDetail? = null
    ) : SuperStreamResponse() {

        @Keep
        data class TvDetail(
            val id: Int? = null,
            val title: String? = null,
            val poster: String? = null,
            @SerializedName("banner_mini") val banner: String? = null,
            val description: String? = null,
            val cats: String? = null,
            val year: Int? = null,
            val view: Int? = null,
            @SerializedName("max_season") val maxSeason: Int? = null,
            @SerializedName("max_episode") val maxEpisode: Int? = null,
            @SerializedName("imdb_rating") val imdbRating: String? = null,
            @SerializedName("trailer_url") val trailerUrl: String? = null,
            val season: List<Int>? = null,
            val episode: List<Episode>? = null,
            @SerializedName("country_list") val countryList: List<String>? = null
        )
    }


    val id: String?
        get() {
            return when (this) {
                is SuperStreamMovieDetail -> this.data?.id?.toString()
                is SuperStreamTvDetail -> this.data?.id?.toString()
            }
        }

    val trailerUrl: String?
        get() {
            return when (this) {
                is SuperStreamMovieDetail -> this.data?.trailerUrl
                is SuperStreamTvDetail -> this.data?.trailerUrl
            }
        }

    val name: String?
        get() {
            return when (this) {
                is SuperStreamMovieDetail -> this.data?.title
                is SuperStreamTvDetail -> this.data?.title
            }
        }

    val content: String?
        get() {
            return when (this) {
                is SuperStreamMovieDetail -> this.data?.description
                is SuperStreamTvDetail -> this.data?.description
            }
        }

    fun getListCategories(): List<Category> {
        return when (this) {
            is SuperStreamMovieDetail -> this.data?.cats
            is SuperStreamTvDetail -> this.data?.cats
        }
            ?.split(",")
            ?.map { it.trim().replaceFirstChar { first -> first.uppercase() } }
            ?.map { Category(null, it, null) } ?: listOf()
    }
}


@Keep
data class EpisodeResponse(
    val data: List<Episode>? = null
)

@Keep
data class Episode(
    val id: Int? = null,
    val tid: Int? = null,
    val season: Int? = null,
    val episode: Int? = null,
    @SerializedName("imdb_id") val imdbId: String? = null,
    val title: String? = null,
    val thumbs: String? = null,
    val synopsis: String? = null,
    val runtime: Int? = null
)






