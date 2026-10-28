package com.scorller.data.model

import com.google.firebase.Timestamp

data class Item(
    val id: String = "",
    val title: String = "",
    val photoUrls: List<String> = emptyList(),
    val link: String? = null,
    val notes: String? = null,
    val createdAt: Timestamp? = null,
    val createdByUserId: String = "",
    val source: String = "manual",
    val linkPreview: Map<String, Any>? = null,
    val votes: Map<String, Boolean> = emptyMap()
) {
    val yesCount: Int get() = votes.values.count { it }

    fun isUnanimous(totalParticipants: Int): Boolean =
        totalParticipants > 0 && votes.size == totalParticipants && yesCount == totalParticipants
}
