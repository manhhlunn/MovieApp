package com.android.movieapp.usecase

import androidx.paging.PagingData
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.repository.PopularRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface PopularUseCase {

    fun invokeMovie(query: String): Flow<PagingData<Movie>>
    fun invokeTv(query: String): Flow<PagingData<Tv>>
    fun invokePerson(query: String): Flow<PagingData<Person>>
}

class PopularUseCaseImpl @Inject constructor(
    private val popularRepository: PopularRepository
) : PopularUseCase {

    override fun invokeMovie(query: String): Flow<PagingData<Movie>> {
        val movies =
            if (query.isBlank()) popularRepository.getMovies() else popularRepository.searchMovies(
                query
            )
        return movies.flowOn(Dispatchers.IO)
    }

    override fun invokeTv(query: String): Flow<PagingData<Tv>> {
        val movies =
            if (query.isBlank()) popularRepository.getTvs() else popularRepository.searchTvs(
                query
            )
        return movies.flowOn(Dispatchers.IO)
    }

    override fun invokePerson(query: String): Flow<PagingData<Person>> {
        val movies =
            if (query.isBlank()) popularRepository.getPersons() else popularRepository.searchPersons(
                query
            )
        return movies.flowOn(Dispatchers.IO)
    }
}