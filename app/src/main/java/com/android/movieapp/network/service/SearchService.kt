package com.android.movieapp.network.service

import com.android.movieapp.models.network.MovieListResponse
import com.android.movieapp.models.network.PersonListResponse
import com.android.movieapp.models.network.TvListResponse

class SearchService(private val networkService: NetworkService) {

    suspend fun searchMovie(
        language: String,
        page: Int,
        query: String
    ) = networkService.request<MovieListResponse>(
        url = "/3/search/movie",
        parameters = hashMapOf(
            "language" to language,
            "page" to page,
            "query" to query,
            "include_adult" to true
        )
    )

    suspend fun searchTv(
        language: String,
        page: Int,
        query: String
    ) = networkService.request<TvListResponse>(
        url = "/3/search/tv",
        parameters = hashMapOf(
            "language" to language,
            "page" to page,
            "query" to query,
            "include_adult" to true
        )
    )

    suspend fun searchPerson(
        language: String,
        page: Int,
        query: String
    ) = networkService.request<PersonListResponse>(
        url = "/3/search/person",
        parameters = hashMapOf(
            "language" to language,
            "page" to page,
            "query" to query,
            "include_adult" to true
        )
    )
}
