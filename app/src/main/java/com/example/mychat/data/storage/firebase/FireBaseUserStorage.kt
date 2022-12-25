package com.example.mychat.data.storage.firebase

import com.example.mychat.domain.models.User

interface FireBaseUserStorage {
    fun userRegistration(user: User)
}