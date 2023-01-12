package com.example.mychat.domain.models

data class Chat (
    val id: String,
    val name: String?,
    val users: List<User>,
    val lastMessage: String,
)