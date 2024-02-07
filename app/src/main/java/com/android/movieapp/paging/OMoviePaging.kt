package com.android.movieapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.movieapp.models.network.MyMovie
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.OMovie
import com.android.movieapp.models.network.OMovieResponse
import com.android.movieapp.network.service.OMovieRequest
import com.android.movieapp.ui.media.FilterCategory
import com.android.movieapp.ui.media.FilterCountry
import com.android.movieapp.ui.media.MediaType
import com.android.movieapp.ui.media.SortTime
import kotlin.math.ceil


abstract class BaseOMoviePagingSource : PagingSource<Int, OMovie>() {

    override fun getRefreshKey(state: PagingState<Int, OMovie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OMovie> {
        val page = params.key ?: 1
        return when (val result = apiFetch(page)) {
            is NetworkResponse.Error -> LoadResult.Error(result.error)

            is NetworkResponse.Success -> {
                val prevKey = if (page == 1) null else page - 1
                val nextKey by lazy {
                    val totalItems =
                        result.data.pageProps?.data?.params?.pagination?.totalItems
                            ?: return@lazy null
                    val totalItemsPerPage =
                        result.data.pageProps.data.params.pagination.totalItemsPerPage
                            ?: return@lazy null
                    val maxPage = ceil(totalItems / totalItemsPerPage.toDouble()).toInt()
                    return@lazy if (page < maxPage) page + 1 else null
                }

                LoadResult.Page(
                    data = result.data.pageProps?.data?.items ?: emptyList(),
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        }
    }

    abstract suspend fun apiFetch(page: Int): NetworkResponse<OMovieResponse>

}

class SearchOMoviePaging(
    private val oMovieRequest: OMovieRequest,
    private val query: String,
    private val time: SortTime,
    private val filterCategory: FilterCategory?,
    private val filterCountry: FilterCountry?,
    private val year: Int?
) : BaseOMoviePagingSource() {
    override suspend fun apiFetch(page: Int) = oMovieRequest.searchMovie(
        page = page,
        query = query,
        time = time,
        filterCategory = filterCategory,
        filterCountry = filterCountry,
        year = year
    )
}

class OMoviePaging(
    private val oMovieRequest: OMovieRequest,
    private val type: MediaType,
    private val time: SortTime,
    private val filterCategory: FilterCategory?,
    private val filterCountry: FilterCountry?,
    private val year: Int?
) : BaseOMoviePagingSource() {
    override suspend fun apiFetch(page: Int) = oMovieRequest.getMovie(
        page = page,
        type = type,
        time = time,
        filterCategory = filterCategory,
        filterCountry = filterCountry,
        year = year
    )
}

class MyMoviePaging(
    private val oMovieRequest: OMovieRequest
) : PagingSource<Int, MyMovie>() {
    override fun getRefreshKey(state: PagingState<Int, MyMovie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MyMovie> {
        val page = params.key ?: 1
        return when (val result = oMovieRequest.getMyMovies(page)) {
            is NetworkResponse.Error -> LoadResult.Error(result.error)

            is NetworkResponse.Success -> {
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (result.data.hasNext == true) page + 1 else null
                LoadResult.Page(
                    data = result.data.items ?: emptyList(),
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        }
    }
}


