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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserRepositoryImpl(
    private val firebaseStorage: FireBaseUserStorage,
    private val sharedPrefsStorage: SharedPreferencesStorage,
) : UserRepository {

    private val appScope: CoroutineScope = GlobalScope

    private val userListResult = MutableSharedFlow<ResultData<List<User>>>()
    override fun observeUserListResult() = userListResult.asSharedFlow()

    private val signOutResult = MutableSharedFlow<ResultData<Boolean>>()
    override fun observeSignOutResult() = signOutResult.asSharedFlow()

    private val authResult = MutableSharedFlow<ResultData<User>>()
    override fun observeAuthResult() = authResult.asSharedFlow()

    private val registrationResult = MutableSharedFlow<ResultData<User>>()
    override fun observeRegistration() = registrationResult.asSharedFlow()

    private val messagesResult = MutableSharedFlow<ResultData<List<ChatMessage>>>()
    override fun observeMessages(): SharedFlow<ResultData<List<ChatMessage>>> = messagesResult.asSharedFlow()

    override fun uploadUserList() {
        appScope.launch {
            userListResult.emit(ResultData.loading(null))

            val result: List<User>? = firebaseStorage.getAllUsers()

            if (result != null) {
                userListResult.emit(ResultData.success(result))
            } else {
                userListResult.emit(ResultData.failure("Something goes wrong"))
            }
        }
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

    override fun userRegistration(user: User) {
        appScope.launch {
            registrationResult.emit(ResultData.loading(null))

            val authData = AuthData(email = user.email, password = user.password)

            val findResult: User? = firebaseStorage.findUser(authData)
            if (findResult != null) {
                registrationResult.emit(ResultData.failure("This user already exist"))
            } else {
                val result: User? = firebaseStorage.userRegistration(user)

                if (result != null) {
                    sharedPrefsStorage.saveUserDetails(user = result)
                    firebaseStorage.updateToken(userId = result.id)
                    registrationResult.emit(ResultData.success(result))
                } else {
                    registrationResult.emit(ResultData.failure("Something goes wrong"))
                }
            }
        }
    }

    override fun userAuthorization(authData: AuthData) {
        appScope.launch {
            authResult.emit(ResultData.loading(null))

            val result: User? = firebaseStorage.findUser(authData)

            if (result != null) {
                sharedPrefsStorage.saveUserDetails(user = result)
                firebaseStorage.updateToken(userId = result.id)
                authResult.emit(ResultData.success(result))
            } else {
                authResult.emit(ResultData.failure("Something goes wrong"))
            }
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

    override fun listenMessages(sender: User, receiver: User) = callbackFlow<ResultData<List<ChatMessage>>> {
        firebaseStorage.fetchNewMessages(sender = sender, receiver = receiver, this)
    }

    override fun createNewChat(users: List<User>) {
        appScope.launch {
            firebaseStorage.createNewChat(users)
        }
    }

    override fun fetchChats(user: User) = callbackFlow<ResultData<List<Chat>>> {
        firebaseStorage.fetchChats(user = user, this)
    }
}