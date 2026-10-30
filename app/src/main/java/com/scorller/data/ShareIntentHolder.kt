package com.scorller.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridges an incoming Android "Share" intent (from a browser, the Airbnb app, etc.)
 * into the Compose nav graph. MainActivity sets [pendingLink] when it receives a
 * share; the nav graph observes it to jump to the session picker, and AddItemScreen
 * consumes it once to prefill + auto-fetch.
 */
object ShareIntentHolder {
    private val _pendingLink = MutableStateFlow<String?>(null)
    val pendingLink: StateFlow<String?> = _pendingLink

    fun offer(link: String) {
        _pendingLink.value = link
    }

    fun consume(): String? {
        val value = _pendingLink.value
        _pendingLink.value = null
        return value
    }
}
