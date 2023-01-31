package com.example.mychat.domain.usecase

import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserAuthorizationUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(authData: AuthData): Flow<ResultData<User>> {
        return repository.userAuthorization(authData = authData)
    }
}