package com.android.movieapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.entities.MediaHistory

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: MediaHistory)

    @Query("SELECT * FROM MediaHistory WHERE id = :id")
    suspend fun getMediaHistory(id: String): MediaHistory?
}
