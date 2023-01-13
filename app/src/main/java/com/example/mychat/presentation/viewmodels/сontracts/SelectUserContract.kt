package com.example.mychat.presentation.viewmodels.сontracts

import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.User
import com.example.mychat.presentation.viewmodels.base.UiEffect
import com.example.mychat.presentation.viewmodels.base.UiEvent
import com.example.mychat.presentation.viewmodels.base.UiState

class SelectUserContract {
    // Events that user performed
    sealed class Event : UiEvent {
        object OnBackButtonClicked : Event()
        data class OnUserClicked(val user: User) : Event()
    }

    // Ui View States
    data class State(
        val recyclerViewState: RecyclerViewState,
    ) : UiState

    // View State that related to Random Number
    sealed class RecyclerViewState {
        object Idle : RecyclerViewState()
        object Loading : RecyclerViewState()
        data class Success(val users: List<User>) : RecyclerViewState()
        data class Error(val errorMessage: String) : RecyclerViewState()
    }

    // Side effects
    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
        data class ShowRecyclerFailure(val message: String) : Effect()
        object ToBackFragment : Effect()
        data class ToChatFragment(val chat: Chat) : Effect()
    }

}