package com.example.mychat.domain.repository

import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import kotlinx.coroutines.flow.SharedFlow

interface UserRepository {
    fun userRegistration(user: User)

    fun userAuthorization(authData: AuthData)

    fun dopelUserRegistration(user: User)

    fun dopelUserAuthorization(authData: AuthData)
}