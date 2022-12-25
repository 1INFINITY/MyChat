package com.example.mychat.data.repository

import com.example.mychat.data.storage.firebase.FireBaseUserStorage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.UserRepository

class UserRepositoryImpl(private val firebaseStorage: FireBaseUserStorage): UserRepository {
    override fun userRegistration(user: User) {
        firebaseStorage.userRegistration(user)
    }
}