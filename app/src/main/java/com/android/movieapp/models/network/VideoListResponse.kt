package com.android.movieapp.models.network

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable

@Keep
@Immutable
data class VideoListResponse(
  val id: Int,
  val results: List<Video>
)

@Keep
@Immutable
data class Video(
  val id: String?,
  val name: String?,
  val site: String?,
  val key: String?,
  val size: Int?,
  val type: String?
)
