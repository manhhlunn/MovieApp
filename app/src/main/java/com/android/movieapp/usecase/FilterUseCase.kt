package com.android.movieapp.usecase

import androidx.paging.PagingData
import com.android.movieapp.ds.DataStoreManager

import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.network.GenreItemResponse
import com.android.movieapp.network.service.SortValue
import com.android.movieapp.repository.ConfigureRepository
import com.android.movieapp.repository.FilterRepository
import com.android.movieapp.repository.MovieRepository
import com.android.movieapp.repository.TvRepository
import com.android.movieapp.ui.ext.DropdownItem
import com.android.movieapp.ui.ext.countryIcon
import com.android.movieapp.ui.ext.languageIcon
import com.android.movieapp.ui.ext.sub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar
import javax.inject.Inject

interface FilterUseCase {

    fun invokeMovie(
        sortValue: SortValue,
        withOriginCountry: String,
        withOriginLanguage: String,
        withGenres: List<Int>,
        years: List<Int>,
        isIncludeFavoritePeople: Boolean,
        isIncludeWatched: Boolean
    ): Flow<PagingData<Movie>>

    fun invokeTv(
        sortValue: SortValue,
        withOriginCountry: String,
        withOriginLanguage: String,
        withGenres: List<Int>,
        years: List<Int>,
        isIncludeWatched: Boolean,
        isIncludeEnded: Boolean
    ): Flow<PagingData<Tv>>

    suspend fun getCountries(): Pair<DropdownItem, List<DropdownItem>>
    suspend fun getLanguages(): Pair<DropdownItem, List<DropdownItem>>
    suspend fun getMovieGenres(): List<GenreItemResponse>
    suspend fun getTvGenres(): List<GenreItemResponse>
    suspend fun getYears(): List<Int>
}

class FilterUseCaseImpl @Inject constructor(
    private val filterRepository: FilterRepository,
    private val movieRepository: MovieRepository,
    private val tvRepository: TvRepository,
    private val configureRepository: ConfigureRepository,
    private val dataStoreManager: DataStoreManager
) : FilterUseCase {

    override fun invokeMovie(
        sortValue: SortValue,
        withOriginCountry: String,
        withOriginLanguage: String,
        withGenres: List<Int>,
        years: List<Int>,
        isIncludeFavoritePeople: Boolean,
        isIncludeWatched: Boolean
    ): Flow<PagingData<Movie>> {
        return filterRepository.filterMovies(
            sortValue,
            withOriginCountry,
            withOriginLanguage,
            withGenres,
            emptyList(),
            years,
            emptyList(),
            isIncludeFavoritePeople,
            isIncludeWatched
        ).flowOn(Dispatchers.IO)
    }

    override fun invokeTv(
        sortValue: SortValue,
        withOriginCountry: String,
        withOriginLanguage: String,
        withGenres: List<Int>,
        years: List<Int>,
        isIncludeWatched: Boolean,
        isIncludeEnded: Boolean
    ): Flow<PagingData<Tv>> {
        return filterRepository.filterTvs(
            sortValue,
            withOriginCountry,
            withOriginLanguage,
            withGenres,
            emptyList(),
            years,
            emptyList(),
            isIncludeEnded,
            isIncludeWatched
        ).flowOn(Dispatchers.IO)
    }

    override suspend fun getCountries(): Pair<DropdownItem, List<DropdownItem>> {
        val values = mutableListOf(DropdownItem())
        values.addAll(configureRepository.getCountries().map {
            DropdownItem(
                it.iso31661,
                it.englishName.sub(30),
                it.iso31661.countryIcon()
            )
        })
        val current = values.find { it.value == dataStoreManager.region } ?: DropdownItem()
        return current to values
    }

    override suspend fun getLanguages(): Pair<DropdownItem, List<DropdownItem>> {
        val values = mutableListOf(DropdownItem())
        values.addAll(configureRepository.getLanguages().map {
            DropdownItem(
                it.iso6391,
                it.englishName.sub(30),
                it.iso6391.languageIcon()
            )
        })
        return DropdownItem() to values
    }

    override suspend fun getMovieGenres() = movieRepository.getMovieGenres()

    override suspend fun getTvGenres() = tvRepository.getTvGenres()

    override suspend fun getYears(): List<Int> {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val years = mutableListOf(-1)
        repeat(10) {
            years.add(year - it)
        }
        return years
    }
}
