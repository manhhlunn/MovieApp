package com.android.movieapp.di

import com.android.movieapp.usecase.FilterUseCase
import com.android.movieapp.usecase.FilterUseCaseImpl
import com.android.movieapp.usecase.PopularUseCase
import com.android.movieapp.usecase.PopularUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelModule {

    @Binds
    @ViewModelScoped
    fun bindPopularUseCase(impl: PopularUseCaseImpl): PopularUseCase

    @Binds
    @ViewModelScoped
    fun bindFilterUseCase(impl: FilterUseCaseImpl): FilterUseCase

}