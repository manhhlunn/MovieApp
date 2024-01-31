package com.android.movieapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.network.LanguageItemResponse

@Dao
interface LanguageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(values: List<LanguageItemResponse>)

    @Query("SELECT (SELECT COUNT(*) FROM LanguageItemResponse) == 0")
    suspend fun isEmpty(): Boolean

    @Query("Select * From LanguageItemResponse")
    suspend fun getAll(): List<LanguageItemResponse>

}
