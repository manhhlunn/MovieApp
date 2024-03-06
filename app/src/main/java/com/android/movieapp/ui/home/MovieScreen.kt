package com.android.movieapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.usecase.FilterUseCase
import com.android.movieapp.usecase.PopularUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class PopularMovieViewModel @Inject constructor(private val popularUseCase: PopularUseCase) :
    BaseSearchViewModel<Movie>() {

    override val invoke: (String) -> Flow<PagingData<Movie>> = {
        popularUseCase.invokeMovie(it)
    }
}

@HiltViewModel
class FavoriteMovieViewModel @Inject constructor(favoriteRepository: FavoriteRepository) :
    ViewModel() {

    val favoriteMovie = favoriteRepository.favoriteMovies()
}

@HiltViewModel
class WatchedMovieViewModel @Inject constructor(favoriteRepository: FavoriteRepository) :
    ViewModel() {

    val watchedMovie = favoriteRepository.watchedMovies()
}

@HiltViewModel
class FilterMovieViewModel @Inject constructor(private val filterUseCase: FilterUseCase) :
    BaseFilterViewModel<Movie>() {

    override val invoke: (FilterValue) -> Flow<PagingData<Movie>> = {
        filterUseCase.invokeMovie(
            it.sortValue,
            it.originCountry.value,
            it.originLanguage.value,
            it.withGenres,
            it.years,
            it.includes.firstOrNull { include -> include.type == Includes.FAVORITE }?.value
                ?: false,
            it.includes.firstOrNull { include -> include.type == Includes.WATCHED }?.value ?: false,
        )
    }

    override suspend fun invokeGenre() = filterUseCase.getMovieGenres()
    override suspend fun invokeLanguage() = filterUseCase.getLanguages()
    override suspend fun invokeCountry() = filterUseCase.getCountries()
    override suspend fun invokeYear() = filterUseCase.getYears()

    init {
        fetch(FilterValue(includes = Includes.entries.mapNotNull {
            when (it) {
                Includes.FAVORITE -> IncludesData(it, true)
                Includes.WATCHED -> IncludesData(it, false)
                Includes.ENDED -> null
            }
        }))
    }
}

