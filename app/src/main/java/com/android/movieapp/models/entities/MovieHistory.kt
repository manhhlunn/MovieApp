package com.android.movieapp.models.entities

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Keep
@Immutable
@Entity
data class MovieHistory(
    @PrimaryKey
    var slug: String,
    var serverName: String?,
    var index: Int,
    var position: Long
) : Serializable
