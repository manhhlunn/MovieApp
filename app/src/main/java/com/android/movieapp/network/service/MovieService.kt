package com.android.movieapp.network.service

import com.android.movieapp.models.network.ExternalIDs
import com.android.movieapp.models.network.GenreListResponse
import com.android.movieapp.models.network.ImageListResponse
import com.android.movieapp.models.network.KeywordListResponse
import com.android.movieapp.models.network.ListCreditsResponse
import com.android.movieapp.models.network.MovieDetail
import com.android.movieapp.models.network.VideoListResponse

class MovieService(private val networkService: NetworkService) {

    suspend fun fetchCredits(
        id: Int,
        language: String
    ) = networkService.request<ListCreditsResponse>(
        url = "/3/movie/$id/credits",
        parameters = hashMapOf("language" to language)
    )

    suspend fun fetchKeywords(id: Int) = networkService.request<KeywordListResponse>(
        url = "/3/movie/$id/keywords"
    )

    suspend fun fetchDetail(id: Int) = networkService.request<MovieDetail>(
        url = "/3/movie/$id"
    )

    suspend fun fetchExternalIDs(id: Int) = networkService.request<ExternalIDs>(
        url = "/3/movie/$id/external_ids"
    )

    suspend fun fetchVideos(id: Int) = networkService.request<VideoListResponse>(
        url = "/3/movie/$id/videos"
    )

    suspend fun fetchImages(id: Int) = networkService.request<ImageListResponse>(
        url = "/3/movie/$id/images"
    )

    suspend fun genresMovie(language: String) =
        networkService.request<GenreListResponse>(
            url = "/3/genre/movie/list",
            parameters = hashMapOf("language" to language)
        )

}

