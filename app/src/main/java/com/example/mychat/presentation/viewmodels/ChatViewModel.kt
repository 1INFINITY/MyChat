package com.example.mychat.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.ChatContract
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(
    private val repository: UserRepository,
) :
    BaseViewModel<ChatContract.Event, ChatContract.State, ChatContract.Effect>() {


    private lateinit var chat: Chat
    private lateinit var userSender: User
    private lateinit var userReceiver: User
    private var chatListenJob: Job? = null
    private var messages: MutableList<ChatMessage> = mutableListOf()

    override fun createInitialState(): ChatContract.State {

        return ChatContract.State(
            sender = null,
            chatName = "",
            recyclerViewState = ChatContract.RecyclerViewState.Idle,
            changeMessageState = ChatContract.ChangeMessageState.Idle,
        )
    }

    override fun handleEvent(event: ChatContract.Event) {
        when (event) {
            is ChatContract.Event.MessageSent -> {
                if (event.message != "")
                    trySendMessage(message = event.message)
            }
            is ChatContract.Event.OnBackButtonClicked -> {
                setEffect { ChatContract.Effect.ToBackFragment }
            }
            is ChatContract.Event.OnMessageChangeClicked -> {
                setState {
                    copy(
                        changingMessage = event.message,
                        changeMessageState = ChatContract.ChangeMessageState.Changing
                    )
                }
            }
            is ChatContract.Event.OnMessageDeleteClicked -> {
               deleteMessage(message = event.message)
            }
            is ChatContract.Event.OnConfirmButtonClicked -> {
                val newMessage = uiState.value.changingMessage!!.copy(message = event.changedMessage)
                changeMessage(message = newMessage)
                setState {
                    copy(
                        changingMessage = null,
                        changeMessageState = ChatContract.ChangeMessageState.Idle
                    )
                }
            }
            is ChatContract.Event.OnCancelChangeClicked -> {
                setState {
                    copy(
                        changingMessage = null,
                        changeMessageState = ChatContract.ChangeMessageState.Idle
                    )
                }
            }
        }
    }

    fun listenChatWithId(chatId: String) {
        chatListenJob?.let {
            if(chatId == chat.id)
                return
            it.cancel()
        }
        userSender = repository.getCachedUser()
        chatListenJob = viewModelScope.launch {
            repository.openChat(userSender = userSender, chatId = chatId).collect {
                when(it) {
                    is ResultData.Success -> {
                        chat = it.value
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
                                recyclerViewState = ChatContract.RecyclerViewState.Error(it.message)
                            )
                        }
                    }
                }
            }
            userReceiver = chat.userReceiver
            setState {
                copy(
                    sender = userSender,
                    chatName = userReceiver.name
                )
            }
            repository.listenMessages(chat = chat).collect { result ->
                when (result) {
                    is ResultData.Removed -> {
                        result.value.forEach { removedMessage ->
                            val index = messages.indexOfFirst { it.id == removedMessage.id }
                            if (index != -1){
                                messages.removeAt(index)
                            }
                        }
                        setState {
                            copy(
                                recyclerViewState = ChatContract.RecyclerViewState.Success(
                                    chatMessages = messages.toList())
                            )
                        }
                    }
                    is ResultData.Update -> {
                        result.value.forEach { updatedMessage ->
                            val index = messages.indexOfFirst { it.id == updatedMessage.id }
                            if (index != -1){
                                messages[index] = updatedMessage
                            }
                        }
                        setState {
                            copy(
                                recyclerViewState = ChatContract.RecyclerViewState.Success(
                                    chatMessages = messages.toList())
                            )
                        }
                    }
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

    private fun deleteMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.deleteMessage(chatMessage = message).collect {
                when(it) {
                    is ResultData.Success -> Log.d("Chat", "Message deleted ${it.value.id}")
                }
            }
        }
    }
    private fun changeMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.changeMessage(chatMessage = message).collect {
                when(it) {
                    is ResultData.Success -> Log.d("Chat", "Message changed ${it.value.id}")
                }
            }
        }
    }
    private fun trySendMessage(message: String) {
        val message = ChatMessage(
            id = "",
            chat = chat,
            sender = userSender,
            message = message,
            date = Date())
        viewModelScope.launch {
            repository.sendMessage(message).collect {
                when(it) {
                    is ResultData.Success -> Log.d("Chat", it.value.toString())
                }
            }
        }
    }
}