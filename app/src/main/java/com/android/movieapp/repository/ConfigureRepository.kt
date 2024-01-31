package com.android.movieapp.repository

import com.android.movieapp.db.CountryDao
import com.android.movieapp.db.LanguageDao
import com.android.movieapp.models.network.CountryItemResponse
import com.android.movieapp.models.network.LanguageItemResponse
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.network.service.ConfigureService

class ConfigureRepository(
    private val configureService: ConfigureService,
    private val languageDao: LanguageDao,
    private val countryDao: CountryDao
) : Repository {

    suspend fun getLanguages(): List<LanguageItemResponse> {
        return if (languageDao.isEmpty()) when (val value = configureService.languages()) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> {
                languageDao.insertAll(value.data)
                value.data
            }
        }
        else languageDao.getAll()
    }

    suspend fun getCountries(): List<CountryItemResponse> {
        return if (countryDao.isEmpty()) when (val value = configureService.countries()) {
            is NetworkResponse.Error -> emptyList()
            is NetworkResponse.Success -> {
                countryDao.insertAll(value.data)
                value.data
            }
        }
        else countryDao.getAll()
    }
}
