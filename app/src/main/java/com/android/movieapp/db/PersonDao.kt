package com.android.movieapp.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.entities.FavoritePerson
import com.android.movieapp.models.entities.Person


@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(persons: List<Person>)

    @Query("Select * From Person Order By page")
    fun getPersons(): PagingSource<Int, Person>

    @Query("Delete From Person")
    suspend fun clearAllPersons()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(person: FavoritePerson)

    @Query("SELECT EXISTS(SELECT * FROM FavoritePerson WHERE id = :id)")
    suspend fun isExist(id: Int): Boolean

    @Query("Delete From FavoritePerson WHERE id = :id")
    suspend fun deleteFavorite(id: Int)

    @Query("Select * From FavoritePerson")
    fun getFavorites(): PagingSource<Int, FavoritePerson>

    @Query("Select * From FavoritePerson")
    fun getAllFavorites(): List<FavoritePerson>
}
