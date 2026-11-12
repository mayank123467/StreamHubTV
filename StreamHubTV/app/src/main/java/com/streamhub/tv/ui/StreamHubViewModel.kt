package com.streamhub.tv.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.streamhub.tv.data.ContentRepository
import com.streamhub.tv.model.MediaItem
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HubUiState(
    val isLoading: Boolean = true,
    val hasApiKeys: Boolean = false,
    val latest: List<MediaItem> = emptyList(),
    val mostRated: List<MediaItem> = emptyList(),
    val forYou: List<MediaItem> = emptyList(),
    val youtubeTrending: List<MediaItem> = emptyList(),
    val selectedGenres: Set<Int> = emptySet(),
    val selectedItem: MediaItem? = null,
    val showInterestPicker: Boolean = false
)

class StreamHubViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ContentRepository(app)
    private val _state = MutableStateFlow(HubUiState(selectedGenres = repo.getSelectedGenres(), hasApiKeys = repo.hasApiKeys()))
    val state: StateFlow<HubUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val latestDeferred = async { repo.fetchLatestOtt() }
            val ratedDeferred = async { repo.fetchMostRatedOtt() }
            val ytDeferred = async { repo.fetchYouTubeTrending() }

            val latest = latestDeferred.await()
            val rated = ratedDeferred.await()
            val yt = ytDeferred.await()
            val genres = _state.value.selectedGenres

            val combined = (latest + rated).distinctBy { it.id }
            val interested = combined
                .sortedByDescending { item -> item.genreIds.count { it in genres } * 10 + item.rating }
                .take(15)

            _state.value = _state.value.copy(
                isLoading = false,
                latest = latest,
                mostRated = rated,
                forYou = interested,
                youtubeTrending = yt
            )
        }
    }

    fun toggleGenre(genreId: Int) {
        val updated = repo.toggleGenre(genreId)
        val combined = (_state.value.latest + _state.value.mostRated).distinctBy { it.id }
        val interested = combined
            .sortedByDescending { item -> item.genreIds.count { it in updated } * 10 + item.rating }
            .take(15)
        _state.value = _state.value.copy(selectedGenres = updated, forYou = interested)
    }

    fun selectItem(item: MediaItem?) {
        _state.value = _state.value.copy(selectedItem = item)
    }

    fun toggleInterestPicker(show: Boolean) {
        _state.value = _state.value.copy(showInterestPicker = show)
    }
}
