package com.scorller.ui.sessionhub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scorller.data.model.Session
import com.scorller.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionHubViewModel(
    repository: SessionRepository,
    sessionId: String
) : ViewModel() {

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session

    init {
        viewModelScope.launch {
            _session.value = repository.getSession(sessionId)
        }
    }
}

class SessionHubViewModelFactory(
    private val repository: SessionRepository,
    private val sessionId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SessionHubViewModel(repository, sessionId) as T
}
