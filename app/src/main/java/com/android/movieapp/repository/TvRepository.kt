package com.android.movieapp.repository

import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.models.network.GenreItemResponse
import com.android.movieapp.models.network.ImageResponse
import com.android.movieapp.models.network.Keyword
import com.android.movieapp.models.network.ListCreditsResponse
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.TvDetail
import com.android.movieapp.models.network.Video
import com.android.movieapp.models.network.mapToListSocial
import com.android.movieapp.network.service.TvService
import com.android.movieapp.ui.detail.SocialData

class TvRepository(
    private val tvService: TvService,
    private val dataStoreManager: DataStoreManager
) : Repository {

    suspend fun getTvDetail(id: Int): TvDetail? {
        return when (val value = tvService.fetchDetail(id)) {
            is NetworkResponse.Error -> null
            is NetworkResponse.Success -> value.data
        }
    }

    suspend fun getTvGenres(): List<GenreItemResponse> {
        return when (val value = tvService.genresTv(dataStoreManager.language)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.genres
        }
    }

    suspend fun getTvCredit(id: Int): ListCreditsResponse? {
        return when (val value = tvService.fetchCredits(id, dataStoreManager.language)) {
            is NetworkResponse.Error -> (null)
            is NetworkResponse.Success -> (value.data)
        }
    }

    suspend fun getTvImages(id: Int): List<ImageResponse> {
        return when (val value = tvService.fetchImages(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> {
                val images = value.data.posters.toMutableList()
                images.addAll(value.data.backdrops)
                return images.sortedByDescending { it.voteCount }
            }
        }
    }

    suspend fun getTvKeywords(id: Int): List<Keyword> {
        return when (val value = tvService.fetchKeywords(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.results
        }
    }

    suspend fun getTvVideos(id: Int): List<Video> {
        return when (val value = tvService.fetchVideos(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.results
        }
    }

    suspend fun getIds(id: Int): List<SocialData> {
        return when (val value = tvService.fetchExternalIDs(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.mapToListSocial()
        }
    }
}
