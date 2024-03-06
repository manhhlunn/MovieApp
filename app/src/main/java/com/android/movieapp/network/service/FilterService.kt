package com.android.movieapp.network.service

import com.android.movieapp.models.network.MovieListResponse
import com.android.movieapp.models.network.TvListResponse

class FilterService(private val networkService: NetworkService) {

    suspend fun filterMovie(
        language: String,
        page: Int,
        sortBy: SortValue,
        withOriginCountry: String,
        withOriginLanguage: String,
        withGenres: List<Int>,
        withKeywords: List<Int>,
        years: List<Int>,
        withCompanies: List<Int>,
        peoples: List<Int>
    ) = networkService.request<MovieListResponse>(
        url = "/3/discover/movie",
        parameters = hashMapOf(
            "include_adult" to true,
            "include_video" to true,
            "language" to language,
            "page" to page,
            "sort_by" to sortBy.getValue(true),
            "with_origin_country" to withOriginCountry,
            "with_original_language" to withOriginLanguage,
            "with_genres" to withGenres.joinToString(","),
            "with_keywords" to withKeywords.joinToString(","),
            "with_people" to peoples.joinToString("|"),
            "with_companies" to withCompanies.joinToString("|"),
            "first_air_date.gte" to years.getMinDate(),
            "first_air_date.lte" to years.getMaxDate(),
        )
    )

    suspend fun filterTv(
        language: String,
        page: Int,
        sortBy: SortValue,
        withOriginCountry: String,
        withOriginLanguage: String,
        withGenres: List<Int>,
        withKeywords: List<Int>,
        years: List<Int>,
        withCompanies: List<Int>,
        status: String
    ) = networkService.request<TvListResponse>(
        url = "/3/discover/tv",
        parameters = hashMapOf(
            "include_adult" to true,
            "language" to language,
            "page" to page,
            "sort_by" to sortBy.getValue(false),
            "with_origin_country" to withOriginCountry,
            "with_original_language" to withOriginLanguage,
            "with_genres" to withGenres.joinToString(","),
            "with_keywords" to withKeywords.joinToString(","),
            "with_companies" to withCompanies.joinToString("|"),
            "with_status" to status,
            "first_air_date.gte" to years.getMinDate(),
            "first_air_date.lte" to years.getMaxDate(),
        )
    )
}

enum class SortValue(val display: String) {
    POPULAR_DESC("Popular (↓)"),
    POPULAR_ASC("Popular (↑)"),
    REVENUE_DESC("Revenue (↓)"),
    REVENUE_ASC("Revenue (↑)"),
    DATE_DESC("Date (↓)"),
    DATE_ASC("Date (↑)"),
    RATING_DESC("Rating (↓)"),
    RATING_ASC("Rating (↑)"),
    VOTE_DESC("Vote (↓)"),
    VOTE_ASC("Vote (↑)");

    fun getValue(isMovie: Boolean): String {
        return when(this) {
            POPULAR_DESC -> "popularity.desc"
            POPULAR_ASC -> "popularity.asc"
            REVENUE_DESC -> "revenue.desc"
            REVENUE_ASC -> "revenue.asc"
            DATE_DESC -> if (isMovie) "primary_release_date.desc" else "first_air_date.desc"
            DATE_ASC -> if (isMovie) "primary_release_date.asc" else "first_air_date.asc"
            RATING_DESC -> "vote_average.desc"
            RATING_ASC -> "vote_average.asc"
            VOTE_DESC -> "vote_count.desc"
            VOTE_ASC -> "vote_count.asc"
        }
    }

}

fun List<Int>.getMinDate(): String {
    val min = minOrNull() ?: return ""
    return "$min-01-01"
}

fun List<Int>.getMaxDate(): String {
    val min = maxOrNull() ?: return ""
    return "$min-12-31"
}
