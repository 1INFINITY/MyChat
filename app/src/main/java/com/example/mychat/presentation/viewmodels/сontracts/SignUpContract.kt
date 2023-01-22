package com.example.mychat.presentation.viewmodels.сontracts

import android.graphics.Bitmap
import android.net.Uri
import com.example.mychat.presentation.viewmodels.base.UiEffect
import com.example.mychat.presentation.viewmodels.base.UiEvent
import com.example.mychat.presentation.viewmodels.base.UiState

class SignUpContract {
    // Events that user performed
    sealed class Event : UiEvent {
        object OnSignInTextClicked : Event()
        data class OnSignUpButtonClicked(
            val name: String,
            val email: String,
            val password: String,
            val confirmPassword: String,
        ) : Event()
        data class OnImageProfileSelected(
            val uri: Uri?,
            val image: Bitmap?
        ) : Event()
    }

    // Ui View States
    data class State(
        val profileImage: Uri?,
        val name: String?,
        val email: String?,
        val password: String?,
        val confirmPassword: String?,
        val fragmentViewState: ViewState,
    ) : UiState

    // View State that related to Random Number
    sealed class ViewState {
        object Idle : ViewState()
        object Loading : ViewState()
        object Success : ViewState()
        data class Error(val errorMessage: String) : ViewState()
    }

    // Side effects
    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
        object ToSignInFragment : Effect()
    }

}