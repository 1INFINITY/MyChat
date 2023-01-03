package com.example.mychat.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mychat.domain.repository.UserRepository

class SingUpModelFactory(
    val repository: UserRepository,
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SingUpViewModel(
            repository = repository
        ) as T
    }

}