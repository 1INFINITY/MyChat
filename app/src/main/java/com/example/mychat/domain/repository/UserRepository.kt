package com.example.mychat.domain.repository

import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface UserRepository {

    fun observeUserListResult(): SharedFlow<ResultData<List<User>>>

    fun observeSignOutResult(): SharedFlow<ResultData<Boolean>>

    fun observeAuthResult(): SharedFlow<ResultData<User>>

    fun observeRegistration(): SharedFlow<ResultData<User>>

    fun observeMessages(): SharedFlow<ResultData<List<ChatMessage>>>

    fun uploadUserList()

    fun signOut()

    fun userRegistration(user: User)

    fun userAuthorization(authData: AuthData)

    fun getCachedUser(): User

    fun sendMessage(chatMessage: ChatMessage)

    fun listenMessages(sender: User, receiver: User): Flow<ResultData<List<ChatMessage>>>

    fun createNewChat(users: List<User>)

    fun fetchChats(user: User): Flow<ResultData<List<Chat>>>
}