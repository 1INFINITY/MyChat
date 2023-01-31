package com.example.mychat.domain.usecase

import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChangeMessageUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(chatMessage: ChatMessage): Flow<ResultData<ChatMessage>> {
        return repository.changeMessage(chatMessage = chatMessage)
    }
}