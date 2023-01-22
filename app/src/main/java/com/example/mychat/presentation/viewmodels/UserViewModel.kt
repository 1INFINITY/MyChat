package com.example.mychat.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.UserContract
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) :
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
            user = getMainUser()
            setState {
                copy(
                    userName = user.name,
                    profileImage = user.image,
                    recyclerViewState = UserContract.RecyclerViewState.Loading
                )
            }
            repository.fetchChats(getMainUser()).collect { result ->
                when (result) {
                    is ResultData.Success -> {
                        chatsUpdate(updates = result.value)
                        setState {
                            copy(
                                recyclerViewState = UserContract.RecyclerViewState.Success(chats)
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

    fun signOut() {
        viewModelScope.launch {
            repository.signOut().collect { result ->
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

    fun getMainUser(): User {
        return repository.getCachedUser()
    }


    private fun chatsUpdate(updates: List<Chat>) {
        updates.map { chat ->
            val index = chats.indexOfFirst { it.id == chat.id }
            if (index == -1)
                chats.add(chat)
            else
                chats[index] = chat
        }
    }

}