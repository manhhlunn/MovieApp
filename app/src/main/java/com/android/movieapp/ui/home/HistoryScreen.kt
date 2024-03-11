package com.android.movieapp.ui.home

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.android.movieapp.NavScreen
import com.android.movieapp.db.HistoryDao
import com.android.movieapp.models.entities.MediaHistory
import com.android.movieapp.ui.home.widget.MovieHistoryItemView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun HistoryScreen(navController: NavController, viewModel: HistoryViewModel = hiltViewModel()) {
    val values by viewModel.values.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .fillMaxSize()
    ) {
        items(values.size, key = { values[it] }) {
            val item = values[it]
            MovieHistoryItemView(history = item, onExpandDetails = {
                if (item.data?.filmType == null) {
                    navController.navigate(
                        NavScreen.OMovieDetailScreen.navigateWithArgument(
                            item.data ?: return@MovieHistoryItemView
                        )
                    )
                } else {
                    navController.navigate(
                        NavScreen.SuperStreamMovieDetailScreen.navigateWithArgument(
                            item.data
                        )
                    )
                }
            }, onRemove = {
                viewModel.deleteMediaHistory(item)
            })
        }
    }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(private val historyDao: HistoryDao) : ViewModel() {

    fun deleteMediaHistory(item: MediaHistory) {
        viewModelScope.launch {
            historyDao.deleteMediaHistory(item.id)
        }
    }

    val values =
        historyDao.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}