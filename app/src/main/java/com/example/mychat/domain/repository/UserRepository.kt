package com.example.mychat.domain.repository

import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface UserRepository {

    fun observeSignOutResult(): SharedFlow<ResultData<Boolean>>

    fun observeAuthResult(): SharedFlow<ResultData<User>>

    fun observeRegistration(): SharedFlow<ResultData<User>>

    fun signOut()

    fun userRegistration(user: User)

    fun userAuthorization(authData: AuthData)

    fun getCachedUser(): User
}