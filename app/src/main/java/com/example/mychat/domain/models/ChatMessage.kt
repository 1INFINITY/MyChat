package com.example.mychat.domain.models

import java.util.*

data class ChatMessage(
    val id: String,
    val sender: User,
    val message: String,
    val date: Date
)