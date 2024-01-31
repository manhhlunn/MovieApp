package com.android.movieapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.entities.MovieHistory

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: MovieHistory)

    @Query("SELECT * FROM MovieHistory WHERE slug = :slug")
    suspend fun getMovieHistory(slug: String): MovieHistory?
}
