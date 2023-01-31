package com.example.mychat.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.domain.usecase.GetCachedUserUseCase
import com.example.mychat.domain.usecase.ObserveChatsUseCase
import com.example.mychat.domain.usecase.SignOutUseCase
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.UserContract
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewModel(
    private val getCachedUserUseCase: GetCachedUserUseCase,
    private val observeChatsUseCase: ObserveChatsUseCase,
    private val signOutUseCase: SignOutUseCase
) :
    BaseViewModel<UserContract.Event, UserContract.State, UserContract.Effect>() {

    private var chats: MutableList<Chat> = mutableListOf()
    private lateinit var user: User

    /**
     * Create initial State of Views
     */
    override fun createInitialState(): UserContract.State {
        return UserContract.State(
            userName = "",
            profileImage = null,
            recyclerViewState = UserContract.RecyclerViewState.Idle
        )
    }

    override fun handleEvent(event: UserContract.Event) {
        when (event) {
            is UserContract.Event.OnSignOutClicked -> {
                signOut()
            }
            is UserContract.Event.OnChatClicked -> {
                val chat = event.chat
                setEffect { UserContract.Effect.ToChatFragment(chatId = chat.id) }
            }
            is UserContract.Event.OnBackButtonClicked -> {
                // Todo: make it in correct way
                setEffect { UserContract.Effect.ToBackFragment }
            }
            is UserContract.Event.OnFloatingButtonClicked -> {
                setEffect { UserContract.Effect.ToSelectUserFragment }
            }
        }
    }

    init {
        viewModelScope.launch {
            user = getCachedUserUseCase.execute()
            setState {
                copy(
                    userName = user.name,
                    profileImage = user.image,
                    recyclerViewState = UserContract.RecyclerViewState.Loading
                )
            }
            observeChatsUseCase.execute(userSender = user).collect { result ->
                when (result) {
                    is ResultData.Removed -> {
                        val index = chats.indexOfFirst { it.id == result.value.id }
                        if (index != -1) {
                            chats.removeAt(index)
                            setState {
                                copy(
                                    recyclerViewState = UserContract.RecyclerViewState.Success(chats.toList())
                                )
                            }
                        }
                    }
                    is ResultData.Update -> {
                        val index = chats.indexOfFirst { it.id == result.value.id }
                        if (index != -1) {
                            chats[index] = result.value
                            setState {
                                copy(
                                    recyclerViewState = UserContract.RecyclerViewState.Success(chats.toList())
                                )
                            }
                        }
                    }
                    is ResultData.Success -> {
                        chats.add(result.value)
                        setState {
                            copy(
                                recyclerViewState = UserContract.RecyclerViewState.Success(chats.toList())
                            )
                        }
                    }
                    is ResultData.Loading -> {
                        setState {
                            copy(
                                recyclerViewState = UserContract.RecyclerViewState.Loading
                            )
                        }
                    }
                    is ResultData.Failure -> {
                        setState {
                            copy(
                                recyclerViewState = UserContract.RecyclerViewState.Error(result.message)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase.execute().collect { result ->
                when (result) {
                    is ResultData.Success -> {
                        setEffect { UserContract.Effect.ToBackFragment }
                    }
                    is ResultData.Loading -> {
                        setState {
                            copy(
                                recyclerViewState = UserContract.RecyclerViewState.Loading
                            )
                        }
                    }
                    is ResultData.Failure -> {
                        setState {
                            copy(
                                recyclerViewState = UserContract.RecyclerViewState.Error(result.message)
                            )
                        }
                    }
                }
            }
        }
    }
}