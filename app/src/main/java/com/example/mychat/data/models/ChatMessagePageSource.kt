package com.example.mychat.data.models

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.google.firebase.firestore.Query
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ChatMessagePageSource @AssistedInject constructor(
    private val repository: UserRepository,
    @Assisted("chat") private val chat: Chat,
) : PagingSource<Int, ChatMessage>() {
    override fun getRefreshKey(state: PagingState<Int, ChatMessage>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatMessage> {

        val page: Int = params.key ?: 0
        val pageSize: Int = params.loadSize

        val response = repository.fetchPagingMessages(chat = chat, page = page, pageSize = pageSize)

        if (response is ResultData.Success) {
            val messages = response.value
            val nextKey = if (messages.size < pageSize) null else page + 1
            val prevKey = null//if (page == 1) null else page - 1
            return LoadResult.Page(messages, prevKey, nextKey)
        } else {
            return LoadResult.Error(Exception("FIRESTORE ERROR"))
        }
    }
    @AssistedFactory
    interface Factory {

        fun create(@Assisted("chat") chat: Chat): ChatMessagePageSource
    }

}