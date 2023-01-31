package com.example.mychat.domain.usecase

import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChatUseCase @Inject constructor(private val repository: UserRepository) {

    fun execute(chat: Chat): Flow<ResultData<ChatMessage>> {
        return repository.fetchMessages(chat = chat)
    }
}