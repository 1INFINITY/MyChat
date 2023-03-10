package com.example.mychat.domain.usecase

import androidx.paging.PagingData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChatUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(chat: Chat, scope: CoroutineScope): Flow<PagingData<ChatMessage>> {
        return repository.getMessages(chat = chat, scope = scope)
    }
}