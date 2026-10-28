package com.scorller.data.model

import com.google.firebase.Timestamp

data class Session(
    val id: String = "",
    val joinCode: String = "",
    val title: String = "",
    val createdAt: Timestamp? = null,
    val createdByUserId: String = ""
)
