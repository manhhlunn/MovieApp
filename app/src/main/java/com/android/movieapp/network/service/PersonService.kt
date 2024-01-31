package com.android.movieapp.network.service

import com.android.movieapp.models.network.ExternalIDs
import com.android.movieapp.models.network.PersonDetail
import com.android.movieapp.models.network.PersonImageListResponse
import com.android.movieapp.models.network.PersonMovieResponse
import com.android.movieapp.models.network.PersonTvResponse

class PersonService(private val networkService: NetworkService) {

    suspend fun fetchDetail(
        id: Int,
        language: String
    ) = networkService.request<PersonDetail>(
        url = "/3/person/$id",
        parameters = hashMapOf("language" to language)
    )

    suspend fun fetchMovieCredits(
        id: Int,
        language: String
    ) = networkService.request<PersonMovieResponse>(
        url = "/3/person/$id/movie_credits",
        parameters = hashMapOf("language" to language)
    )

    suspend fun fetchExternalIDs(id: Int) = networkService.request<ExternalIDs>(
        url = "/3/person/$id/external_ids"
    )

    suspend fun fetchTvCredits(
        id: Int,
        language: String
    ) = networkService.request<PersonTvResponse>(
        url = "/3/person/$id/tv_credits",
        parameters = hashMapOf("language" to language)
    )

    suspend fun fetchImages(
        id: Int
    ) = networkService.request<PersonImageListResponse>(
        url = "/3/person/$id/images"
    )
}
