package com.example.mychat.domain.models

data class ChatMessage(
    val senderId: String,
    val receiverId: String,
    val message: String,
    val dataTime: String,
)