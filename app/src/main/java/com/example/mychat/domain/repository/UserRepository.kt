package com.example.mychat.domain.repository

import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import kotlinx.coroutines.flow.*

interface UserRepository {

    fun uploadUserList(): Flow<ResultData<List<User>>>

    fun signOut()

    fun userRegistration(user: User): Flow<ResultData<User>>

    fun userAuthorization(authData: AuthData): Flow<ResultData<User>>

    fun getCachedUser(): User

    fun sendMessage(chatMessage: ChatMessage)

    fun listenMessages(chat: Chat): Flow<ResultData<List<ChatMessage>>>

    fun createNewChat(users: List<User>): Flow<ResultData<Chat>>

    fun openChat(chat: Chat): Flow<ResultData<Chat>>

    fun fetchChats(user: User): Flow<ResultData<List<Chat>>>
}