package com.android.movieapp.paging

import com.android.movieapp.db.AppDatabase
import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.models.entities.Person
import com.android.movieapp.network.service.PopularService
import com.android.movieapp.network.service.SearchService


class PersonsRemoteMediator(
    discoverService: PopularService,
    appDatabase: AppDatabase,
    dataStoreManager: DataStoreManager
) : BaseRemoteMediator<Person>(discoverService, appDatabase, dataStoreManager) {

    override val type: TypeRemoteMediator
        get() = TypeRemoteMediator.Person
}

class SearchPersonPagingSource(
    private val searchService: SearchService,
    private val query: String,
    private val dataStoreManager: DataStoreManager
) : BasePagingSource<Person>() {

    override suspend fun apiFetch(page: Int) =
        searchService.searchPerson(page = page, query = query, language = dataStoreManager.language)
}
