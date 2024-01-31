package com.android.movieapp.models.network


import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Keep
@Entity
data class CountryItemResponse(
    @PrimaryKey
    @SerializedName("english_name")
    val englishName: String,
    @SerializedName("iso_3166_1")
    val iso31661: String,
    @SerializedName("native_name")
    val nativeName: String?
)
