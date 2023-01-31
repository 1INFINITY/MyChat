package com.example.mychat.domain.usecase

import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateNewChatUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(userSender: User, users: List<User>): Flow<ResultData<Chat>> {
        return repository.createNewChat(userSender = userSender, users = users)
    }
}