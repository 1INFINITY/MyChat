package com.example.mychat.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.domain.usecase.CreateNewChatUseCase
import com.example.mychat.domain.usecase.GetCachedUserUseCase
import com.example.mychat.domain.usecase.UploadUserListUseCase
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.ChatContract
import com.example.mychat.presentation.viewmodels.сontracts.SelectUserContract
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectUserViewModel(
    private val getCachedUserUseCase: GetCachedUserUseCase,
    private val uploadUserListUseCase: UploadUserListUseCase,
    private val createNewChatUseCase: CreateNewChatUseCase
) :
    BaseViewModel<SelectUserContract.Event, SelectUserContract.State, SelectUserContract.Effect>() {

    override fun createInitialState(): SelectUserContract.State {
        return SelectUserContract.State(
            recyclerViewState = SelectUserContract.RecyclerViewState.Idle
        )
    }

    override fun handleEvent(event: SelectUserContract.Event) {
        when (event) {
            is SelectUserContract.Event.OnUserClicked -> {
                onUserClicked(event.user)
            }
            is SelectUserContract.Event.OnBackButtonClicked -> {
                // Todo: make it in correct way
                setEffect { SelectUserContract.Effect.ToBackFragment }
            }
        }
    }

    init {
        viewModelScope.launch {
            uploadUserListUseCase.execute().collectLatest { state ->
                when (state) {
                    is ResultData.Success -> {
                        setState {
                            copy(
                                recyclerViewState = SelectUserContract.RecyclerViewState.Success(
                                    users = state.value
                                )
                            )
                        }
                    }
                    is ResultData.Loading -> {
                        setState {
                            copy(
                                recyclerViewState = SelectUserContract.RecyclerViewState.Loading
                            )
                        }
                    }
                    is ResultData.Failure -> {
                        setState {
                            copy(
                                recyclerViewState = SelectUserContract.RecyclerViewState.Error(
                                    errorMessage = state.message)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onUserClicked(userReceiver: User) {
        val userMain: User = getCachedUserUseCase.execute()
        val users = listOf(userMain, userReceiver)

        viewModelScope.launch {
            createNewChatUseCase.execute(userSender = userMain, users = users).collect { result ->
                when (result) {
                    is ResultData.Success -> {
                        setState {
                            copy(
                                recyclerViewState = SelectUserContract.RecyclerViewState.Loading
                            )
                        }
                        setEffect { SelectUserContract.Effect.ToChatFragment(chat = result.value) }
                    }
                    is ResultData.Loading -> {
                        setState {
                            copy(
                                recyclerViewState = SelectUserContract.RecyclerViewState.Loading
                            )
                        }
                    }
                    is ResultData.Failure -> {
                        setState {
                            copy(
                                recyclerViewState = SelectUserContract.RecyclerViewState.Error(
                                    errorMessage = result.message)
                            )
                        }
                    }
                }

            }
        }
    }
}