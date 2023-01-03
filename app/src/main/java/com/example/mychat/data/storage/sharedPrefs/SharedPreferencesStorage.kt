package com.example.mychat.data.storage.sharedPrefs

import com.example.mychat.domain.models.User

interface SharedPreferencesStorage {

    fun saveUserDetails(user: User)

    fun getUserDetails(): User

    fun clearUserDetails()
}