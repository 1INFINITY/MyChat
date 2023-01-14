package com.example.mychat.data.repository

import android.util.Log
import com.example.mychat.data.storage.StorageConstants.KEY_FCM_TOKEN
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine

class UserRepositoryImpl(
    private val firebaseStorage: FireBaseUserStorage,
    private val sharedPrefsStorage: SharedPreferencesStorage,
) : UserRepository {

    private val appScope: CoroutineScope = GlobalScope
    private val signOutResult = MutableSharedFlow<ResultData<Boolean>>()

    override fun uploadUserList() = flow<ResultData<List<User>>> {
        emit(ResultData.loading(null))
        firebaseStorage.getAllUsers(flow = this)
    }

    override fun signOut() {
        appScope.launch {
            signOutResult.emit(ResultData.loading(null))

            val userCached: User = sharedPrefsStorage.getUserDetails()
            val result: Boolean = firebaseStorage.deleteUserFieldById(
                userId = userCached.id,
                fieldName = KEY_FCM_TOKEN)
            if (result) {
                Log.d("MyRep", "Success sign out")
                sharedPrefsStorage.clearUserDetails()
                signOutResult.emit(ResultData.success(true))
            } else {
                Log.d("MyRep", "Failure sign out")
                signOutResult.emit(ResultData.failure("Something goes wrong"))
            }
        }
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

        user?.let{
            sharedPrefsStorage.saveUserDetails(user = it)
        }
    }

    override fun getCachedUser(): User {
        val user = sharedPrefsStorage.getUserDetails()
        appScope.launch {
            if (user.id == "") {
                Log.d("MyRep", "Failure get cached user")
            } else {
                Log.d("MyRep", "Success get cached user")
                //firebaseStorage.updateToken(user.id)
            }
        }
        return user
    }

    override fun sendMessage(chatMessage: ChatMessage) {
        appScope.launch {
            val result: Boolean = firebaseStorage.sendMessage(chatMessage)
        }
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