package com.example.mychat.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemContainerChatBinding
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.User
import com.example.mychat.presentation.listeners.ChatListener

class RecentChatsAdapter(private val mainUser: User, private val chatListener: ChatListener) :
    RecyclerView.Adapter<RecentChatsAdapter.ChatViewHolder>() {

    var chats: List<Chat> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class ChatViewHolder(val binding: ItemContainerChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = chats.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContainerChatBinding.inflate(inflater, parent, false)

        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        val context = holder.itemView.context

        val anotherUser = chat.userReceiver

        with(holder.binding) {
            imageProfile.setImageBitmap(anotherUser.image)
            textName.text = anotherUser.name
            textLatestMessage.text = chat.lastMessage
            root.setOnClickListener { chatListener.onChatClicked(chat) }
        }
    }

}