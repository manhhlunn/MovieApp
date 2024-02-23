package com.android.movieapp.network.service

import com.android.movieapp.models.network.CustomException
import com.android.movieapp.models.network.NetworkResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

typealias Parameters = HashMap<String, Any>

interface NetworkService {

    @GET
    suspend fun <T> get(
        @Url url: String,
        @QueryMap parameters: Parameters
    ): Response<T>

    @POST
    suspend fun <T> post(
        @Url url: String,
        @Body parameters: Parameters
    ): Response<T>
}

interface MediaService {
    @GET
    suspend fun <T> get(
        @Url url: String,
        @QueryMap parameters: Parameters
    ): Response<T>

    @GET
    suspend fun getResponse(
        @Url url: String
    ): Response<ResponseBody>

    @POST
    @FormUrlEncoded
    suspend fun post(
        @Url url: String,
        @FieldMap parameters: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}


suspend inline fun <reified T> MediaService.request(
    url: String,
    parameters: Parameters = hashMapOf()
): NetworkResponse<T> {
    return try {
        val filter: Parameters = hashMapOf()
        parameters.forEach { (s, any) ->
            if (any is String && any.isEmpty()) return@forEach
            else if (any is Int && any == -1) return@forEach
            else filter[s] = any
        }
        val response = get<T>(url, filter)
        val body: T? = fromJson(Gson().toJson(response.body()))
        if (response.isSuccessful && body != null) {
            NetworkResponse.Success(body)
        } else {
            val ex = HttpException(response)
            NetworkResponse.Error(CustomException.RequestFail(ex))
        }
    } catch (e: HttpException) {
        NetworkResponse.Error(CustomException.RequestFail(e))
    } catch (e: Exception) {
        NetworkResponse.Error(CustomException.Normal(e))
    }
}

suspend inline fun <reified T> NetworkService.request(
    type: HTTPMethod = HTTPMethod.GET,
    url: String,
    parameters: Parameters = hashMapOf()
): NetworkResponse<T> {
    return try {
        val filter: Parameters = hashMapOf()
        parameters.forEach { (s, any) ->
            if (any is String && any.isEmpty()) return@forEach
            else if (any is Int && any == -1) return@forEach
            else filter[s] = any
        }
        val response = when (type) {
            HTTPMethod.GET -> get(url, filter)
            HTTPMethod.POST -> post<T>(url, filter)
        }
        val body: T? = fromJson(Gson().toJson(response.body()))
        if (response.isSuccessful && body != null) {
            NetworkResponse.Success(body)
        } else {
            val ex = HttpException(response)
            NetworkResponse.Error(CustomException.RequestFail(ex))
        }
    } catch (e: HttpException) {
        NetworkResponse.Error(CustomException.RequestFail(e))
    } catch (e: Exception) {
        NetworkResponse.Error(CustomException.Normal(e))
    }
}

enum class HTTPMethod {
    GET, POST
}

inline fun <reified T> fromJson(json: String): T? {
    return Gson().fromJson(json, object : TypeToken<T>() {}.type)
}
