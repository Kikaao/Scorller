package com.scorller.data.model

import com.google.firebase.Timestamp

data class Participant(
    val userId: String = "",
    val displayName: String = "",
    val joinedAt: Timestamp? = null
)
