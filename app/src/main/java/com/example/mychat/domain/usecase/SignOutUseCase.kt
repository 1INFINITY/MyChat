package com.example.mychat.domain.usecase

import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val repository: UserRepository) {
    fun execute(): Flow<ResultData<Boolean>> {
        return repository.signOut()
    }
}