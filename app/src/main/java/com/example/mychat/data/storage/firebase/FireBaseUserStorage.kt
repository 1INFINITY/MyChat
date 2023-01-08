package com.example.mychat.data.storage.firebase

import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

interface FireBaseUserStorage {
    suspend fun userRegistration(user: User): User?

    suspend fun checkUserExistAuthorization(authData: AuthData): Boolean

    suspend fun findUser(authData: AuthData): User?

    suspend fun findUserById(userId: String): User?

    suspend fun updateToken(userId: String)

    suspend fun deleteUserFieldById(userId: String, fieldName: String): Boolean

    suspend fun getAllUsers(): List<User>?

    suspend fun sendMessage(chatMessage: ChatMessage): Boolean

    suspend fun fetchNewMessages(
        sender: User,
        receiver: User,
        flow: ProducerScope<ResultData<List<ChatMessage>>>
    )
}