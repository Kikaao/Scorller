package com.scorller.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scorller.data.local.LocalIdentityManager
import com.scorller.data.model.RecentSession
import com.scorller.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Idle : HomeUiState()
    data object Loading : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    data class Success(val sessionId: String) : HomeUiState()
}

class HomeViewModel(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _recentSessions = MutableStateFlow<List<RecentSession>>(emptyList())
    val recentSessions: StateFlow<List<RecentSession>> = _recentSessions

    init {
        refreshRecentSessions()
    }

    fun refreshRecentSessions() {
        viewModelScope.launch {
            _recentSessions.value = identityManager.getRecentSessions()
        }
    }

    fun createSession(title: String) {
        if (title.isBlank()) {
            _uiState.value = HomeUiState.Error("Give this session a title.")
            return
        }
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                val userId = identityManager.getOrCreateUserId()
                val session = repository.createSession(title.trim(), userId)
                identityManager.addRecentSession(RecentSession(session.id, session.title, session.joinCode))
                _uiState.value = HomeUiState.Success(session.id)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Couldn't create session.")
            }
        }
    }

    fun joinSession(code: String) {
        if (code.isBlank()) {
            _uiState.value = HomeUiState.Error("Enter a join code.")
            return
        }
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                val session = repository.findSessionByJoinCode(code.trim())
                if (session == null) {
                    _uiState.value = HomeUiState.Error("Code not found.")
                } else {
                    identityManager.addRecentSession(RecentSession(session.id, session.title, session.joinCode))
                    _uiState.value = HomeUiState.Success(session.id)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Couldn't join session.")
            }
        }
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}

class HomeViewModelFactory(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(repository, identityManager) as T
}
