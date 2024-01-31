package com.android.movieapp.models.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

abstract class RemoteKeys {
    abstract val prevKey: Int?
    abstract val nextKey: Int?
}

@Keep
@Entity(tableName = "remote_key_movie")
data class RemoteKeyMovie(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "movie_id")
    val movieID: Int?,
    override val prevKey: Int?,
    val currentPage: Int,
    override val nextKey: Int?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) : RemoteKeys()

@Keep
@Entity(tableName = "remote_key_tv")
data class RemoteKeyTv(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "tv_id")
    val tvID: Int?,
    override val prevKey: Int?,
    val currentPage: Int,
    override val nextKey: Int?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) : RemoteKeys()

@Keep
@Entity(tableName = "remote_key_person")
data class RemoteKeyPerson(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "person_id")
    val personId: Int?,
    override val prevKey: Int?,
    val currentPage: Int,
    override val nextKey: Int?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) : RemoteKeys()