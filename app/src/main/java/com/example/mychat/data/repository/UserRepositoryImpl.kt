package com.example.mychat.data.repository

import com.example.mychat.data.storage.firebase.FireBaseUserStorage
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorage
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class UserRepositoryImpl(
    private val firebaseStorage: FireBaseUserStorage,
    private val sharedPrefsStorage: SharedPreferencesStorage,
) : UserRepository {

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

    override fun sendMessage(chatMessage: ChatMessage) = flow<ResultData<Boolean>> {
        emit(ResultData.loading(null))
        firebaseStorage.sendMessage(chatMessage, flow = this)
    }

    override fun listenMessages(chat: Chat) = callbackFlow<ResultData<List<ChatMessage>>> {
        trySendBlocking(ResultData.loading(null))
        firebaseStorage.fetchNewMessages(chat = chat, flow = this)
    }

    override fun createNewChat(users: List<User>) = flow<ResultData<Chat>> {
        emit(ResultData.loading(null))
        firebaseStorage.createNewChat(users = users, flow = this)
    }

    override fun openChat(chat: Chat) = flow<ResultData<Chat>> {
        emit(ResultData.loading(null))
        firebaseStorage.openChat(chat = chat, flow = this)
    }

    override fun fetchChats(user: User) = callbackFlow<ResultData<List<Chat>>> {
        trySendBlocking(ResultData.loading(null))
        firebaseStorage.fetchChats(user = user, this)
    }
}