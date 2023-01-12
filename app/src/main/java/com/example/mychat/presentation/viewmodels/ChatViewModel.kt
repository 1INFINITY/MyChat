package com.example.mychat.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.ChatContract
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(
    private val chat: Chat,
    private val repository: UserRepository,
) :
    BaseViewModel<ChatContract.Event, ChatContract.State, ChatContract.Effect>() {

    private lateinit var userSender: User
    private lateinit var userReceiver: User
    private var messages: MutableList<ChatMessage> = mutableListOf()

    override fun createInitialState(): ChatContract.State {

        return ChatContract.State(
            sender = null,
            chatName = "",
            recyclerViewState = ChatContract.RecyclerViewState.Idle
        )
    }

    override fun handleEvent(event: ChatContract.Event) {
        when (event) {
            is ChatContract.Event.MessageSent -> {
                if (event.message != "")
                    trySendMessage(message = event.message)
            }
            is ChatContract.Event.OnBackButtonClicked -> {
                // Todo: make it in correct way
                setEffect { ChatContract.Effect.ChangeFragment(null) }
            }
        }
    }

    init {
        viewModelScope.launch {

            userSender = repository.getCachedUser()
            userReceiver = chat.users.find { it.id != userSender.id } ?: userSender
            setState {
                copy(
                    sender = userSender,
                    chatName = chat.name ?: userReceiver.name
                )
            }
            repository.listenMessages(chat = chat).collect { result ->
                when (result) {
                    is ResultData.Success -> {
                        result.value.forEach {
                            messages.add(it)
                        }
                        messages.sortBy { it.date }
                        setState {
                            copy(
                                recyclerViewState = ChatContract.RecyclerViewState.Success(
                                    chatMessages = messages.toList())
                            )
                        }
                    }
                    is ResultData.Loading -> {
                        setState {
                            copy(
                                recyclerViewState = ChatContract.RecyclerViewState.Loading
                            )
                        }
                    }
                    is ResultData.Failure -> {
                        setState {
                            copy(
                                recyclerViewState = ChatContract.RecyclerViewState.Error(result.message)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun trySendMessage(message: String) {
        val message = ChatMessage(
            chat = chat,
            sender = userSender,
            message = message,
            date = Date())
        repository.sendMessage(message)
    }
}