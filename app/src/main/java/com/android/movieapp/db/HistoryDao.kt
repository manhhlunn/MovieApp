package com.android.movieapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.entities.MediaHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: MediaHistory)

    @Query("SELECT * FROM MediaHistory WHERE id = :id ORDER BY time DESC")
    suspend fun getMediaHistory(id: String): MediaHistory?

    @Query("SELECT * FROM MediaHistory ORDER BY time DESC")
    fun getAll(): Flow<List<MediaHistory>>

    @Query("DELETE FROM MediaHistory WHERE id = :id")
    suspend fun deleteMediaHistory(id: String)
}
