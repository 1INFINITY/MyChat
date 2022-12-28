package com.example.mychat.domain.repository

import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface UserRepository {

    fun observeAuthResult(): SharedFlow<ResultData<String>>

    fun observeRegistration(): SharedFlow<ResultData<Any>>

    fun userRegistration(user: User)

    fun userAuthorization(authData: AuthData)
}