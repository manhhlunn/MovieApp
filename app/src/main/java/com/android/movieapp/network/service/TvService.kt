package com.android.movieapp.network.service

import com.android.movieapp.models.network.ExternalIDs
import com.android.movieapp.models.network.GenreListResponse
import com.android.movieapp.models.network.ImageListResponse
import com.android.movieapp.models.network.KeywordResultResponse
import com.android.movieapp.models.network.ListCreditsResponse
import com.android.movieapp.models.network.TvDetail
import com.android.movieapp.models.network.VideoListResponse

class TvService(private val networkService: NetworkService) {

    suspend fun fetchCredits(
        id: Int,
        language: String
    ) = networkService.request<ListCreditsResponse>(
        url = "/3/tv/$id/credits",
        parameters = hashMapOf("language" to language)
    )

    suspend fun fetchKeywords(id: Int) = networkService.request<KeywordResultResponse>(
        url = "/3/tv/$id/keywords"
    )

    suspend fun fetchDetail(id: Int) = networkService.request<TvDetail>(
        url = "/3/tv/$id"
    )

    suspend fun fetchVideos(id: Int) = networkService.request<VideoListResponse>(
        url = "/3/tv/$id/videos"
    )

    suspend fun fetchExternalIDs(id: Int) = networkService.request<ExternalIDs>(
        url = "/3/tv/$id/external_ids"
    )

    suspend fun fetchImages(id: Int) = networkService.request<ImageListResponse>(
        url = "/3/tv/$id/images"
    )

    suspend fun genresTv(language: String) =
        networkService.request<GenreListResponse>(
            url = "/3/genre/tv/list",
            parameters = hashMapOf("language" to language)
        )
}
