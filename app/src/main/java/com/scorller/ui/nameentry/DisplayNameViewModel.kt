package com.scorller.ui.nameentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scorller.data.local.LocalIdentityManager
import com.scorller.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DisplayNameUiState {
    data object Idle : DisplayNameUiState()
    data object Loading : DisplayNameUiState()
    data class Error(val message: String) : DisplayNameUiState()
    data object Confirmed : DisplayNameUiState()
}

class DisplayNameViewModel(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager,
    private val sessionId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<DisplayNameUiState>(DisplayNameUiState.Idle)
    val uiState: StateFlow<DisplayNameUiState> = _uiState

    private val _prefillName = MutableStateFlow("")
    val prefillName: StateFlow<String> = _prefillName

    init {
        viewModelScope.launch {
            identityManager.getDisplayName()?.let { _prefillName.value = it }
        }
    }

    fun confirmName(name: String) {
        if (name.isBlank()) {
            _uiState.value = DisplayNameUiState.Error("Enter a name so your votes are labeled.")
            return
        }
        _uiState.value = DisplayNameUiState.Loading
        viewModelScope.launch {
            try {
                val userId = identityManager.getOrCreateUserId()
                identityManager.setDisplayName(name.trim())
                repository.upsertParticipant(sessionId, userId, name.trim())
                _uiState.value = DisplayNameUiState.Confirmed
            } catch (e: Exception) {
                _uiState.value = DisplayNameUiState.Error(e.message ?: "Couldn't join session.")
            }
        }
    }
}

class DisplayNameViewModelFactory(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager,
    private val sessionId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DisplayNameViewModel(repository, identityManager, sessionId) as T
}
