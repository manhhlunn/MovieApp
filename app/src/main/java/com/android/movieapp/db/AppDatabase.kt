package com.android.movieapp.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.movieapp.db.converters.IntegerListConverter
import com.android.movieapp.db.converters.StringListConverter
import com.android.movieapp.models.entities.FavoriteMovie
import com.android.movieapp.models.entities.FavoritePerson
import com.android.movieapp.models.entities.FavoriteTv
import com.android.movieapp.models.entities.MediaHistory
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.RemoteKeyMovie
import com.android.movieapp.models.entities.RemoteKeyPerson
import com.android.movieapp.models.entities.RemoteKeyTv
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.entities.WatchedMovie
import com.android.movieapp.models.entities.WatchedTv
import com.android.movieapp.models.network.CountryItemResponse
import com.android.movieapp.models.network.LanguageItemResponse

@Database(
    entities = [(Movie::class), (Tv::class), (Person::class),
        (FavoriteMovie::class), (FavoriteTv::class), (FavoritePerson::class),
        (WatchedMovie::class), (WatchedTv::class),
        (RemoteKeyMovie::class), (RemoteKeyTv::class), (RemoteKeyPerson::class),
        (LanguageItemResponse::class), (CountryItemResponse::class),
        (MediaHistory::class)
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    value = [
        (StringListConverter::class), (IntegerListConverter::class)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun tvDao(): TvDao
    abstract fun personDao(): PersonDao
    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun languageDao(): LanguageDao
    abstract fun countryDao(): CountryDao
    abstract fun historyDao(): HistoryDao
}
