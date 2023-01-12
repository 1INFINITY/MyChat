package com.example.mychat.presentation.listeners

import com.example.mychat.domain.models.Chat

interface ChatListener {
    fun onChatClicked(chat: Chat)
}