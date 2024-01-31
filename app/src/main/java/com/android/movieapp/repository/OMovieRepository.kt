package com.android.movieapp.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.android.movieapp.db.HistoryDao
import com.android.movieapp.models.entities.MovieHistory
import com.android.movieapp.network.Api
import com.android.movieapp.network.service.OMovieRequest
import com.android.movieapp.paging.OMoviePaging
import com.android.movieapp.paging.SearchOMoviePaging
import com.android.movieapp.ui.media.FilterCategory
import com.android.movieapp.ui.media.FilterCountry
import com.android.movieapp.ui.media.MediaType
import com.android.movieapp.ui.media.SortTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class OMovieRepository(
    private val oMovieRequest: OMovieRequest,
    private val historyDao: HistoryDao
) {

    suspend fun insertHistory(value: MovieHistory) {
        historyDao.insert(value)
    }

    suspend fun getMovieHistory(slug: String): MovieHistory? {
        return historyDao.getMovieHistory(slug)
    }

    fun searchMovie(
        query: String,
        time: SortTime,
        filterCategory: FilterCategory?,
        filterCountry: FilterCountry?,
        year: Int?
    ) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            SearchOMoviePaging(
                oMovieRequest,
                query,
                time,
                filterCategory,
                filterCountry,
                year
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)


    fun getMovies(
        type: MediaType,
        time: SortTime,
        filterCategory: FilterCategory?,
        filterCountry: FilterCountry?,
        year: Int?
    ) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            OMoviePaging(
                oMovieRequest,
                type,
                time,
                filterCategory,
                filterCountry,
                year
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)


    suspend fun getMovieDetail(slug: String) = oMovieRequest.getMovieDetail(slug)

}