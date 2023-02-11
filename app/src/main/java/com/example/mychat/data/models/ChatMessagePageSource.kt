package com.example.mychat.data.models

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mychat.data.storage.firebase.FireBaseUserStorage
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.google.firebase.firestore.DocumentSnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ChatMessagePageSource @AssistedInject constructor(
    private val fireBaseUserStorage: FireBaseUserStorage,
    @Assisted("chat") private val chat: Chat,
) : PagingSource<DocumentSnapshot, ChatMessage>() {

    private var prevOffset: DocumentSnapshot? = null
    private var nextOffset: DocumentSnapshot? = null

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, ChatMessage>): DocumentSnapshot? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey ?: page.nextKey
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, ChatMessage> {

        val limit = params.loadSize
        return try {
            val messages = fetchMessages(limit = limit)
            val nextKey = null
            val prevKey = if (messages.size < limit) null else nextOffset
            return LoadResult.Page(messages, prevKey, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun fetchMessages(limit: Int): List<ChatMessage> {
        val result =
            fireBaseUserStorage.fetchMessages(chat = chat, limit = limit, offset = nextOffset)
        prevOffset = result.prevOffset
        nextOffset = result.nextOffset

        return result.list.map { chatMessageFirestore ->
            val userSender: User =
                fireBaseUserStorage.findUserByRef(chatMessageFirestore.senderId!!)
            ChatMessage(
                id = chatMessageFirestore.id!!,
                sender = userSender,
                message = chatMessageFirestore.message!!,
                date = chatMessageFirestore.timestamp!!
            )
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("chat") chat: Chat,
        ): ChatMessagePageSource
    }

}