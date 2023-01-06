package com.example.mychat.presentation.listeners

import com.example.mychat.domain.models.User

interface UserListener {
    fun onUserClicked(user: User)
}