package com.android.movieapp.di

import android.content.Context
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
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
import com.android.movieapp.network.service.MovieService
import com.android.movieapp.network.service.OMovieRequest
import com.android.movieapp.network.service.PersonService
import com.android.movieapp.network.service.PopularService
import com.android.movieapp.network.service.SearchService
import com.android.movieapp.network.service.TvService
import com.android.movieapp.repository.ConfigureRepository
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.repository.FilterRepository
import com.android.movieapp.repository.MovieRepository
import com.android.movieapp.repository.OMovieRepository
import com.android.movieapp.repository.PersonRepository
import com.android.movieapp.repository.PopularRepository
import com.android.movieapp.repository.TvRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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
        oMovieRequest: OMovieRequest,
        historyDao: HistoryDao
    ): OMovieRepository {
        return OMovieRepository(oMovieRequest, historyDao)
    }

    @UnstableApi
    @Provides
    @ViewModelScoped
    fun provideExoPlayer(
        @ApplicationContext app: Context
    ): ExoPlayer {

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(32 * 1024, 64 * 1024, 10 * 1024, 10 * 1024)
            .build()

        return ExoPlayer.Builder(app).apply {
            setHandleAudioBecomingNoisy(true)
            setLoadControl(loadControl)
            setWakeMode(WAKE_MODE_NETWORK)
        }.build()
    }
}
