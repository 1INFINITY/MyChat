package com.example.mychat.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemContainerReceivedBinding
import com.example.mychat.databinding.ItemContainerSentMessageBinding
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.presentation.listeners.ChatMessageListener
import java.text.SimpleDateFormat
import java.util.*

class ChatPagingAdapter(
    private val context: Context,
    private val sender: User,
    private val messageListener: ChatMessageListener,
) :
    PagingDataAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffUtilCallBack()) {

    private val layoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            SENT -> {
                val binding = ItemContainerSentMessageBinding.inflate(layoutInflater, parent, false)
                SentMessageViewHolder(binding)
            }
            RECEIVED -> {
                val binding = ItemContainerReceivedBinding.inflate(layoutInflater, parent, false)
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)?.sender?.id) {
            sender.id -> SENT
            null -> NULL
            else -> RECEIVED
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == SENT){
            (holder as SentMessageViewHolder).setData(getItem(position), messageListener)
            return
        }
        if (getItemViewType(position) == RECEIVED){
            (holder as ReceivedMessageViewHolder).setData(getItem(position))
            return
        }
        throw(IllegalArgumentException("Illegal view type"))
    }

    companion object {
        const val SENT = 1
        const val RECEIVED = 2
        const val NULL = 3
    }
}


class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun setData(msg: ChatMessage?, listener: ChatMessageListener) {
        with(binding) {
            textMessage.text = msg?.message
            textDateTime.text = msg?.let { formatDate(it.date) }
            root.setOnClickListener {
                msg?.let { listener.onChatMessageClicked(msg) }
            }
        }
    }

    // Todo: Refactor (this function shouldn't be here)
    private fun formatDate(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }
}

class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun setData(msg: ChatMessage?) {
        with(binding) {
            textMessage.text = msg?.message
            textDateTime.text = msg?.let { formatDate(it.date) }
            imageProfile.setImageBitmap(msg?.sender?.image)
        }
    }

    // Todo: Refactor (this function shouldn't be here)
    private fun formatDate(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }
}

private class ChatMessageDiffUtilCallBack : DiffUtil.ItemCallback<ChatMessage>() {

    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
        oldItem.message == newItem.message && oldItem.date == newItem.date

}