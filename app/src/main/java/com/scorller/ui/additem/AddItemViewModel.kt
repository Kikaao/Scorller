package com.scorller.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

class AddItemViewModel(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager,
    private val sessionId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddItemUiState>(AddItemUiState.Idle)
    val uiState: StateFlow<AddItemUiState> = _uiState

    fun addItem(title: String, photoUrls: List<String>, link: String?, notes: String?) {
        if (title.isBlank()) {
            _uiState.value = AddItemUiState.Error("Give this item a title.")
            return
        }
        _uiState.value = AddItemUiState.Saving
        viewModelScope.launch {
            try {
                val userId = identityManager.getOrCreateUserId()
                repository.addItem(
                    sessionId = sessionId,
                    title = title.trim(),
                    photoUrls = photoUrls.filter { it.isNotBlank() },
                    link = link?.trim()?.takeIf { it.isNotBlank() },
                    notes = notes?.trim()?.takeIf { it.isNotBlank() },
                    createdByUserId = userId
                )
                _uiState.value = AddItemUiState.Saved
            } catch (e: Exception) {
                _uiState.value = AddItemUiState.Error(e.message ?: "Couldn't add item.")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddItemUiState.Idle
    }
}

class AddItemViewModelFactory(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager,
    private val sessionId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AddItemViewModel(repository, identityManager, sessionId) as T
}
