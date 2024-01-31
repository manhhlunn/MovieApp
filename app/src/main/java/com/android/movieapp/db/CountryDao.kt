package com.android.movieapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.network.CountryItemResponse

@Dao
interface CountryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(values: List<CountryItemResponse>)

    @Query("SELECT (SELECT COUNT(*) FROM CountryItemResponse) == 0")
    suspend fun isEmpty(): Boolean

    @Query("Select * From CountryItemResponse")
    suspend fun getAll(): List<CountryItemResponse>

}
