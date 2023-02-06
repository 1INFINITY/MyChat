package com.example.mychat.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.mychat.data.storage.firebase.FireBaseUserStorage
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.google.firebase.firestore.DocumentSnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@ExperimentalPagingApi
class MessagesRemoteMediator @AssistedInject constructor(
    private val fireBaseUserStorage: FireBaseUserStorage,
    @Assisted private val chat: Chat,
) : RemoteMediator<Int, ChatMessage>() {

    private var pageIndex = 0
    private var offset: DocumentSnapshot? = null

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ChatMessage>,
    ): MediatorResult {

        pageIndex = getPageIndex(loadType) ?: return MediatorResult.Success(endOfPaginationReached = true)

        val limit = state.config.pageSize

        return try {
            val messages = fetchMessages(limit = limit)
//            if (loadType = LoadType.REFRESH) {
//                messagesDao.refresh(chat, messages)
//            } else {
//                messagesDao.save(messages)
//            }
            MediatorResult.Success(
                endOfPaginationReached = messages.size < limit
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private fun getPageIndex(loadType: LoadType): Int? {
        pageIndex = when(loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> --pageIndex
            LoadType.APPEND -> ++pageIndex
        }
        return pageIndex
    }

    private suspend fun fetchMessages(limit: Int): List<ChatMessage> {

        val result = fireBaseUserStorage.fetchMessages(chat = chat, limit = limit, offset = offset)
        offset = result.nextOffset

        return result.list.map { chatMessageFirestore ->
            val userSender: User = fireBaseUserStorage.findUserByRef(chatMessageFirestore.senderId!!)
            ChatMessage(
                id = chatMessageFirestore.id!!,
                sender = userSender,
                message = chatMessageFirestore.message!!,
                date = chatMessageFirestore.timestamp!!
            )
        }
    }
    @AssistedFactory
    interface Factory{
        fun create(chat: Chat): MessagesRemoteMediator
    }
}