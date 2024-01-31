package com.android.movieapp.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.entities.FavoriteTv
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.entities.WatchedTv

@Dao
interface TvDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tvs: List<Tv>)

    @Query("Select * From Tv Order By page")
    fun getTvs(): PagingSource<Int, Tv>

    @Query("Delete From Tv")
    suspend fun clearAllTvs()

    @Query("SELECT EXISTS(SELECT * FROM FavoriteTv WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: FavoriteTv)

    @Query("Delete From FavoriteTv WHERE id = :id")
    suspend fun deleteFavorite(id: Int)

    @Query("Select * From FavoriteTv")
    fun getFavorites(): PagingSource<Int, FavoriteTv>

    @Query("SELECT EXISTS(SELECT * FROM WatchedTv WHERE id = :id)")
    suspend fun isWatched(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatched(tv: WatchedTv)

    @Query("Delete From WatchedTv WHERE id = :id")
    suspend fun deleteWatched(id: Int)

    @Query("Select * From WatchedTv")
    fun getWatched(): PagingSource<Int, WatchedTv>

    @Query("Select * From WatchedTv")
    fun getAllWatched(): List<WatchedTv>
}
