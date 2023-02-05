package com.example.mychat.data.storage.firebase

import com.example.mychat.data.models.ChatFirestore
import com.example.mychat.data.models.ChatMessageFirestore
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

interface FireBaseUserStorage {
    suspend fun userRegistration(user: User, flow: FlowCollector<ResultData<User>>): User?

    suspend fun checkUserExistAuthorization(authData: AuthData): Boolean

    suspend fun findUserByRef(userRef: DocumentReference): User

    suspend fun findUser(authData: AuthData): User?

    suspend fun updateToken(userId: String)

    suspend fun signOutUser(user: User, flow: FlowCollector<ResultData<Boolean>>): Boolean

    suspend fun getAllUsers(flow: FlowCollector<ResultData<List<User>>>): List<User>?

    suspend fun sendMessage(chatMessage: ChatMessage, chat: Chat, flow: FlowCollector<ResultData<Boolean>>): Boolean

    suspend fun userAuthorization(authData: AuthData, flow: FlowCollector<ResultData<User>>): User?

    suspend fun fetchNewMessages(
        chat: Chat,
    ): Flow<ResultData<ChatMessageFirestore>>

    suspend fun fetchPagingMessages(
        chat: Chat,
        page: Int,
        pageSize: Int
    ): ResultData<List<ChatMessageFirestore>>

    suspend fun findChatByRef(chatRef: DocumentReference): ChatFirestore

    suspend fun createNewChat(userSender: User, users: List<User>, flow: FlowCollector<ResultData<Chat>>)

    suspend fun openChat(user: User, chatId: String, flow: FlowCollector<ResultData<Chat>>)

    suspend fun fetchChats(user: User): Flow<ResultData<ChatFirestore>>

    suspend fun changeMessage(chatMessage: ChatMessage, flow: FlowCollector<ResultData<ChatMessage>>)

    suspend fun deleteMessage(chatMessage: ChatMessage, flow: FlowCollector<ResultData<ChatMessage>>)
}