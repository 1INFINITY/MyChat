package com.example.mychat.presentation.viewmodels.сontracts

import android.graphics.Bitmap
import androidx.fragment.app.Fragment
import com.example.mychat.domain.models.Chat
import com.example.mychat.presentation.viewmodels.base.UiEffect
import com.example.mychat.presentation.viewmodels.base.UiEvent
import com.example.mychat.presentation.viewmodels.base.UiState

class UserContract {

    // Events that user performed
    sealed class Event : UiEvent {
        object OnSignOutClicked : Event()
        object OnBackButtonClicked : Event()
        object OnFloatingButtonClicked : Event()
        data class OnChatClicked(val chat: Chat) : Event()
    }

    // Ui View States
    data class State(
        val userName: String? = null,
        val profileImage: Bitmap? = null,
        val recyclerViewState: RecyclerViewState,
    ) : UiState

    // View State that related to Random Number
    sealed class RecyclerViewState {
        object Idle : RecyclerViewState()
        object Loading : RecyclerViewState()
        data class Success(val chatsList: List<Chat>) : RecyclerViewState()
        data class Error(val errorMessage: String): RecyclerViewState()
    }

    // Side effects
    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
        data class ShowRecyclerFailure(val message: String) : Effect()
        object ToSelectUserFragment : Effect()
        data class ToChatFragment(val chatId: String) : Effect()
        object ToBackFragment : Effect()
    }

}