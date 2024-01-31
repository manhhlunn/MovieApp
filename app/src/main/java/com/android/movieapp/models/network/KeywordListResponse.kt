package com.android.movieapp.models.network

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable

@Keep
@Immutable
data class KeywordListResponse(
  val id: Int,
  val keywords: List<Keyword>
)

@Keep
@Immutable
data class KeywordResultResponse(
  val id: Int,
  val results: List<Keyword>
)

@Keep
@Immutable
data class Keyword(
  val id: Int?,
  val name: String?
)

