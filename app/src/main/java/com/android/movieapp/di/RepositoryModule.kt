package com.android.movieapp.di

import com.android.movieapp.db.AppDatabase
import com.android.movieapp.db.CountryDao
import com.android.movieapp.db.HistoryDao
import com.android.movieapp.db.LanguageDao
import com.android.movieapp.db.MovieDao
import com.android.movieapp.db.PersonDao
import com.android.movieapp.db.TvDao
import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.network.service.ConfigureService
import com.android.movieapp.network.service.FilterService
import com.android.movieapp.network.service.MediaRequest
import com.android.movieapp.network.service.MovieService
import com.android.movieapp.network.service.PersonService
import com.android.movieapp.network.service.PopularService
import com.android.movieapp.network.service.SearchService
import com.android.movieapp.network.service.TvService
import com.android.movieapp.repository.ConfigureRepository
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.repository.FilterRepository
import com.android.movieapp.repository.MediaRepository
import com.android.movieapp.repository.MovieRepository
import com.android.movieapp.repository.PersonRepository
import com.android.movieapp.repository.PopularRepository
import com.android.movieapp.repository.TvRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @ViewModelScoped
    fun provideDiscoverRepository(
        discoverService: PopularService,
        searchService: SearchService,
        appDatabase: AppDatabase,
        dataStoreManager: DataStoreManager
    ): PopularRepository {
        return PopularRepository(
            discoverService,
            searchService,
            appDatabase,
            dataStoreManager
        )
    }

    @Provides
    @ViewModelScoped
    fun provideFavoriteRepository(
        movieDao: MovieDao,
        tvDao: TvDao,
        personDao: PersonDao
    ): FavoriteRepository {
        return FavoriteRepository(
            movieDao,
            tvDao,
            personDao
        )
    }

    @Provides
    @ViewModelScoped
    fun provideMovieRepository(
        movieService: MovieService,
        dataStoreManager: DataStoreManager
    ): MovieRepository {
        return MovieRepository(movieService, dataStoreManager)
    }

    @Provides
    @ViewModelScoped
    fun providePeopleRepository(
        personService: PersonService,
        dataStoreManager: DataStoreManager
    ): PersonRepository {
        return PersonRepository(personService, dataStoreManager)
    }

    @Provides
    @ViewModelScoped
    fun provideTvRepository(
        tvService: TvService,
        dataStoreManager: DataStoreManager
    ): TvRepository {
        return TvRepository(tvService, dataStoreManager)
    }

    @Provides
    @ViewModelScoped
    fun provideConfigureRepository(
        configureService: ConfigureService,
        languageDao: LanguageDao,
        countryDao: CountryDao
    ): ConfigureRepository {
        return ConfigureRepository(configureService, languageDao, countryDao)
    }

    @Provides
    @ViewModelScoped
    fun provideFilterRepository(
        filterService: FilterService,
        dataStoreManager: DataStoreManager,
        movieDao: MovieDao,
        tvDao: TvDao,
        personDao: PersonDao
    ): FilterRepository {
        return FilterRepository(filterService, dataStoreManager, movieDao, tvDao, personDao)
    }

    @Provides
    @ViewModelScoped
    fun provideOMovieRepository(
        mediaRequest: MediaRequest,
        historyDao: HistoryDao
    ): MediaRepository {
        return MediaRepository(mediaRequest, historyDao)
    }

}
