package com.android.movieapp.models.entities

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
@Immutable
@Entity(primaryKeys = ["id"])
data class Person(
  var page: Int?,
  @SerializedName("adult")
  val adult: Boolean?,
  @SerializedName("gender")
  val gender: Int?,
  @SerializedName("id")
  val id: Int?,
  @SerializedName("known_for_department")
  val knownForDepartment: String?,
  @SerializedName("name")
  val name: String?,
  @SerializedName("popularity")
  val popularity: Double?,
  @SerializedName("profile_path")
  val profilePath: String?
): Serializable

@Keep
@Immutable
@Entity(primaryKeys = ["id"])
data class FavoritePerson(
  @SerializedName("adult")
  val adult: Boolean?,
  @SerializedName("gender")
  val gender: Int?,
  @SerializedName("id")
  val id: Int?,
  @SerializedName("known_for_department")
  val knownForDepartment: String?,
  @SerializedName("name")
  val name: String?,
  @SerializedName("popularity")
  val popularity: Double?,
  @SerializedName("profile_path")
  val profilePath: String?
): Serializable
