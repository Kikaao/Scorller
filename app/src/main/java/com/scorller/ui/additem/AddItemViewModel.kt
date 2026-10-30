package com.scorller.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scorller.data.linkpreview.LinkPreview
import com.scorller.data.linkpreview.LinkPreviewFetcher
import com.scorller.data.local.LocalIdentityManager
import com.scorller.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddItemUiState {
    data object Idle : AddItemUiState()
    data object Saving : AddItemUiState()
    data class Error(val message: String) : AddItemUiState()
    data object Saved : AddItemUiState()
}

sealed class PreviewFetchState {
    data object Idle : PreviewFetchState()
    data object Loading : PreviewFetchState()
    data object NotFound : PreviewFetchState()
    data class Found(val preview: LinkPreview) : PreviewFetchState()
}

class AddItemViewModel(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager,
    private val linkPreviewFetcher: LinkPreviewFetcher,
    private val sessionId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddItemUiState>(AddItemUiState.Idle)
    val uiState: StateFlow<AddItemUiState> = _uiState

    private val _previewState = MutableStateFlow<PreviewFetchState>(PreviewFetchState.Idle)
    val previewState: StateFlow<PreviewFetchState> = _previewState

    fun fetchPreview(url: String) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return
        _previewState.value = PreviewFetchState.Loading
        viewModelScope.launch {
            val preview = linkPreviewFetcher.fetch(trimmed)
            _previewState.value = if (preview != null) {
                PreviewFetchState.Found(preview)
            } else {
                PreviewFetchState.NotFound
            }
        }
    }

    fun clearPreview() {
        _previewState.value = PreviewFetchState.Idle
    }

    fun addItem(title: String, photoUrls: List<String>, link: String?, notes: String?) {
        val fetched = (_previewState.value as? PreviewFetchState.Found)?.preview
        val resolvedTitle = title.trim().ifBlank {
            fetched?.title?.trim()?.ifBlank { null }
                ?: link?.trim()?.ifBlank { null }?.let { hostNameOf(it) }
                ?: "Untitled item"
        }
        val mergedPhotoUrls = (photoUrls.filter { it.isNotBlank() } + (fetched?.imageUrls ?: emptyList())).distinct()
        _uiState.value = AddItemUiState.Saving
        viewModelScope.launch {
            try {
                val userId = identityManager.getOrCreateUserId()
                repository.addItem(
                    sessionId = sessionId,
                    title = resolvedTitle,
                    photoUrls = mergedPhotoUrls,
                    link = link?.trim()?.takeIf { it.isNotBlank() },
                    notes = notes?.trim()?.takeIf { it.isNotBlank() },
                    createdByUserId = userId,
                    source = if (fetched != null) "link_preview" else "manual",
                    linkPreview = fetched?.let {
                        mapOf(
                            "title" to it.title,
                            "description" to it.description,
                            "imageUrls" to it.imageUrls
                        )
                    }
                )
                _uiState.value = AddItemUiState.Saved
            } catch (e: Exception) {
                _uiState.value = AddItemUiState.Error(e.message ?: "Couldn't add item.")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddItemUiState.Idle
        _previewState.value = PreviewFetchState.Idle
    }
}

private fun hostNameOf(url: String): String? = runCatching {
    android.net.Uri.parse(url).host?.removePrefix("www.")
}.getOrNull()

class AddItemViewModelFactory(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager,
    private val linkPreviewFetcher: LinkPreviewFetcher,
    private val sessionId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AddItemViewModel(repository, identityManager, linkPreviewFetcher, sessionId) as T
}
