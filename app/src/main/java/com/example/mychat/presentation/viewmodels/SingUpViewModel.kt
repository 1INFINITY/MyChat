package com.example.mychat.presentation.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychat.R
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.io.IOException

class SingUpViewModel(private val repository: UserRepository) : ViewModel() {
    private var _uiState = MutableStateFlow<ResultData<String>>(ResultData.empty(null))
    val uiState = _uiState.asStateFlow()

    private var profileImageBitmap: Bitmap? = null

    init {
        viewModelScope.launch {
            repository.observeAuthResult().collectLatest { result ->
                when (result) {
                    is ResultData.Success -> {
                        _uiState.value = ResultData.success(result.value)
                    }
                    is ResultData.Loading -> {
                        _uiState.value = ResultData.loading(null)
                    }
                    is ResultData.Failure -> {
                        _uiState.value = ResultData.failure(result.message)
                    }
                    else -> {}
                }
            }
        }
    }

    fun userRegistration(name: String, email: String, password: String, confirmPassword: String) {
        if (isValidSignUpDetails(
                name = name,
                email = email,
                password = password,
                confirmPassword = confirmPassword)
        ) {
            val user = User(
                image = profileImageBitmap!!,
                name = name,
                email = email,
                password = password
            )
            repository.userRegistration(user)
        }
    }

    fun setProfileImage(selectedFileUri: Uri, context: Context) {
        profileImageBitmap = uriToBitmap(selectedFileUri = selectedFileUri, context = context)
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
        if (profileImageBitmap == null) {
            _uiState.value = ResultData.failure("Select a profile image")
            result = false
        } else if (name.trim().isEmpty()) {
            _uiState.value = ResultData.failure("Enter name")
            result = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ResultData.failure("Enter email")

            result = false
        } else if (password.trim().isEmpty()) {
            _uiState.value = ResultData.failure("Enter password")

            result = false
        } else if (confirmPassword.trim().isEmpty()) {
            _uiState.value = ResultData.failure("Confirm you passwords")
            result = false
        } else if (confirmPassword != password) {
            _uiState.value = ResultData.failure("Password and confirm must be same")
            result = false
        }
        return result
    }
}