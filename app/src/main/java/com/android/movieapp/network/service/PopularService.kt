package com.android.movieapp.network.service

import com.android.movieapp.models.network.MovieListResponse
import com.android.movieapp.models.network.PersonListResponse
import com.android.movieapp.models.network.TvListResponse

class PopularService(private val networkService: NetworkService) {

    suspend fun fetchPopularMovie(
        language: String,
        page: Int
    ) = networkService.request<MovieListResponse>(
        url = "/3/trending/movie/day",
        parameters = hashMapOf("language" to language, "page" to page)
    )

    suspend fun fetchPopularTv(
        language: String,
        page: Int
    ) = networkService.request<TvListResponse>(
        url = "/3/trending/tv/day",
        parameters = hashMapOf("language" to language, "page" to page)
    )

    suspend fun fetchPopularPerson(
        language: String,
        page: Int
    ) = networkService.request<PersonListResponse>(
        url = "/3/trending/person/day",
        parameters = hashMapOf("language" to language, "page" to page)
    )
}
