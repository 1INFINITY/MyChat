package com.example.mychat.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemContainerUserBinding
import com.example.mychat.domain.models.User
import com.example.mychat.presentation.listeners.UserListener

class UserAdapter(private val userListener: UserListener ) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val differ: AsyncListDiffer<User> = AsyncListDiffer(this, DiffCallback())

    fun submitList(list: List<User>) {
        differ.submitList(list)
    }

    fun currentList(): List<User> {
        return differ.currentList
    }

    class UserViewHolder(val binding: ItemContainerUserBinding): RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = differ.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContainerUserBinding.inflate(inflater, parent, false)

        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val person = differ.currentList[position]
        val context = holder.itemView.context

        with(holder.binding) {

            textName.text = person.name
            textEmail.text = person.email
            imageProfile.setImageBitmap(person.image)
            root.setOnClickListener { userListener.onUserClicked(person)}
        }
    }
    private class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem.id == newItem.id
    }

}