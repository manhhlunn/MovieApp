package com.android.movieapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieapp.models.entities.RemoteKeyMovie
import com.android.movieapp.models.entities.RemoteKeyPerson
import com.android.movieapp.models.entities.RemoteKeyTv


@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeyPerson(remoteKey: List<RemoteKeyPerson>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeyMovie(remoteKey: List<RemoteKeyMovie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeyTv(remoteKey: List<RemoteKeyTv>)

    @Query("Select * From remote_key_movie Where movie_id = :id")
    suspend fun getRemoteKeyByMovieID(id: Int?): RemoteKeyMovie?

    @Query("Select * From remote_key_tv Where tv_id = :id")
    suspend fun getRemoteKeyByTvID(id: Int?): RemoteKeyTv?

    @Query("Select * From remote_key_person Where person_id = :id")
    suspend fun getRemoteKeyByPersonID(id: Int?): RemoteKeyPerson?

    @Query("Delete From remote_key_movie")
    suspend fun clearRemoteKeyMovie()

    @Query("Delete From remote_key_tv")
    suspend fun clearRemoteKeyTv()

    @Query("Delete From remote_key_person")
    suspend fun clearRemoteKeyPerson()

    @Query("Select created_at From remote_key_movie Order By created_at DESC LIMIT 1")
    suspend fun getCreationTimeMovie(): Long?

    @Query("Select created_at From remote_key_tv Order By created_at DESC LIMIT 1")
    suspend fun getCreationTimeTv(): Long?

    @Query("Select created_at From remote_key_person Order By created_at DESC LIMIT 1")
    suspend fun getCreationTimePerson(): Long?
}