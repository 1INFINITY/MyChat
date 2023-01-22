package com.example.mychat.presentation.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.SignInContract
import kotlinx.coroutines.launch

class SignInViewModel(private val repository: UserRepository) :
    BaseViewModel<SignInContract.Event, SignInContract.State, SignInContract.Effect>() {

    override fun createInitialState(): SignInContract.State {
        return SignInContract.State(
            email = "",
            password = "",
            fragmentViewState = SignInContract.ViewState.Idle
        )
    }

    override fun handleEvent(event: SignInContract.Event) {
        when (event) {
            is SignInContract.Event.OnSignInButtonClicked -> {
                userAuthorization(
                    email = event.email,
                    password = event.password,
                )
            }
            is SignInContract.Event.OnSignUpTextClicked -> {
                setEffect { SignInContract.Effect.ToSignUpFragment }
            }
        }
    }

    private fun userAuthorization(email: String, password: String) {

        if (!isValidSignUpDetails(email, password))
            return

        val authData = AuthData(
            email = email,
            password = password)
        viewModelScope.launch {
            repository.userAuthorization(authData).collect { result ->
                when (result) {
                    is ResultData.Success -> {
                        setState {
                            copy(fragmentViewState = SignInContract.ViewState.Success)
                        }
                        setEffect {
                            SignInContract.Effect.ToUserFragment
                        }
                    }
                    is ResultData.Loading -> {
                        setState {
                            copy(fragmentViewState = SignInContract.ViewState.Loading)
                        }
                    }
                    is ResultData.Failure -> {
                        setState {
                            copy(fragmentViewState = SignInContract.ViewState.Error(errorMessage = result.message))
                        }
                        setEffect {
                            SignInContract.Effect.ShowToast(message = "Неправильная почта/пароль")
                        }
                    }
                }
            }
        }
    }

    private fun isValidSignUpDetails(email: String, password: String): Boolean {
        var result = true
        var message = ""
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            message = "Enter email"
            result = false
        } else if (password.trim().isEmpty()) {
            message = "Enter password"
            result = false
        }
        if (!result)
            setEffect { SignInContract.Effect.ShowToast(message = message) }
        return result
    }
}