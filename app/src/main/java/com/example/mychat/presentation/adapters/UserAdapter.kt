package com.example.mychat.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemContainerUserBinding
import com.example.mychat.domain.models.User

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    var data : List<User> = emptyList()
        set(newValue){
            field = newValue
            notifyDataSetChanged()
        }

    class UserViewHolder(val binding: ItemContainerUserBinding): RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContainerUserBinding.inflate(inflater, parent, false)

        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val person = data[position]
        val context = holder.itemView.context

        with(holder.binding) {

            textName.text = person.name
            textEmail.text = person.email
            imageProfile.setImageBitmap(person.image)

        }
    }

}