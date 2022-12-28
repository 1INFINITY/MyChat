package com.example.mychat.presentation.viewmodels

import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychat.R
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SingInViewModel(private val repository: UserRepository): ViewModel() {

    private var _uiState = MutableStateFlow<ResultData<String>>(ResultData.empty(null))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAuthResult().collectLatest { result ->
                when(result) {
                    is ResultData.Success -> {
                        _uiState.value = ResultData.success(result.value)
                    }
                    is ResultData.Loading -> {
                        _uiState.value = ResultData.loading(null)
                    }
                    is ResultData.Failure -> {
                        _uiState.value = ResultData.failure(result.message)
                    }
                }
            }
        }
    }

    fun userAuthorization(email: String, password: String){
        if (isValidSignUpDetails(email, password)) {
            val authData = AuthData(
                email = email,
                password = password)
            repository.userAuthorization(authData)
        }
    }

    private fun isValidSignUpDetails(email: String, password: String): Boolean {
        var result = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ResultData.failure("Enter email")
            result = false
        } else if (password.trim().isEmpty()) {
            _uiState.value = ResultData.failure("Enter password")
            result = false
        }
        return result
    }
}