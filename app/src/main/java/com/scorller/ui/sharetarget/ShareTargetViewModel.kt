package com.scorller.ui.sharetarget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scorller.data.local.LocalIdentityManager
import com.scorller.data.model.RecentSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShareTargetViewModel(identityManager: LocalIdentityManager) : ViewModel() {

    private val _sessions = MutableStateFlow<List<RecentSession>>(emptyList())
    val sessions: StateFlow<List<RecentSession>> = _sessions

    init {
        viewModelScope.launch {
            _sessions.value = identityManager.getRecentSessions()
        }
    }
}

class ShareTargetViewModelFactory(
    private val identityManager: LocalIdentityManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ShareTargetViewModel(identityManager) as T
}
