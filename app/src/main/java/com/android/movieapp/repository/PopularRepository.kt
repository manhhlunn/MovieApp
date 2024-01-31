package com.android.movieapp.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.android.movieapp.db.AppDatabase
import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.network.Api
import com.android.movieapp.network.service.PopularService
import com.android.movieapp.network.service.SearchService
import com.android.movieapp.paging.MoviesRemoteMediator
import com.android.movieapp.paging.PersonsRemoteMediator
import com.android.movieapp.paging.SearchMoviePagingSource
import com.android.movieapp.paging.SearchPersonPagingSource
import com.android.movieapp.paging.SearchTvPagingSource
import com.android.movieapp.paging.TvsRemoteMediator

class PopularRepository(
    private val discoverService: PopularService,
    private val searchService: SearchService,
    private val appDatabase: AppDatabase,
    private val dataStoreManager: DataStoreManager
) : Repository {


    @OptIn(ExperimentalPagingApi::class)
    fun getMovies() = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            appDatabase.movieDao().getMovies()
        },
        remoteMediator = MoviesRemoteMediator(
            discoverService,
            appDatabase,
            dataStoreManager
        )
    ).flow


    fun searchMovies(query: String) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            SearchMoviePagingSource(searchService, query, dataStoreManager)
        }
    ).flow


    @OptIn(ExperimentalPagingApi::class)
    fun getTvs() = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            appDatabase.tvDao().getTvs()
        },
        remoteMediator = TvsRemoteMediator(
            discoverService,
            appDatabase,
            dataStoreManager
        )
    ).flow

    fun searchTvs(query: String) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            SearchTvPagingSource(searchService, query, dataStoreManager)
        }
    ).flow

    @OptIn(ExperimentalPagingApi::class)
    fun getPersons() = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            appDatabase.personDao().getPersons()
        },
        remoteMediator = PersonsRemoteMediator(
            discoverService,
            appDatabase,
            dataStoreManager
        )
    ).flow

    fun searchPersons(query: String) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            SearchPersonPagingSource(searchService, query, dataStoreManager)
        }
    ).flow


}
