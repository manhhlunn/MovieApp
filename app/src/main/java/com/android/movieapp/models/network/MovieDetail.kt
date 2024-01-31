package com.android.movieapp.models.network


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MovieDetail(
    @SerializedName("genres")
    val genres: List<GenreItemResponse>?,
    @SerializedName("production_companies")
    val productionCompanies: List<ProductionCompany>?,
    @SerializedName("budget")
    val budget: Int?,
    @SerializedName("revenue")
    val revenue: Int?,
    @SerializedName("runtime")
    val runtime: Int?,
    @SerializedName("status")
    val status: String?
)
