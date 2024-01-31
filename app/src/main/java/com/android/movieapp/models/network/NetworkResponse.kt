package com.android.movieapp.models.network


import androidx.annotation.Keep
import retrofit2.HttpException

@Keep
sealed class NetworkResponse<out R> {
    data class Success<R>(val data: R) : NetworkResponse<R>()
    data class Error(val error: CustomException) : NetworkResponse<Nothing>()
}

@Keep
sealed class CustomException(e: Exception) : Exception(e) {
    data class RequestFail(val ex: HttpException) : CustomException(ex)
    data class Normal(val ex: Exception) : CustomException(ex)
}
