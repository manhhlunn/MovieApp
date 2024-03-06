package com.android.movieapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.android.movieapp.models.entities.Person
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.usecase.PopularUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class PersonScreenViewModel @Inject constructor(private val popularUseCase: PopularUseCase) :
    BaseSearchViewModel<Person>() {

    override val invoke: (String) -> Flow<PagingData<Person>> = {
        popularUseCase.invokePerson(it)
    }
}

@HiltViewModel
class FavoritePersonViewModel @Inject constructor(favoriteRepository: FavoriteRepository) :
    ViewModel() {

    val favoritePerson = favoriteRepository.favoritePersons()
}


