package com.android.movieapp.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.android.movieapp.db.HistoryDao
import com.android.movieapp.models.entities.MediaHistory
import com.android.movieapp.models.network.HomePageData
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.SuperStreamSearchItem.Companion.hasContent
import com.android.movieapp.network.Api
import com.android.movieapp.network.service.MediaRequest
import com.android.movieapp.paging.OMoviePaging
import com.android.movieapp.paging.SearchOMoviePaging
import com.android.movieapp.paging.SuperStreamMoviePaging
import com.android.movieapp.paging.SuperStreamSearchMoviePaging
import com.android.movieapp.ui.media.FilterCategory
import com.android.movieapp.ui.media.FilterCountry
import com.android.movieapp.ui.media.OMovieType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class MediaRepository(
    private val mediaRequest: MediaRequest,
    private val historyDao: HistoryDao
) {

    suspend fun insertHistory(value: MediaHistory) {
        historyDao.insert(value)
    }

    suspend fun getMediaHistory(id: String?): MediaHistory? {
        return if (id == null) null else historyDao.getMediaHistory(id)
    }

    fun searchOMovies(
        query: String,
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
                mediaRequest,
                query,
                filterCategory,
                filterCountry,
                year
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)


    fun getOMovies(
        type: OMovieType,
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
                mediaRequest,
                type,
                filterCategory,
                filterCountry,
                year
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)

    fun getSuperStreamMovies(type: String?) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            SuperStreamMoviePaging(
                mediaRequest,
                type
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)


    suspend fun getSuperStreamHomePageMovies(): Pair<String?, List<HomePageData>> {
        return when (val data = mediaRequest.getSuperStream(1, 10)) {
            is NetworkResponse.Error -> null to emptyList()
            is NetworkResponse.Success -> {
                val values = data.data.data.filter {
                    !it.list.isNullOrEmpty() && it.list.any { item -> item.hasContent() }
                }
                return values.firstOrNull()?.type to values
            }
        }
    }


    fun searchSuperStreamMovies(query: String) = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            SuperStreamSearchMoviePaging(
                mediaRequest,
                query
            )
        }
    )
        .flow
        .flowOn(Dispatchers.IO)


    suspend fun getOMovieDetail(slug: String) = mediaRequest.getOMovieDetail(slug)
    suspend fun getOMovieDetail2(slug: String) = mediaRequest.getOMovieDetail2(slug)
    suspend fun getSuperStreamMovieDetail(id: String) = mediaRequest.getSuperStreamMovieDetail(id)
    suspend fun getSuperStreamTvShowDetail(id: String) = mediaRequest.getSuperStreamTvShowDetail(id)
    suspend fun getSourceLinksSuperStream(
        id: Int,
        season: Int?,
        episode: Int?,
        mediaId: Int?,
        imdbId: String?
    ) = mediaRequest.getSourceLinksSuperStream(id, season, episode, mediaId, imdbId)
}
