package com.android.movieapp.models.entities

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.movieapp.models.network.SearchResultItem
import java.io.Serializable

@Keep
@Immutable
@Entity
data class MediaHistory(
    @PrimaryKey
    val id: String,
    val serverIdx: Int,
    val index: Int,
    val position: Long,
    val data: SearchResultItem?,
    @ColumnInfo(defaultValue = "0")
    val time: Long = System.currentTimeMillis()
) : Serializable
