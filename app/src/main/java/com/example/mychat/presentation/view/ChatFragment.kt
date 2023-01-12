package com.example.mychat.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.databinding.FragmentChatBinding
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.adapters.ChatAdapter
import com.example.mychat.presentation.viewmodels.ChatModelFactory
import com.example.mychat.presentation.viewmodels.ChatViewModel
import com.example.mychat.presentation.viewmodels.UserModelFactory
import com.example.mychat.presentation.viewmodels.UserViewModel
import com.example.mychat.presentation.viewmodels.сontracts.ChatContract
import com.example.mychat.presentation.viewmodels.сontracts.UserContract
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class ChatFragment(val chat: Chat) : Fragment() {

    private lateinit var vm: ChatViewModel
    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        val storage = FireBaseStorageImpl(firestoreDb = db)
        val sharedPrefs =
            SharedPreferencesStorageImpl(appContext = requireActivity().applicationContext)
        val repository =
            UserRepositoryImpl(firebaseStorage = storage, sharedPrefsStorage = sharedPrefs)

        val vmFactory = ChatModelFactory(chat = chat, repository = repository)
        vm = ViewModelProvider(this, vmFactory)
            .get(ChatViewModel::class.java)


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
                loadReceiverData(uiState = it)
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
                    is ChatContract.Effect.ChangeFragment -> {
                        switchPage(it.fragment)
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

    private fun loadReceiverData(uiState: ChatContract.State) {
        binding.textName.text = uiState.chatName
        uiState.sender?.let {
            adapter = ChatAdapter(uiState.receiverImage ?: it.image, it)
            binding.recyclerViewChat.adapter = adapter
        }
    }

    private fun sendMessage() {
        val message: String = binding.inputMessage.text.toString()
        vm.setEvent(ChatContract.Event.MessageSent(message = message))
        binding.inputMessage.text = null
    }

    private fun switchPage(fragment: Fragment?) {
        if (fragment == null) {
            requireActivity().supportFragmentManager.popBackStack()
            return
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this.id, fragment)
            .addToBackStack(null)
            .commit()
    }
}