package com.example.mychat.presentation.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mychat.R
import com.example.mychat.databinding.FragmentChatBinding
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.presentation.adapters.ChatAdapter
import com.example.mychat.presentation.app.App
import com.example.mychat.presentation.listeners.ChatMessageListener
import com.example.mychat.presentation.viewmodels.ChatViewModel
import com.example.mychat.presentation.viewmodels.ViewModelFactory
import com.example.mychat.presentation.viewmodels.сontracts.ChatContract
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class ChatFragment : Fragment(), ChatMessageListener {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var vm: ChatViewModel
    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter
    private val args: ChatFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().applicationContext as App).appComponent.inject(this)
        vm = ViewModelProvider(this, vmFactory)[ChatViewModel::class.java]
        vm.listenChatWithId(chatId = args.chatId)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.imageBack.setOnClickListener { vm.setEvent(ChatContract.Event.OnBackButtonClicked) }
        binding.buttonSend.setOnClickListener { sendMessage() }
        initObservers()
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            vm.uiState.collectLatest {
                loadInitialData(uiState = it)
                when (it.recyclerViewState) {
                    is ChatContract.RecyclerViewState.Idle -> {
                        loading(false)
                    }
                    is ChatContract.RecyclerViewState.Loading -> {
                        loading(true)
                    }
                    is ChatContract.RecyclerViewState.Success -> {
                        loading(false)
                        adapter.messages = it.recyclerViewState.chatMessages
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            vm.effect.collect {
                when (it) {
                    is ChatContract.Effect.ShowToast -> {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    is ChatContract.Effect.ToBackFragment -> {
                        findNavController().popBackStack()
                    }
                }
            }
        }

    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.recyclerViewChat.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.recyclerViewChat.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun loadInitialData(uiState: ChatContract.State) {
        binding.textName.text = uiState.chatName
        uiState.sender?.let { user ->
            if (!this::adapter.isInitialized) {
                adapter = ChatAdapter(sender = user, messageListener = this)
                binding.recyclerViewChat.adapter = adapter
            }
        }
    }

    private fun sendMessage() {
        val message: String = binding.inputMessage.text.toString()
        vm.setEvent(ChatContract.Event.MessageSent(message = message))
        binding.inputMessage.text = null
    }

    override fun onChatMessageClicked(message: ChatMessage) {
        activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setItems(R.array.message_action
                ) { dialog, actionIndex ->
                    when (actionIndex) {
                        0 -> vm.setEvent(ChatContract.Event.OnMessageDeleteClicked(message = message))
                        1 -> vm.setEvent(ChatContract.Event.OnMessageChangeClicked(message = message))
                    }
                    dialog.cancel()
                }
            builder.create().show()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}