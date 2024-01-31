package com.android.movieapp.models.network


import androidx.annotation.Keep
import com.android.movieapp.models.entities.Person
import com.google.gson.annotations.SerializedName

@Keep
data class ListCreditsResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("cast")
    val cast: List<Cast>?,
    @SerializedName("crew")
    val crew: List<Crew>?
)

@Keep
data class Cast(
    @SerializedName("adult")
    val adult: Boolean?,
    @SerializedName("credit_id")
    val creditId: String?,
    @SerializedName("gender")
    val gender: Int?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("known_for_department")
    val knownForDepartment: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("original_name")
    val originalName: String?,
    @SerializedName("popularity")
    val popularity: Double?,
    @SerializedName("profile_path")
    val profilePath: String?,
    @SerializedName("cast_id")
    val castId: Int?,
    @SerializedName("character")
    val character: String?,
    @SerializedName("order")
    val order: Int?,
)

fun Cast.toPerson(): Person {
    return Person(
        page = null,
        adult = adult,
        gender = gender,
        id = id,
        knownForDepartment = knownForDepartment,
        name = name,
        popularity = popularity,
        profilePath = profilePath,
    )
}

fun Crew.toPerson(): Person {
    return Person(
        page = null,
        adult = adult,
        gender = gender,
        id = id,
        knownForDepartment = knownForDepartment,
        name = name,
        popularity = popularity,
        profilePath = profilePath,
    )
}

@Keep
data class Crew(
    @SerializedName("adult")
    val adult: Boolean?,
    @SerializedName("credit_id")
    val creditId: String?,
    @SerializedName("department")
    val department: String?,
    @SerializedName("gender")
    val gender: Int?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("job")
    val job: String?,
    @SerializedName("known_for_department")
    val knownForDepartment: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("original_name")
    val originalName: String?,
    @SerializedName("popularity")
    val popularity: Double?,
    @SerializedName("profile_path")
    val profilePath: String?,
)
