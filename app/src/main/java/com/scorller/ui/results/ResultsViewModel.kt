package com.scorller.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scorller.data.model.Item
import com.scorller.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RankedItem(val item: Item, val totalParticipants: Int)

class ResultsViewModel(
    repository: SessionRepository,
    sessionId: String
) : ViewModel() {

    private val _rankedItems = MutableStateFlow<List<RankedItem>>(emptyList())
    val rankedItems: StateFlow<List<RankedItem>> = _rankedItems

    private var items: List<Item> = emptyList()
    private var totalParticipants: Int = 0

    init {
        viewModelScope.launch {
            repository.observeItems(sessionId).collect { newItems ->
                items = newItems
                recompute()
            }
        }
        viewModelScope.launch {
            repository.observeParticipants(sessionId).collect { participants ->
                totalParticipants = participants.size
                recompute()
            }
        }
    }

    private fun recompute() {
        _rankedItems.value = items
            .map { RankedItem(it, totalParticipants) }
            .sortedByDescending { it.item.yesCount }
    }
}

class ResultsViewModelFactory(
    private val repository: SessionRepository,
    private val sessionId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ResultsViewModel(repository, sessionId) as T
}
