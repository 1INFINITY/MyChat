package com.example.mychat.domain.models

data class Chat (
    val id: String,
    val name: String?,
    val userReceiver: User,
    val lastMessage: String,
)