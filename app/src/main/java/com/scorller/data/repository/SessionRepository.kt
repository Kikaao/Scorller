package com.scorller.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.scorller.data.model.Item
import com.scorller.data.model.Participant
import com.scorller.data.model.Session
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val SESSIONS = "sessions"
private const val ITEMS = "items"
private const val PARTICIPANTS = "participants"
private val JOIN_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toList()

class SessionRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private fun sessionsRef() = firestore.collection(SESSIONS)
    private fun itemsRef(sessionId: String) = sessionsRef().document(sessionId).collection(ITEMS)
    private fun participantsRef(sessionId: String) = sessionsRef().document(sessionId).collection(PARTICIPANTS)

    suspend fun createSession(title: String, createdByUserId: String): Session {
        var joinCode = generateJoinCode()
        while (sessionsRef().whereEqualTo("joinCode", joinCode).limit(1).get().await().documents.isNotEmpty()) {
            joinCode = generateJoinCode()
        }
        val doc = sessionsRef().document()
        val session = Session(
            id = doc.id,
            joinCode = joinCode,
            title = title,
            createdAt = Timestamp.now(),
            createdByUserId = createdByUserId
        )
        doc.set(session.toMap()).await()
        return session
    }

    suspend fun getSession(sessionId: String): Session? {
        val snapshot = sessionsRef().document(sessionId).get().await()
        return if (snapshot.exists()) snapshot.toSession() else null
    }

    suspend fun findSessionByJoinCode(code: String): Session? {
        val snapshot = sessionsRef().whereEqualTo("joinCode", code.uppercase()).limit(1).get().await()
        val docSnapshot = snapshot.documents.firstOrNull() ?: return null
        return docSnapshot.toSession()
    }

    suspend fun upsertParticipant(sessionId: String, userId: String, displayName: String) {
        val existing = participantsRef(sessionId).document(userId).get().await()
        val joinedAt = if (existing.exists()) existing.getTimestamp("joinedAt") ?: Timestamp.now() else Timestamp.now()
        participantsRef(sessionId).document(userId).set(
            mapOf(
                "userId" to userId,
                "displayName" to displayName,
                "joinedAt" to joinedAt
            )
        ).await()
    }

    fun observeParticipants(sessionId: String): Flow<List<Participant>> = callbackFlow {
        val registration = participantsRef(sessionId).addSnapshotListener { snapshot, _ ->
            val participants = snapshot?.documents?.mapNotNull { it.toParticipant() } ?: emptyList()
            trySend(participants)
        }
        awaitClose { registration.remove() }
    }

    suspend fun addItem(
        sessionId: String,
        title: String,
        photoUrls: List<String>,
        link: String?,
        notes: String?,
        createdByUserId: String
    ) {
        val doc = itemsRef(sessionId).document()
        val item = Item(
            id = doc.id,
            title = title,
            photoUrls = photoUrls,
            link = link,
            notes = notes,
            createdAt = Timestamp.now(),
            createdByUserId = createdByUserId,
            source = "manual",
            linkPreview = null,
            votes = emptyMap()
        )
        doc.set(item.toMap()).await()
    }

    fun observeItems(sessionId: String): Flow<List<Item>> = callbackFlow {
        val registration = itemsRef(sessionId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.documents?.mapNotNull { it.toItem() } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun setVote(sessionId: String, itemId: String, userId: String, isRight: Boolean) {
        itemsRef(sessionId).document(itemId).update("votes.$userId", isRight).await()
    }
}

private fun generateJoinCode(): String = (1..6).map { JOIN_CODE_ALPHABET.random() }.joinToString("")

private fun Session.toMap(): Map<String, Any?> = mapOf(
    "joinCode" to joinCode,
    "title" to title,
    "createdAt" to createdAt,
    "createdByUserId" to createdByUserId
)

private fun Item.toMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "photoUrls" to photoUrls,
    "link" to link,
    "notes" to notes,
    "createdAt" to createdAt,
    "createdByUserId" to createdByUserId,
    "source" to source,
    "linkPreview" to linkPreview,
    "votes" to votes
)

private fun com.google.firebase.firestore.DocumentSnapshot.toSession(): Session? {
    val title = getString("title") ?: return null
    return Session(
        id = id,
        joinCode = getString("joinCode") ?: "",
        title = title,
        createdAt = getTimestamp("createdAt"),
        createdByUserId = getString("createdByUserId") ?: ""
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toItem(): Item? {
    val title = getString("title") ?: return null
    @Suppress("UNCHECKED_CAST")
    val votes = (get("votes") as? Map<String, Any?>)?.mapValues { it.value as? Boolean ?: false } ?: emptyMap()
    @Suppress("UNCHECKED_CAST")
    val photoUrls = (get("photoUrls") as? List<String>) ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val linkPreview = get("linkPreview") as? Map<String, Any>
    return Item(
        id = id,
        title = title,
        photoUrls = photoUrls,
        link = getString("link"),
        notes = getString("notes"),
        createdAt = getTimestamp("createdAt"),
        createdByUserId = getString("createdByUserId") ?: "",
        source = getString("source") ?: "manual",
        linkPreview = linkPreview,
        votes = votes
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toParticipant(): Participant? {
    val displayName = getString("displayName") ?: return null
    return Participant(
        userId = id,
        displayName = displayName,
        joinedAt = getTimestamp("joinedAt")
    )
}
