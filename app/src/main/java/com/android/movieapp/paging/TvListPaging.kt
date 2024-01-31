package com.android.movieapp.paging

import com.android.movieapp.db.AppDatabase
import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.network.service.FilterService
import com.android.movieapp.network.service.PopularService
import com.android.movieapp.network.service.SearchService
import com.android.movieapp.network.service.SortValue


class SearchTvPagingSource(
    private val searchService: SearchService,
    private val query: String,
    private val dataStoreManager: DataStoreManager
) : BasePagingSource<Tv>() {

    override suspend fun apiFetch(page: Int) =
        searchService.searchTv(page = page, query = query, language = dataStoreManager.language)
}

class FilterTvListPagingSource(
    private val filterService: FilterService,
    private val dataStoreManager: DataStoreManager,
    private val sortValue: SortValue,
    private val withOriginCountry: String,
    private val withOriginLanguage: String,
    private val withGenres: List<Int>,
    private val withKeywords: List<Int>,
    private val years: List<Int>,
    private val withCompanies: List<Int>,
    private val status: String
) : BasePagingSource<Tv>() {

    override suspend fun apiFetch(page: Int) =
        filterService.filterTv(
            page = page,
            language = dataStoreManager.language,
            sortBy = sortValue,
            withOriginCountry = withOriginCountry,
            withOriginLanguage = withOriginLanguage,
            withGenres = withGenres,
            withKeywords = withKeywords,
            years = years,
            withCompanies = withCompanies,
            status = status
        )
}

class TvsRemoteMediator(
    discoverService: PopularService,
    appDatabase: AppDatabase,
    dataStoreManager: DataStoreManager
) : BaseRemoteMediator<Tv>(discoverService, appDatabase, dataStoreManager) {

    override val type: TypeRemoteMediator
        get() = TypeRemoteMediator.Tv
}


