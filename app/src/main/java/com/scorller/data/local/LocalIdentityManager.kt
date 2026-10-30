package com.scorller.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.scorller.data.model.RecentSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.identityDataStore by preferencesDataStore(name = "identity")
private const val MAX_RECENT_SESSIONS = 25

/**
 * Persists the device's anonymous voter identity: a random userId generated once,
 * the display name the user last typed, and the list of sessions this device has
 * created or joined, so the user can jump back into any of them from Home.
 */
class LocalIdentityManager(private val context: Context) {

    private val userIdKey = stringPreferencesKey("user_id")
    private val displayNameKey = stringPreferencesKey("display_name")
    private val recentSessionsKey = stringPreferencesKey("recent_sessions")

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

    suspend fun getRecentSessions(): List<RecentSession> {
        val raw = context.identityDataStore.data.map { it[recentSessionsKey] }.first() ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                RecentSession(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    joinCode = obj.getString("joinCode")
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun addRecentSession(session: RecentSession) {
        val updated = listOf(session) + getRecentSessions().filterNot { it.id == session.id }
        val array = JSONArray()
        updated.take(MAX_RECENT_SESSIONS).forEach { s ->
            array.put(JSONObject().put("id", s.id).put("title", s.title).put("joinCode", s.joinCode))
        }
        context.identityDataStore.edit { it[recentSessionsKey] = array.toString() }
    }
}
