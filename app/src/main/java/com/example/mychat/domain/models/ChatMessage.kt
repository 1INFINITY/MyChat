package com.example.mychat.domain.models

import java.util.*

data class ChatMessage(
    val senderId: String,
    val receiverId: String,
    val message: String,
    val date: Date
)