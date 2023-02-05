package com.example.mychat.domain.usecase

import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(chatMessage: ChatMessage, chat: Chat): Flow<ResultData<Boolean>> {
        return repository.sendMessage(chatMessage = chatMessage, chat = chat)
    }
}