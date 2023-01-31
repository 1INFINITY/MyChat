package com.example.mychat.domain.usecase

import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.UserRepository
import javax.inject.Inject

class GetCachedUserUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(): User {
        return repository.getCachedUser()
    }
}