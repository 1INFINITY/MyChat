package com.example.mychat.presentation.viewmodels

import android.graphics.Bitmap
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.SignUpContract
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignUpViewModel(private val repository: UserRepository) :
    BaseViewModel<SignUpContract.Event, SignUpContract.State, SignUpContract.Effect>() {

    private var profileImage: Bitmap? = null

    override fun createInitialState(): SignUpContract.State {
        return SignUpContract.State(
            profileImage = null,
            name = null,
            email = null,
            password = null,
            confirmPassword = null,
            fragmentViewState = SignUpContract.ViewState.Idle
        )
    }

    override fun handleEvent(event: SignUpContract.Event) {
        when (event) {
            is SignUpContract.Event.OnImageProfileSelected -> {
                profileImage = event.image
                setState {
                    copy(profileImage = event.uri)
                }
            }
            is SignUpContract.Event.OnSignUpButtonClicked -> {
                userRegistration(
                    name = event.name,
                    email = event.email,
                    password = event.password,
                    confirmPassword = event.confirmPassword
                )
            }
            is SignUpContract.Event.OnSignInTextClicked -> {
                setEffect { SignUpContract.Effect.ToSignInFragment }
            }
        }
    }

    private fun userRegistration(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) {
        val user: User = makeUser(name, email, password, confirmPassword) ?: return

        viewModelScope.launch {
            repository.userRegistration(user = user).collectLatest { result ->
                when (result) {
                    is ResultData.Success -> {
                        setState {
                            copy(fragmentViewState = SignUpContract.ViewState.Success)
                        }
                        setEffect { SignUpContract.Effect.ToSignInFragment }
                    }
                    is ResultData.Loading -> {
                        setState {
                            copy(fragmentViewState = SignUpContract.ViewState.Loading)
                        }
                    }
                    is ResultData.Failure -> {
                        setState {
                            copy(fragmentViewState = SignUpContract.ViewState.Error(result.message))
                        }
                    }
                }
            }
        }
    }

    private fun makeUser(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): User? {
        if (
            !isValidSignUpDetails(
                name = name,
                email = email,
                password = password,
                confirmPassword = confirmPassword)
        ) {

            return null
        }

        return User(
            id = "",
            image = profileImage!!,
            name = name,
            email = email,
            password = password
        )
    }


    private fun isValidSignUpDetails(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): Boolean {
        var result = true
        var message = ""
        if (profileImage == null) {
            message = "Select a profile image"
            result = false
        } else if (name.trim().isEmpty()) {
            message = "Enter name"
            result = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            message = "Enter email"
            result = false
        } else if (password.trim().isEmpty()) {
            message = "Enter password"
            result = false
        } else if (confirmPassword.trim().isEmpty()) {
            message = "Confirm you passwords"
            result = false
        } else if (confirmPassword != password) {
            message = "Password and confirm must be same"
            result = false
        }
        if (!result)
            setEffect { SignUpContract.Effect.ShowToast(message = message) }
        return result
    }
}