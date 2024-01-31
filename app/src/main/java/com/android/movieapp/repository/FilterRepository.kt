package com.android.movieapp.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.filter
import com.android.movieapp.db.MovieDao
import com.android.movieapp.db.PersonDao
import com.android.movieapp.db.TvDao
import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.network.Api
import com.android.movieapp.network.service.FilterService
import com.android.movieapp.network.service.SortValue
import com.android.movieapp.paging.FilterMovieListPagingSource
import com.android.movieapp.paging.FilterTvListPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class FilterRepository(
    private val filterService: FilterService,
    private val dataStoreManager: DataStoreManager,
    private val movieDao: MovieDao,
    private val tvDao: TvDao,
    private val personDao: PersonDao
) {

    fun filterMovies(
        sortValue: SortValue,
        withOriginCountry: String,
        withOriginLanguage: String,
        withGenres: List<Int>,
        withKeywords: List<Int>,
        years: List<Int>,
        withCompanies: List<Int>,
        isIncludeFavoritePeople: Boolean,
        isIncludeWatched: Boolean
    ) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            FilterMovieListPagingSource(
                filterService,
                dataStoreManager,
                sortValue,
                withOriginCountry,
                withOriginLanguage,
                withGenres,
                withKeywords,
                years,
                if (isIncludeFavoritePeople) personDao.getAllFavorites()
                    .mapNotNull { it.id } else emptyList(),
                withCompanies,
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)
        .map { pagingSource ->
            if (isIncludeWatched) pagingSource else pagingSource.filter { movie ->
                movie.id !in movieDao.getAllWatched().mapNotNull { it.id }
            }
        }


    fun filterTvs(
        sortValue: SortValue,
        withOriginCountry: String,
        withOriginLanguage: String,
        withGenres: List<Int>,
        withKeywords: List<Int>,
        years: List<Int>,
        withCompanies: List<Int>,
        isIncludeEnded: Boolean,
        isIncludeWatched: Boolean
    ) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            FilterTvListPagingSource(
                filterService,
                dataStoreManager,
                sortValue,
                withOriginCountry,
                withOriginLanguage,
                withGenres,
                withKeywords,
                years,
                withCompanies,
                if (isIncludeEnded) "3" else "",
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)
        .map { pagingSource ->
            if (isIncludeWatched) pagingSource else pagingSource.filter { tv ->
                tv.id !in tvDao.getAllWatched().mapNotNull { it.id }
            }
        }

}