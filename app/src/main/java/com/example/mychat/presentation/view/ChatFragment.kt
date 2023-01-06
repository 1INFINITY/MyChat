package com.example.mychat.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mychat.R
import com.example.mychat.databinding.FragmentChatBinding
import com.example.mychat.databinding.FragmentSelectUserBinding
import com.example.mychat.domain.models.User

class ChatFragment(val user: User) : Fragment() {

    private lateinit var binding: FragmentChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setListeners()
        loadReceiverData()
    }
    private fun loadReceiverData() {
        binding.textName.text = user.name
    }
    private fun setListeners() {
        binding.imageBack.setOnClickListener { requireActivity().onBackPressed() }
    }

}