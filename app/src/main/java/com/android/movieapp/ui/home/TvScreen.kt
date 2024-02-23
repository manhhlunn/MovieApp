package com.android.movieapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.usecase.FilterUseCase
import com.android.movieapp.usecase.PopularUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class PopularTvViewModel @Inject constructor(private val popularUseCase: PopularUseCase) :
    BaseSearchViewModel<Tv>() {

    override val invoke: (String) -> Flow<PagingData<Tv>> = {
        popularUseCase.invokeTv(it)
    }
}


@HiltViewModel
class FavoriteTvViewModel @Inject constructor(favoriteRepository: FavoriteRepository) :
    ViewModel() {

    val favoriteTv = favoriteRepository.favoriteTvs()
}

@HiltViewModel
class WatchedTvViewModel @Inject constructor(favoriteRepository: FavoriteRepository) :
    ViewModel() {

    val watchedTv = favoriteRepository.watchedTvs()
}

@HiltViewModel
class FilterTvViewModel @Inject constructor(private val filterUseCase: FilterUseCase) :
    BaseFilterViewModel<Tv>() {

    override val invoke: (FilterValue) -> Flow<PagingData<Tv>> = {
        filterUseCase.invokeTv(
            it.sortValue,
            it.originCountry.value,
            it.originLanguage.value,
            it.withGenres,
            it.years,
            it.includes.firstOrNull { include -> include.type == Includes.WATCHED }?.value ?: false,
            it.includes.firstOrNull { include -> include.type == Includes.ENDED }?.value ?: false,
        )
    }

    override suspend fun invokeGenre() = filterUseCase.getTvGenres()
    override suspend fun invokeLanguage() = filterUseCase.getLanguages()
    override suspend fun invokeCountry() = filterUseCase.getCountries()
    override suspend fun invokeYear() = filterUseCase.getYears()

    init {
        fetch(FilterValue(includes = Includes.entries.mapNotNull {
            when (it) {
                Includes.FAVORITE -> null
                Includes.WATCHED -> IncludesData(it, false)
                Includes.ENDED -> IncludesData(it, true)
            }
        }))
    }
}