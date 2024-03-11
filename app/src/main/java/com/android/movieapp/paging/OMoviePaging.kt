package com.android.movieapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.movieapp.MovieApp
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.OMovieResponse
import com.android.movieapp.models.network.SearchResultItem
import com.android.movieapp.models.network.SuperStreamSearchItem.Companion.toSearchResultItem
import com.android.movieapp.network.service.MediaRequest
import com.android.movieapp.ui.media.FilterCategory
import com.android.movieapp.ui.media.FilterCountry
import com.android.movieapp.ui.media.OMovieType
import com.android.movieapp.ui.media.util.MediaType
import kotlin.math.ceil


abstract class BaseOMoviePagingSource : PagingSource<Int, SearchResultItem>() {

    override fun getRefreshKey(state: PagingState<Int, SearchResultItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResultItem> {
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
                    data = result.data.pageProps?.data?.items?.map { SearchResultItem(
                        id = it.slug,
                        title = it.name,
                        image = "${MovieApp.baseImageUrl}${it.thumbUrl}",
                        quality = it.quality
                    ) } ?: emptyList(),
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        }
    }

    abstract suspend fun apiFetch(page: Int): NetworkResponse<OMovieResponse>

}

class SearchOMoviePaging(
    private val mediaRequest: MediaRequest,
    private val query: String,
    private val filterCategory: FilterCategory?,
    private val filterCountry: FilterCountry?,
    private val year: Int?
) : BaseOMoviePagingSource() {
    override suspend fun apiFetch(page: Int) = mediaRequest.searchOMovie(
        page = page,
        query = query,
        filterCategory = filterCategory,
        filterCountry = filterCountry,
        year = year
    )
}

class OMoviePaging(
    private val mediaRequest: MediaRequest,
    private val type: OMovieType,
    private val filterCategory: FilterCategory?,
    private val filterCountry: FilterCountry?,
    private val year: Int?
) : BaseOMoviePagingSource() {
    override suspend fun apiFetch(page: Int) = mediaRequest.getOMovie(
        page = page,
        type = type,
        filterCategory = filterCategory,
        filterCountry = filterCountry,
        year = year
    )
}

class SuperStreamMoviePaging(
    private val mediaRequest: MediaRequest,
    private val type: String?
) : PagingSource<Int, SearchResultItem>() {
    override fun getRefreshKey(state: PagingState<Int, SearchResultItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResultItem> {
        val page = params.key ?: 1
        return when (val result = mediaRequest.getSuperStream(page, 20)) {
            is NetworkResponse.Error -> LoadResult.Error(result.error)

            is NetworkResponse.Success -> {
                val type = result.data.data.firstOrNull { type == it.type }
                if (type != null) {
                    val prevKey = if (page == 1) null else page - 1
                    val nextKey = if (type.isMore == 1) page + 1 else null
                    LoadResult.Page(
                        data = type.list?.map { it.toSearchResultItem() } ?: emptyList(),
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                } else {
                    LoadResult.Page(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = null
                    )
                }
            }
        }
    }
}

class SuperStreamSearchMoviePaging(
    private val mediaRequest: MediaRequest,
    private val query: String
) : PagingSource<Int, SearchResultItem>() {
    override fun getRefreshKey(state: PagingState<Int, SearchResultItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResultItem> {
        val page = params.key ?: 1
        return when (val result = mediaRequest.searchSuperStream(query, page, 20)) {
            is NetworkResponse.Error -> LoadResult.Error(result.error)

            is NetworkResponse.Success -> {
                val mappedItems = result.data.data.results.map { it.toSearchResultItem() }
                val hasNext = (page * 20) < result.data.data.total
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (hasNext) page + 1 else null
                LoadResult.Page(
                    data = mappedItems,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        }
    }
}


