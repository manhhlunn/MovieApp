package com.android.movieapp.repository

import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.network.ImageResponse
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.PersonDetail
import com.android.movieapp.models.network.mapToListSocial
import com.android.movieapp.network.service.PersonService
import com.android.movieapp.ui.detail.SocialData

class PersonRepository(
    private val personService: PersonService,
    private val dataStoreManager: DataStoreManager
) : Repository {


    suspend fun getPersonDetail(id: Int): PersonDetail? {
        return when (val value = personService.fetchDetail(id, dataStoreManager.language)) {
            is NetworkResponse.Error -> null
            is NetworkResponse.Success -> value.data
        }
    }

    suspend fun getPersonImages(id: Int): List<ImageResponse> {
        return when (val value = personService.fetchImages(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.profiles
        }
    }

    suspend fun getPersonMovies(id: Int): List<Movie> {
        return when (val value = personService.fetchMovieCredits(id, dataStoreManager.language)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> {
                val values = value.data.cast?.toMutableList() ?: mutableListOf()
                value.data.crew?.let { values.addAll(it) }
                return values.sortedByDescending { it.releaseDate }
            }
        }
    }

    suspend fun getPersonTvs(id: Int): List<Tv> {
        return when (val value = personService.fetchTvCredits(id, dataStoreManager.language)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> {
                val values = value.data.cast?.toMutableList() ?: mutableListOf()
                value.data.crew?.let { values.addAll(it) }
                return values.sortedByDescending { it.firstAirDate }
            }
        }
    }

    suspend fun getIds(id: Int): List<SocialData> {
        return when (val value = personService.fetchExternalIDs(id)) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> value.data.mapToListSocial()
        }
    }
}
