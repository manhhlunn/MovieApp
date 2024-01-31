package com.android.movieapp.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.android.movieapp.db.AppDatabase
import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.RemoteKeyMovie
import com.android.movieapp.models.entities.RemoteKeyPerson
import com.android.movieapp.models.entities.RemoteKeyTv
import com.android.movieapp.models.entities.RemoteKeys
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.network.BaseListNetworkResponse
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.network.service.PopularService
import java.util.concurrent.TimeUnit

abstract class BasePagingSource<V : Any> : PagingSource<Int, V>() {

    abstract suspend fun apiFetch(page: Int): NetworkResponse<BaseListNetworkResponse<V>>

    override fun getRefreshKey(state: PagingState<Int, V>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, V> {
        val page = params.key ?: 1
        return when (val result = apiFetch(page)) {
            is NetworkResponse.Error -> LoadResult.Error(result.error)

            is NetworkResponse.Success -> {
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (result.data.page < result.data.totalPages) page + 1 else null
                LoadResult.Page(
                    data = result.data.results,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        }
    }
}

enum class TypeRemoteMediator {
    Movie,
    Person,
    Tv
}

@OptIn(ExperimentalPagingApi::class)
abstract class BaseRemoteMediator<V : Any>(
    private val discoverService: PopularService,
    private val appDatabase: AppDatabase,
    private val dataStoreManager: DataStoreManager
) : RemoteMediator<Int, V>() {

    abstract val type: TypeRemoteMediator
    /**
     * When additional data is needed, the Paging library calls the load() method from the RemoteMediator implementation.
     * This function typically fetches the new data from a network source and saves it to local storage.
     * Over time the data stored in the database requires invalidation, such as when the user manually triggers a refresh.
     * This is represented by the LoadType property passed to the load() method.
     * The LoadType informs the RemoteMediator whether it needs to refresh the existing data or fetch additional data that needs to be appended or prepended to the existing list.
     */

    /**
     * In cases where the local data needs to be fully refreshed, initialize() should return InitializeAction.LAUNCH_INITIAL_REFRESH.
     * This causes the RemoteMediator to perform a remote refresh to fully reload the data.
     *
     * In cases where the local data doesn't need to be refreshed, initialize() should return InitializeAction.SKIP_INITIAL_REFRESH.
     * This causes the RemoteMediator to skip the remote refresh and load the cached data.
     */
    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
        val time = appDatabase.remoteKeysDao().run {
            when (type) {
                TypeRemoteMediator.Movie -> getCreationTimeMovie()
                TypeRemoteMediator.Person -> getCreationTimePerson()
                TypeRemoteMediator.Tv -> getCreationTimeTv()
            }
        } ?: 0
        return if (System.currentTimeMillis() - time < cacheTimeout) {
            // Cached data is up-to-date, so there is no need to re-fetch
            // from the network.
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            // Need to refresh cached data from network; returning
            // LAUNCH_INITIAL_REFRESH here will also block RemoteMediator's
            // APPEND and PREPEND from running until REFRESH succeeds.
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    /** LoadType.Append
     * When we need to load data at the end of the currently loaded data set, the load parameter is LoadType.APPEND
     */
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, V>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        val data = state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
        val remoteKeysDao = appDatabase.remoteKeysDao()
        return when {
            (data is Movie) -> remoteKeysDao.getRemoteKeyByMovieID(data.id)
            (data is Tv) -> remoteKeysDao.getRemoteKeyByTvID(data.id)
            (data is Person) -> remoteKeysDao.getRemoteKeyByPersonID(data.id)
            else -> null
        }
    }

    /** LoadType.Prepend
     * When we need to load data at the beginning of the currently loaded data set, the load parameter is LoadType.PREPEND
     */
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, V>): RemoteKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        val data = state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
        val remoteKeysDao = appDatabase.remoteKeysDao()
        return when {
            (data is Movie) -> remoteKeysDao.getRemoteKeyByMovieID(data.id)
            (data is Tv) -> remoteKeysDao.getRemoteKeyByTvID(data.id)
            (data is Person) -> remoteKeysDao.getRemoteKeyByPersonID(data.id)
            else -> null
        }
    }

    /** LoadType.REFRESH
     * Gets called when it's the first time we're loading data, or when PagingDataAdapter.refresh() is called;
     * so now the point of reference for loading our data is the state.anchorPosition.
     * If this is the first load, then the anchorPosition is null.
     * When PagingDataAdapter.refresh() is called, the anchorPosition is the first visible position in the displayed list, so we will need to load the page that contains that specific item.
     */
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, V>): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        val data = state.anchorPosition?.let { state.closestItemToPosition(it) }
        val remoteKeysDao = appDatabase.remoteKeysDao()
        return when {
            (data is Movie) -> remoteKeysDao.getRemoteKeyByMovieID(data.id)
            (data is Tv) -> remoteKeysDao.getRemoteKeyByTvID(data.id)
            (data is Person) -> remoteKeysDao.getRemoteKeyByPersonID(data.id)
            else -> null
        }
    }

    /**.
     *
     * @param state This gives us information about the pages that were loaded before,
     * the most recently accessed index in the list, and the PagingConfig we defined when initializing the paging stream.
     * @param loadType this tells us whether we need to load data at the end (LoadType.APPEND)
     * or at the beginning of the data (LoadType.PREPEND) that we previously loaded,
     * or if this the first time we're loading data (LoadType.REFRESH).
     */
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, V>
    ): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                //New Query so clear the DB
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                val prevKey = remoteKeys?.prevKey
                prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)

                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with endOfPaginationReached = false because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.
                val nextKey = remoteKeys?.nextKey
                nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        return when (type) {
            TypeRemoteMediator.Movie -> {
                val data =
                    discoverService.fetchPopularMovie(
                        page = page,
                        language = dataStoreManager.language
                    )
                when (data) {
                    is NetworkResponse.Success -> {
                        val values = data.data.results
                        val endOfPaginationReached = values.isEmpty()
                        appDatabase.withTransaction {
                            if (loadType == LoadType.REFRESH) {
                                appDatabase.remoteKeysDao().clearRemoteKeyMovie()
                                appDatabase.movieDao().clearAllMovies()
                            }
                            val prevKey = if (page > 1) page - 1 else null
                            val nextKey = if (endOfPaginationReached) null else page + 1

                            val remoteKeys = values.map {
                                RemoteKeyMovie(
                                    movieID = it.id,
                                    prevKey = prevKey,
                                    currentPage = page,
                                    nextKey = nextKey
                                )
                            }
                            appDatabase.remoteKeysDao().insertAllRemoteKeyMovie(remoteKeys)
                            appDatabase.movieDao()
                                .insertAll(values.onEachIndexed { _, movie -> movie.page = page })
                        }
                        return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                    }

                    is NetworkResponse.Error -> MediatorResult.Error(data.error)
                }
            }

            TypeRemoteMediator.Person -> {
                val data =
                    discoverService.fetchPopularPerson(
                        page = page,
                        language = dataStoreManager.language
                    )
                when (data) {
                    is NetworkResponse.Success -> {
                        val values = data.data.results
                        val endOfPaginationReached = values.isEmpty()
                        appDatabase.withTransaction {
                            if (loadType == LoadType.REFRESH) {
                                appDatabase.remoteKeysDao().clearRemoteKeyPerson()
                                appDatabase.personDao().clearAllPersons()
                            }
                            val prevKey = if (page > 1) page - 1 else null
                            val nextKey = if (endOfPaginationReached) null else page + 1

                            val remoteKeys = values.map {
                                RemoteKeyPerson(
                                    personId = it.id,
                                    prevKey = prevKey,
                                    currentPage = page,
                                    nextKey = nextKey
                                )
                            }
                            appDatabase.remoteKeysDao().insertAllRemoteKeyPerson(remoteKeys)
                            appDatabase.personDao()
                                .insertAll(values.onEachIndexed { _, person -> person.page = page })
                        }
                        return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                    }

                    is NetworkResponse.Error -> MediatorResult.Error(data.error)
                }
            }

            TypeRemoteMediator.Tv -> {
                val data = discoverService.fetchPopularTv(
                    page = page,
                    language = dataStoreManager.language
                )
                when (data) {
                    is NetworkResponse.Success -> {
                        val values = data.data.results
                        val endOfPaginationReached = values.isEmpty()
                        appDatabase.withTransaction {
                            if (loadType == LoadType.REFRESH) {
                                appDatabase.remoteKeysDao().clearRemoteKeyTv()
                                appDatabase.tvDao().clearAllTvs()
                            }
                            val prevKey = if (page > 1) page - 1 else null
                            val nextKey = if (endOfPaginationReached) null else page + 1

                            val remoteKeys = values.map {
                                RemoteKeyTv(
                                    tvID = it.id,
                                    prevKey = prevKey,
                                    currentPage = page,
                                    nextKey = nextKey
                                )
                            }
                            appDatabase.remoteKeysDao().insertAllRemoteKeyTv(remoteKeys)
                            appDatabase.tvDao()
                                .insertAll(values.onEachIndexed { _, tv -> tv.page = page })
                        }
                        return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                    }

                    is NetworkResponse.Error -> MediatorResult.Error(data.error)
                }
            }
        }
    }
}