package com.android.movieapp.ui.configure

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.android.movieapp.R
import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.models.network.CountryItemResponse
import com.android.movieapp.models.network.LanguageItemResponse
import com.android.movieapp.repository.ConfigureRepository
import com.android.movieapp.ui.ext.SearchBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun ConfigureScreen(
    viewModel: ConfigureViewModel<*>
) {
    val isCountryScreen = viewModel is CountryViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentValues = uiState.currentValues
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (currentValues != null) {
            Column {
                Text(
                    text = stringResource(if (isCountryScreen) R.string.selected_region else R.string.selected_language),
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp, start = 12.dp, end = 12.dp)
                )
                uiState.currentValue?.let {
                    when (it) {
                        is LanguageItemResponse -> LanguageItemView(it) {}
                        is CountryItemResponse -> RegionItemView(it) {}
                    }
                }
                SearchBar(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    hint = stringResource(if (isCountryScreen) R.string.search_region else R.string.search_language),
                    value = uiState.query
                ) {
                    viewModel.onQuery(it)
                }
                val filter = currentValues.filter {
                    when (it) {
                        is LanguageItemResponse -> it.name?.contains(uiState.query) ?: true || it.englishName.contains(
                            uiState.query
                        )

                        is CountryItemResponse -> it.nativeName?.contains(uiState.query) ?: true || it.englishName.contains(
                            uiState.query
                        )

                        else -> true
                    }
                }
                LazyColumn {
                    items(filter.size) {
                        when (val item = filter[it]) {
                            is LanguageItemResponse -> LanguageItemView(item) {
                                when (viewModel) {
                                    is LanguageViewModel -> viewModel.setValue(item)
                                }
                            }

                            is CountryItemResponse -> RegionItemView(item) {
                                when (viewModel) {
                                    is CountryViewModel -> viewModel.setValue(item)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}


abstract class ConfigureViewModel<T : Any>(
    private val configureRepository: ConfigureRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    protected val mutableUiState = MutableStateFlow(UIStateConfigureScreen<T>())
    val uiState: StateFlow<UIStateConfigureScreen<T>> = mutableUiState

    fun setValue(value: T) {
        when (value) {
            is CountryItemResponse -> {
                dataStoreManager.region = value.iso31661
            }

            is LanguageItemResponse -> {
                dataStoreManager.language = value.iso6391
            }
        }
        mutableUiState.update {
            it.copy(
                currentValue = value as T?
            )
        }
    }

    fun onQuery(query: String) {
        mutableUiState.update {
            it.copy(
                query = query
            )
        }
    }
}

@HiltViewModel
class LanguageViewModel @Inject constructor(
    configureRepository: ConfigureRepository,
    dataStoreManager: DataStoreManager
) : ConfigureViewModel<LanguageItemResponse>(configureRepository, dataStoreManager) {


    init {
        viewModelScope.launch {
            val languages = configureRepository.getLanguages()
            mutableUiState.update { state ->
                state.copy(
                    currentValues = languages,
                    currentValue = languages.firstOrNull { it.iso6391 == dataStoreManager.language }
                )
            }
        }
    }
}

@HiltViewModel
class CountryViewModel @Inject constructor(
    configureRepository: ConfigureRepository,
    dataStoreManager: DataStoreManager
) : ConfigureViewModel<CountryItemResponse>(configureRepository, dataStoreManager) {

    init {
        viewModelScope.launch {
            val countries = configureRepository.getCountries()
            mutableUiState.update { state ->
                state.copy(
                    currentValues = countries,
                    currentValue = countries.firstOrNull { it.iso31661 == dataStoreManager.region }
                )
            }
        }
    }
}


data class UIStateConfigureScreen<T>(
    var currentValues: List<T>? = null,
    var currentValue: T? = null,
    var query: String = ""
)
