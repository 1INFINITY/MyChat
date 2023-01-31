package com.example.mychat.domain.models

data class Chat (
    val id: String,
    val userSender: User,
    val userReceiver: User,
    val lastMessage: String,
)