package com.android.movieapp.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.entities.FavoriteMovie
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.WatchedMovie

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<Movie>)

    @Query("Select * From Movie Order By page")
    fun getMovies(): PagingSource<Int, Movie>

    @Query("Delete From Movie")
    suspend fun clearAllMovies()

    @Query("SELECT EXISTS(SELECT * FROM FavoriteMovie WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: FavoriteMovie)

    @Query("Delete From FavoriteMovie WHERE id = :id")
    suspend fun deleteFavorite(id: Int)

    @Query("Select * From FavoriteMovie")
    fun getFavorites(): PagingSource<Int, FavoriteMovie>

    @Query("SELECT EXISTS(SELECT * FROM WatchedMovie WHERE id = :id)")
    suspend fun isWatched(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatched(movie: WatchedMovie)

    @Query("Delete From WatchedMovie WHERE id = :id")
    suspend fun deleteWatched(id: Int)

    @Query("Select * From WatchedMovie")
    fun getWatched(): PagingSource<Int, WatchedMovie>

    @Query("Select * From WatchedMovie")
    fun getAllWatched(): List<WatchedMovie>
}
