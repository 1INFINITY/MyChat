package com.example.mychat.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.domain.repository.UserRepository

class SingInModelFactory(
    val repository: UserRepository
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SingInViewModel(
            repository = repository
        ) as T
    }

}