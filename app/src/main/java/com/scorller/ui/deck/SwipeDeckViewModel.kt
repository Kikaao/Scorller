package com.scorller.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scorller.data.local.LocalIdentityManager
import com.scorller.data.model.Item
import com.scorller.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SwipeDeckViewModel(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager,
    private val sessionId: String
) : ViewModel() {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items

    /** Index of the top-of-deck card within [items]. */
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private var userId: String? = null

    init {
        viewModelScope.launch {
            userId = identityManager.getOrCreateUserId()
            repository.observeItems(sessionId).collect { newItems ->
                _items.value = newItems
                if (_currentIndex.value > newItems.size) {
                    _currentIndex.value = newItems.size
                }
            }
        }
    }

    fun swipe(item: Item, isRight: Boolean) {
        val uid = userId ?: return
        viewModelScope.launch {
            repository.setVote(sessionId, item.id, uid, isRight)
        }
        if (_currentIndex.value < _items.value.size) {
            _currentIndex.value += 1
        }
    }

    fun goBack() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
        }
    }
}

class SwipeDeckViewModelFactory(
    private val repository: SessionRepository,
    private val identityManager: LocalIdentityManager,
    private val sessionId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SwipeDeckViewModel(repository, identityManager, sessionId) as T
}
