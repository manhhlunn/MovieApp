package com.android.movieapp.di

import com.android.movieapp.network.Api
import com.android.movieapp.network.service.ConfigureService
import com.android.movieapp.network.service.FilterService
import com.android.movieapp.network.service.MediaRequest
import com.android.movieapp.network.service.MediaService
import com.android.movieapp.network.service.MovieService
import com.android.movieapp.network.service.NetworkService
import com.android.movieapp.network.service.PersonService
import com.android.movieapp.network.service.PopularService
import com.android.movieapp.network.service.SearchService
import com.android.movieapp.network.service.TvService
import com.android.movieapp.ui.ext.ignoreAllSSLErrors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

interface RetrofitQualifier {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class TMDBRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class TMDBOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MovieRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MovieOkHttpClient

}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    @RetrofitQualifier.TMDBOkHttpClient
    fun provideTMDBOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(httpLoggingInterceptor)
        addInterceptor(RequestInterceptor())
        connectTimeout(25, TimeUnit.SECONDS)
        callTimeout(25, TimeUnit.SECONDS)
        ignoreAllSSLErrors()
    }.build()

    @Singleton
    @Provides
    @RetrofitQualifier.MovieOkHttpClient
    fun provideMovieOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(httpLoggingInterceptor)
        connectTimeout(25, TimeUnit.SECONDS)
        callTimeout(25, TimeUnit.SECONDS)
        ignoreAllSSLErrors()
    }.build()

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return interceptor
    }

    @Provides
    @Singleton
    @RetrofitQualifier.TMDBRetrofit
    fun provideTMDBRetrofit(@RetrofitQualifier.TMDBOkHttpClient okhHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okhHttpClient)
            .baseUrl(Api.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @RetrofitQualifier.MovieRetrofit
    fun provideOPMovieRetrofit(@RetrofitQualifier.MovieOkHttpClient okhHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okhHttpClient)
            .baseUrl(Api.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkService(@RetrofitQualifier.TMDBRetrofit retrofit: Retrofit): NetworkService {
        return retrofit.create(NetworkService::class.java)
    }

    @Provides
    @Singleton
    fun provideOMovieService(@RetrofitQualifier.MovieRetrofit retrofit: Retrofit): MediaService {
        return retrofit.create(MediaService::class.java)
    }


    @Provides
    @Singleton
    fun provideTheDiscoverService(networkService: NetworkService): PopularService {
        return PopularService(networkService)
    }

    @Provides
    @Singleton
    fun provideMovieService(networkService: NetworkService): MovieService {
        return MovieService(networkService)
    }

    @Provides
    @Singleton
    fun provideTvService(networkService: NetworkService): TvService {
        return TvService(networkService)
    }

    @Provides
    @Singleton
    fun providePeopleService(networkService: NetworkService): PersonService {
        return PersonService(networkService)
    }

    @Provides
    @Singleton
    fun provideSearchService(networkService: NetworkService): SearchService {
        return SearchService(networkService)
    }

    @Provides
    @Singleton
    fun provideConfigureService(networkService: NetworkService): ConfigureService {
        return ConfigureService(networkService)
    }

    @Provides
    @Singleton
    fun provideFilterService(networkService: NetworkService): FilterService {
        return FilterService(networkService)
    }

    @Provides
    @Singleton
    fun provideOMovieRequest(mediaService: MediaService): MediaRequest {
        return MediaRequest(mediaService)
    }
}


class RequestInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val url = originalUrl.newBuilder()
            .addQueryParameter("api_key", Api.API_KEY)
            .build()

        val requestBuilder = originalRequest.newBuilder().url(url)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}





