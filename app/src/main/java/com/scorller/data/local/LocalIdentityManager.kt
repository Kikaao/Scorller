package com.scorller.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.identityDataStore by preferencesDataStore(name = "identity")

/**
 * Persists the device's anonymous voter identity: a random userId generated once,
 * plus the display name the user last typed (reused as a default across sessions).
 */
class LocalIdentityManager(private val context: Context) {

    private val userIdKey = stringPreferencesKey("user_id")
    private val displayNameKey = stringPreferencesKey("display_name")

    suspend fun getOrCreateUserId(): String {
        val existing = context.identityDataStore.data.map { it[userIdKey] }.first()
        if (existing != null) return existing
        val newId = UUID.randomUUID().toString()
        context.identityDataStore.edit { it[userIdKey] = newId }
        return newId
    }

    suspend fun getDisplayName(): String? =
        context.identityDataStore.data.map { it[displayNameKey] }.first()

    suspend fun setDisplayName(name: String) {
        context.identityDataStore.edit { it[displayNameKey] = name }
    }
}
