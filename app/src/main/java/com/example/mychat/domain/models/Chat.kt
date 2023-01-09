package com.example.mychat.domain.models

data class Chat (
    val users: List<User>,
    val lastMessage: String,
    val lastMessageByUserId: String,
)