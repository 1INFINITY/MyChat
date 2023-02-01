package com.example.mychat.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemContainerChatBinding
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.presentation.listeners.ChatListener

class RecentChatsAdapter(private val mainUser: User, private val chatListener: ChatListener) :
    RecyclerView.Adapter<RecentChatsAdapter.ChatViewHolder>() {

    private val differ: AsyncListDiffer<Chat> = AsyncListDiffer(this, DiffCallback())

    fun submitList(list: List<Chat>) {
        differ.submitList(list)
    }

    fun currentList(): List<Chat> {
        return differ.currentList
    }

    class ChatViewHolder(val binding: ItemContainerChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = currentList().size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContainerChatBinding.inflate(inflater, parent, false)

        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = differ.currentList[position]
        val anotherUser = chat.userReceiver

        with(holder.binding) {
            imageProfile.setImageBitmap(anotherUser.image)
            textName.text = anotherUser.name
            textLatestMessage.text = chat.lastMessage
            root.setOnClickListener { chatListener.onChatClicked(chat) }
        }
    }
    private class DiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat) =
            oldItem.lastMessage == newItem.lastMessage
    }

}