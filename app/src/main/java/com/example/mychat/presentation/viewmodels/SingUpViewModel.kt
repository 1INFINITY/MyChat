package com.example.mychat.presentation.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.SignUpContract
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.io.IOException

class SingUpViewModel(private val repository: UserRepository) :
    BaseViewModel<SignUpContract.Event, SignUpContract.State, SignUpContract.Effect>() {

    private var profileImageBitmap: Bitmap? = null

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
            is SignUpContract.Event.OnProfileImageClicked -> {

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

    fun setProfileImage(selectedFileUri: Uri, context: Context) {
        profileImageBitmap = uriToBitmap(selectedFileUri = selectedFileUri, context = context)
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
            image = profileImageBitmap!!,
            name = name,
            email = email,
            password = password
        )
    }

    // TODO: Try implement func without context
    private fun uriToBitmap(selectedFileUri: Uri, context: Context): Bitmap? {
        try {
            val parcelFileDescriptor =
                context.contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun isValidSignUpDetails(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): Boolean {
        var result = true
        var message = ""
        if (profileImageBitmap == null) {
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