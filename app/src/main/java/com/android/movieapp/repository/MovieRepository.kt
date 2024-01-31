package com.android.movieapp.repository

import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.models.network.GenreItemResponse
import com.android.movieapp.models.network.ImageResponse
import com.android.movieapp.models.network.Keyword
import com.android.movieapp.models.network.ListCreditsResponse
import com.android.movieapp.models.network.MovieDetail
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.Video
import com.android.movieapp.models.network.mapToListSocial
import com.android.movieapp.network.service.MovieService
import com.android.movieapp.ui.detail.SocialData

class MovieRepository(
    private val movieService: MovieService,
    private val dataStoreManager: DataStoreManager,
) : Repository {

    suspend fun getMovieDetail(id: Int): MovieDetail? {
        return when (val value = movieService.fetchDetail(id)) {
            is NetworkResponse.Error -> null
            is NetworkResponse.Success -> value.data
        }
    }

    suspend fun getMovieGenres(): List<GenreItemResponse> {
        return when (val value = movieService.genresMovie(dataStoreManager.language)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.genres
        }
    }

    suspend fun getMovieCredit(id: Int): ListCreditsResponse? {
        return when (val value = movieService.fetchCredits(id, dataStoreManager.language)) {
            is NetworkResponse.Error -> (null)
            is NetworkResponse.Success -> (value.data)
        }
    }

    suspend fun getMovieImages(id: Int): List<ImageResponse> {
        return when (val value = movieService.fetchImages(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> {
                val images = value.data.posters.toMutableList()
                images.addAll(value.data.backdrops)
                return images.sortedByDescending { it.voteCount }
            }
        }
    }

    suspend fun getMovieKeywords(id: Int): List<Keyword> {
        return when (val value = movieService.fetchKeywords(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.keywords
        }
    }

    suspend fun getMovieVideos(id: Int): List<Video> {
        return when (val value = movieService.fetchVideos(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.results
        }
    }

    suspend fun getIds(id: Int): List<SocialData> {
        return when (val value = movieService.fetchExternalIDs(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.mapToListSocial()
        }
    }

}
