package com.example.mychat.domain.usecase

import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadUserListUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(): Flow<ResultData<List<User>>> {
        return repository.uploadUserList()
    }
}