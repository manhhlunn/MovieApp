package com.android.movieapp.di

import android.content.Context
import androidx.room.Room
import com.android.movieapp.db.AppDatabase
import com.android.movieapp.db.CountryDao
import com.android.movieapp.db.HistoryDao
import com.android.movieapp.db.LanguageDao
import com.android.movieapp.db.MovieDao
import com.android.movieapp.db.PersonDao
import com.android.movieapp.db.RemoteKeysDao
import com.android.movieapp.db.TvDao
import com.android.movieapp.ds.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRoomDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(context, AppDatabase::class.java, "MovieCompose.db")
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideMovieDao(appDatabase: AppDatabase): MovieDao {
        return appDatabase.movieDao()
    }

    @Provides
    @Singleton
    fun provideTvDao(appDatabase: AppDatabase): TvDao {
        return appDatabase.tvDao()
    }

    @Provides
    @Singleton
    fun providePersonDao(appDatabase: AppDatabase): PersonDao {
        return appDatabase.personDao()
    }

    @Provides
    @Singleton
    fun provideLanguageDao(appDatabase: AppDatabase): LanguageDao {
        return appDatabase.languageDao()
    }

    @Provides
    @Singleton
    fun provideCountryDao(appDatabase: AppDatabase): CountryDao {
        return appDatabase.countryDao()
    }

    @Provides
    @Singleton
    fun provideRemoteKeysDao(appDatabase: AppDatabase): RemoteKeysDao {
        return appDatabase.remoteKeysDao()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(appDatabase: AppDatabase): HistoryDao {
        return appDatabase.historyDao()
    }

    @Provides
    @Singleton
    fun provideUserDataStorePreferences(
        @ApplicationContext applicationContext: Context
    ): DataStoreManager {
        return DataStoreManager(applicationContext)
    }
}
