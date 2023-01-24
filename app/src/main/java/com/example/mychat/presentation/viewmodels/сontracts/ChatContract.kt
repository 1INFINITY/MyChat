package com.example.mychat.presentation.viewmodels.сontracts

import android.graphics.Bitmap
import androidx.fragment.app.Fragment
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.presentation.viewmodels.base.UiEffect
import com.example.mychat.presentation.viewmodels.base.UiEvent
import com.example.mychat.presentation.viewmodels.base.UiState

class ChatContract {
    // Events that user performed
    sealed class Event : UiEvent {
        object OnBackButtonClicked : Event()
        data class MessageSent(val message: String) : Event()
        data class OnMessageDeleteClicked(val message: ChatMessage) : Event()
        data class OnMessageChangeClicked(val message: ChatMessage) : Event()
        data class OnConfirmButtonClicked(val changedMessage: String) : Event()
        object OnCancelChangeClicked : Event()
    }

    // Ui View States
    data class State(
        val sender: User? = null,
        val chatName: String? = null,
        val recyclerViewState: RecyclerViewState,
        val changingMessage: ChatMessage? = null,
        val changeMessageState: ChangeMessageState
    ) : UiState

    // View State that related to Random Number
    sealed class RecyclerViewState {
        object Idle : RecyclerViewState()
        object Loading : RecyclerViewState()
        data class Success(val chatMessages: List<ChatMessage>) : RecyclerViewState()
        data class Error(val errorMessage: String) : RecyclerViewState()
    }

    sealed class ChangeMessageState {
        object Changing : ChangeMessageState()
        object Idle : ChangeMessageState()
    }

    // Side effects
    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
        data class ShowRecyclerFailure(val message: String) : Effect()
        object ToBackFragment : Effect()
    }

}