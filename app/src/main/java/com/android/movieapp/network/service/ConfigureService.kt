package com.android.movieapp.network.service

import com.android.movieapp.models.network.CountryItemResponse
import com.android.movieapp.models.network.LanguageItemResponse

class ConfigureService(private val networkService: NetworkService) {

    suspend fun countries() =
        networkService.request<List<CountryItemResponse>>(url = "/3/configuration/countries")

    suspend fun languages() =
        networkService.request<List<LanguageItemResponse>>(url = "/3/configuration/languages")

}


