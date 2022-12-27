package com.example.mychat.data.repository

import com.example.mychat.data.storage.firebase.FireBaseUserStorage
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class UserRepositoryImpl(private val firebaseStorage: FireBaseUserStorage): UserRepository {

    private val appScope: CoroutineScope = GlobalScope

    private val authResult = MutableSharedFlow<ResultData<Any>>()
    fun observeAuthResult() = authResult.asSharedFlow()

    private val registrationResult = MutableSharedFlow<ResultData<Any>>()
    fun observeRegistration() = registrationResult.asSharedFlow()

    override fun userRegistration(user: User) {
        firebaseStorage.userRegistration(user)
    }

    override fun userAuthorization(authData: AuthData) {
        //firebaseStorage.checkUserExist(authData)
    }

    override fun dopelUserRegistration(user: User) {
        TODO("Not yet implemented")
    }

    override fun dopelUserAuthorization(authData: AuthData){
        appScope.launch {
            authResult.emit(ResultData.loading(null))

            val result = firebaseStorage.checkUserExistAuthorization(authData)

            if (result) {
                // TODO: Save user
                //sharedPrefsStorage.saveUser()
                authResult.emit(ResultData.success(null))
            } else {
                authResult.emit(ResultData.failure("Что-то пошло не так"))
            }
        }
    }

}