package com.example.mychat.domain.repository

import com.example.mychat.domain.models.User

interface UserRepository {
    fun userRegistration(user: User)
}