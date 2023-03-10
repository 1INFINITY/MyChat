package com.example.mychat.data.repository

import androidx.paging.*
import com.example.mychat.data.mapping.ImageMapper
import com.example.mychat.data.mapping.UserMapper
import com.example.mychat.data.models.ChatFirestore
import com.example.mychat.data.models.ChatMessageFirestore
import com.example.mychat.data.models.ChatMessagePageSource
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_CHATS
import com.example.mychat.data.storage.firebase.FireBaseUserStorage
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorage
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*

@ExperimentalPagingApi
class UserRepositoryImpl constructor(
    private val firebaseStorage: FireBaseUserStorage,
    private val sharedPrefsStorage: SharedPreferencesStorage,
    private val pagingSourceFactory: ChatMessagePageSource.Factory
) : UserRepository {

    override fun getMessages(chat: Chat, scope: CoroutineScope): Flow<PagingData<ChatMessage>> {
        val invalidatingPagingSourceFactory = InvalidatingPagingSourceFactory {
            pagingSourceFactory.create(chat = chat)
        }
        firebaseStorage.setCallbackOnChatUpdates(
            scope = scope,
            chat = chat,
            callback = invalidatingPagingSourceFactory::invalidate
        )
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                initialLoadSize = 10
            ),
            pagingSourceFactory = invalidatingPagingSourceFactory
        ).flow
    }

    override fun uploadUserList() = flow<ResultData<List<User>>> {
        emit(ResultData.loading(null))
        firebaseStorage.getAllUsers(flow = this)
    }

    // Todo: how to observe signOut value without Boolean?
    override fun signOut() = flow<ResultData<Boolean>> {
        emit(ResultData.loading(null))
        val userCached: User = sharedPrefsStorage.getUserDetails()

        val result: Boolean = firebaseStorage.signOutUser(user = userCached, flow = this)

        if (result)
            sharedPrefsStorage.clearUserDetails()
    }

    override fun userRegistration(user: User) = flow<ResultData<User>> {
        emit(ResultData.loading(null))
        val user: User? = firebaseStorage.userRegistration(user = user, flow = this)

        user?.let {
            sharedPrefsStorage.saveUserDetails(user = it)
            firebaseStorage.updateToken(userId = it.id)
        }
    }

    override fun userAuthorization(authData: AuthData) = flow<ResultData<User>> {
        emit(ResultData.loading(null))
        val user = firebaseStorage.userAuthorization(authData = authData, flow = this)

        user?.let {
            sharedPrefsStorage.saveUserDetails(user = it)
        }
    }

    override fun getCachedUser(): User {
        return sharedPrefsStorage.getUserDetails()
    }

    override fun sendMessage(chatMessage: ChatMessage, chat: Chat) = flow<ResultData<Boolean>> {
        emit(ResultData.loading(null))
        firebaseStorage.sendMessage(chatMessage = chatMessage, chat = chat, flow = this)
    }

    override fun createNewChat(userSender: User, users: List<User>) = flow<ResultData<Chat>> {
        emit(ResultData.loading(null))
        firebaseStorage.createNewChat(userSender = userSender, users = users, flow = this)
    }

    override fun openChat(userSender: User, chatId: String) = flow<ResultData<Chat>> {
        emit(ResultData.loading(null))
        firebaseStorage.openChat(user = userSender, chatId = chatId, flow = this)
    }

    override fun fetchChats(userSender: User) = callbackFlow<ResultData<Chat>> {
        trySendBlocking(ResultData.loading(null))
        firebaseStorage.fetchChats(user = userSender).collect {
            when (it) {
                is ResultData.Success -> {
                    trySendBlocking(
                        (ResultData.success(
                            chatMapping(
                                userSender = userSender,
                                chatFirestore = it.value
                            )
                        ))
                    )
                }
                is ResultData.Update -> {
                    trySendBlocking(
                        (ResultData.update(
                            chatMapping(
                                userSender = userSender,
                                chatFirestore = it.value
                            )
                        ))
                    )
                }
                is ResultData.Removed -> {
                    trySendBlocking(
                        (ResultData.removed(
                            chatMapping(
                                userSender = userSender,
                                chatFirestore = it.value
                            )
                        ))
                    )
                }
                else -> {}
            }
        }
    }

    private suspend fun chatMapping(userSender: User, chatFirestore: ChatFirestore): Chat {
        val userReceiverRef =
            chatFirestore.usersIdArray!!.find { userRef -> userRef.id != userSender.id }!!
        return Chat(
            id = chatFirestore.id!!,
            userSender = userSender,
            userReceiver = firebaseStorage.findUserByRef(userReceiverRef),
            lastMessage = chatFirestore.lastMessage!!
        )
    }

    override fun deleteMessage(chatMessage: ChatMessage) = flow<ResultData<ChatMessage>> {
        emit(ResultData.loading(null))
        firebaseStorage.deleteMessage(chatMessage = chatMessage, flow = this)
    }

    override fun changeMessage(chatMessage: ChatMessage) = flow<ResultData<ChatMessage>> {
        emit(ResultData.loading(null))
        firebaseStorage.changeMessage(chatMessage = chatMessage, flow = this)
    }
}