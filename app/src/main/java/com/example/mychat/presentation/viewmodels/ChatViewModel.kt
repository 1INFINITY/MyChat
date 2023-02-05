package com.example.mychat.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mychat.data.models.ChatMessagePageSource
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.domain.usecase.*
import com.example.mychat.presentation.adapters.ChatPagingAdapter
import com.example.mychat.presentation.viewmodels.base.BaseViewModel
import com.example.mychat.presentation.viewmodels.сontracts.ChatContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(
    private val repository: UserRepository,
    private val getCachedUserUseCase: GetCachedUserUseCase,
    private val loadChatUseCase: LoadChatUseCase,
    private val observeChatUseCase: ObserveChatUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val changeMessageUseCase: ChangeMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val pagingSourceFactory: ChatMessagePageSource.Factory,
) :
    BaseViewModel<ChatContract.Event, ChatContract.State, ChatContract.Effect>() {


    private lateinit var chat: Chat
    private lateinit var userSender: User
    private lateinit var userReceiver: User
    private var chatListenJob: Job? = null
    private var messages: MutableList<ChatMessage> = mutableListOf()

    val pagingMessages: StateFlow<PagingData<ChatMessage>> by lazy(LazyThreadSafetyMode.NONE) {
        repository
            .getMessages(chat = chat)
            .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())
    }
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
                val newMessage =
                    uiState.value.changingMessage!!.copy(message = event.changedMessage)
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
            if (chatId == chat.id)
                return
            it.cancel()
        }
        userSender = getCachedUserUseCase.execute()
        chatListenJob = viewModelScope.launch {

            loadChatUseCase.execute(userSender = userSender, chatId = chatId).collect {
                when (it) {
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
//            observeChatUseCase.execute(chat = chat).collect { result ->
//                when (result) {
//                    is ResultData.Removed -> {
//                        val index = messages.indexOfFirst { it.id == result.value.id }
//                        messages.removeAt(index)
//                        setState {
//                            copy(
//                                recyclerViewState = ChatContract.RecyclerViewState.Success(
//                                    chatMessages = messages.toList())
//                            )
//                        }
//                    }
//                    is ResultData.Update -> {
//                        val index = messages.indexOfFirst { it.id == result.value.id }
//                        messages[index] = result.value
//                        setState {
//                            copy(
//                                recyclerViewState = ChatContract.RecyclerViewState.Success(
//                                    chatMessages = messages.toList())
//                            )
//                        }
//                    }
//                    is ResultData.Success -> {
//                        messages.add(result.value)
//                        messages.sortBy { it.date }
//                        setState {
//                            copy(
//                                recyclerViewState = ChatContract.RecyclerViewState.Success(
//                                    chatMessages = messages.toList())
//                            )
//                        }
//                    }
//                    is ResultData.Loading -> {
//                        setState {
//                            copy(
//                                recyclerViewState = ChatContract.RecyclerViewState.Loading
//                            )
//                        }
//                    }
//                    is ResultData.Failure -> {
//                        setState {
//                            copy(
//                                recyclerViewState = ChatContract.RecyclerViewState.Error(result.message)
//                            )
//                        }
//                    }
//                }
//            }
        }
    }

    private fun deleteMessage(message: ChatMessage) {
        viewModelScope.launch {
            deleteMessageUseCase.execute(chatMessage = message).collect {
                when (it) {
                    is ResultData.Success -> Log.d("Chat", "Message deleted ${it.value.id}")
                }
            }
        }
    }

    private fun changeMessage(message: ChatMessage) {
        viewModelScope.launch {
            changeMessageUseCase.execute(chatMessage = message).collect {
                when (it) {
                    is ResultData.Success -> Log.d("Chat", "Message changed ${it.value.id}")
                }
            }
        }
    }

    private fun trySendMessage(message: String) {
        val message = ChatMessage(
            id = "",
            sender = userSender,
            message = message,
            date = Date())
        viewModelScope.launch {
            sendMessageUseCase.execute(chatMessage = message, chat = chat).collect {
                when (it) {
                    is ResultData.Success -> Log.d("Chat", it.value.toString())
                }
            }
        }
    }
}