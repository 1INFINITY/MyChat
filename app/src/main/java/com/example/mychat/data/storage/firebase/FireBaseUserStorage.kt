package com.example.mychat.data.storage.firebase

import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User

interface FireBaseUserStorage {
    suspend fun userRegistration(user: User): User?

    suspend fun checkUserExistAuthorization(authData: AuthData): Boolean

    suspend fun findUser(authData: AuthData): User?

    suspend fun findUserById(userId: String): User?

    suspend fun updateToken(userId: String)

    suspend fun deleteUserFieldById(userId: String, fieldName: String): Boolean
}