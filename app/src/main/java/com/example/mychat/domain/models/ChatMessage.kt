package com.example.mychat.domain.models

import java.util.*

data class ChatMessage(
    val chat: Chat,
    val sender: User,
    val message: String,
    val date: Date
)