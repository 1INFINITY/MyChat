package com.example.mychat.domain.repository

import androidx.paging.PagingData
import com.example.mychat.data.models.ChatMessageFirestore
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getMessages(chat: Chat, scope: CoroutineScope): Flow<PagingData<ChatMessage>>

    fun uploadUserList(): Flow<ResultData<List<User>>>

    fun signOut(): Flow<ResultData<Boolean>>

    fun userRegistration(user: User): Flow<ResultData<User>>

    fun userAuthorization(authData: AuthData): Flow<ResultData<User>>

    fun getCachedUser(): User

    fun sendMessage(chatMessage: ChatMessage, chat: Chat): Flow<ResultData<Boolean>>

    fun createNewChat(userSender: User, users: List<User>): Flow<ResultData<Chat>>

    fun openChat(userSender: User, chatId: String): Flow<ResultData<Chat>>

    fun fetchChats(userSender: User): Flow<ResultData<Chat>>

    fun deleteMessage(chatMessage: ChatMessage): Flow<ResultData<ChatMessage>>

    fun changeMessage(chatMessage: ChatMessage): Flow<ResultData<ChatMessage>>
}