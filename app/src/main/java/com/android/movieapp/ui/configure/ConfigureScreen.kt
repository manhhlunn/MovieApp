package com.android.movieapp.ui.configure

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.android.movieapp.R
import com.android.movieapp.ds.DataStoreManager
import com.android.movieapp.models.network.CountryItemResponse
import com.android.movieapp.models.network.LanguageItemResponse
import com.android.movieapp.repository.ConfigureRepository
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
                    modifier = Modifier.padding(horizontal = 12.dp), hint = stringResource(
                        if (isCountryScreen) R.string.search_region else R.string.search_language
                    )
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


@Composable
fun LanguageItemView(
    value: LanguageItemResponse,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable {
                onSelect()
            }) {
        val painter = rememberAsyncImagePainter(
            model = value.iso6391.languageIcon(),
            error = ColorPainter(MaterialTheme.colorScheme.onSecondary)
        )
        Image(
            painter = painter,
            contentDescription = "Language icon",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .width(40.dp)
                .then(
                    (painter.state as? AsyncImagePainter.State.Success)
                        ?.painter
                        ?.intrinsicSize
                        ?.let { intrinsicSize ->
                            val ratio = intrinsicSize.width / intrinsicSize.height
                            Modifier
                                .aspectRatio(ratio)
                                .shadow(4.dp)
                        } ?: Modifier.aspectRatio(2f)
                ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value.englishName + if (value.name.isNullOrEmpty()) "" else " (${value.name})",
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun RegionItemView(
    value: CountryItemResponse,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable {
                onSelect()
            }) {
        val painter = rememberAsyncImagePainter(
            model = value.iso31661.countryIcon(),
            error = ColorPainter(MaterialTheme.colorScheme.onSecondary)
        )
        Image(
            painter = painter,
            contentDescription = "Language icon",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .width(40.dp)
                .then(
                    (painter.state as? AsyncImagePainter.State.Success)
                        ?.painter
                        ?.intrinsicSize
                        ?.let { intrinsicSize ->
                            val ratio = intrinsicSize.width / intrinsicSize.height
                            Modifier
                                .aspectRatio(ratio)
                                .shadow(4.dp)
                        } ?: Modifier.aspectRatio(2f)
                ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value.englishName + if (value.nativeName.isNullOrEmpty()) "" else " (${value.nativeName})",
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    onSearch: (String) -> Unit = {},
) {
    var searchValue by rememberSaveable { mutableStateOf("") }
    var isHintDisplayed by rememberSaveable { mutableStateOf(hint != "") }

    Box(modifier = modifier) {
        BasicTextField(
            value = searchValue,
            onValueChange = {
                searchValue = it
                onSearch.invoke(it)
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            maxLines = 1,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onPrimary)
                .padding(start = 48.dp, top = 16.dp, bottom = 16.dp, end = 24.dp)
                .onFocusChanged {
                    isHintDisplayed = (!it.hasFocus && searchValue.isEmpty())
                },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary)
        )

        Icon(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp),
            painter = rememberVectorPainter(Icons.Default.Search),
            contentDescription = "Search icon"
        )

        if (isHintDisplayed) {
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(
                    start = 48.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                    end = 24.dp
                )
            )
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

fun String.languageIcon() = "https://www.unknown.nu/flags/images/$this-100"
fun String.countryIcon() = "https://flagcdn.com/w80/${this.lowercase()}.png"

data class UIStateConfigureScreen<T>(
    var currentValues: List<T>? = null,
    var currentValue: T? = null,
    var query: String = ""
)
