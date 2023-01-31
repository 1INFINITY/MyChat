package com.example.mychat.domain.usecase

import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChatsUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(userSender: User): Flow<ResultData<Chat>> {
        return repository.fetchChats(userSender = userSender)
    }
}