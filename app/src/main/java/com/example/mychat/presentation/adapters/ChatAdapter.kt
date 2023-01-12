package com.example.mychat.presentation.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemContainerReceivedBinding
import com.example.mychat.databinding.ItemContainerSentMessageBinding
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val sender: User,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var messages: List<ChatMessage> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }


    class SentMessageViewHolder(val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(msg: ChatMessage) {
            binding.textMessage.text = msg.message
            binding.textDateTime.text = formatDate(msg.date)
        }
        private fun formatDate(date: Date): String {
            return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
        }
    }

    class ReceivedMessageViewHolder(val binding: ItemContainerReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(msg: ChatMessage) {
            binding.textMessage.text = msg.message
            binding.textDateTime.text = formatDate(msg.date)
            binding.imageProfile.setImageBitmap(msg.sender.image)
        }
        private fun formatDate(date: Date): String {
            return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ViewType.SENT.type) {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemContainerSentMessageBinding.inflate(inflater, parent, false)
            SentMessageViewHolder(binding)
        } else {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemContainerReceivedBinding.inflate(inflater, parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = messages.size


    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender.id == sender.id)
            ViewType.SENT.type
        else
            ViewType.RECEIVED.type
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ViewType.SENT.type) {
            (holder as SentMessageViewHolder).setData(messages[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(messages[position])
        }
    }

    companion object {
        enum class ViewType(val type: Int) {
            SENT(1),
            RECEIVED(2)
        }
    }
}