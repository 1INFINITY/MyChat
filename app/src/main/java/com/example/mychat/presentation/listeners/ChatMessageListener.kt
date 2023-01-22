package com.example.mychat.presentation.listeners

import com.example.mychat.domain.models.ChatMessage

interface ChatMessageListener {
    fun onChatMessageClicked(message: ChatMessage)
}