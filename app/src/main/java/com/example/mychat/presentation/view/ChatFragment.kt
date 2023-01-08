package com.example.mychat.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.databinding.FragmentChatBinding
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.adapters.ChatAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment(val userReceiver: User) : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var userSender: User
    private lateinit var adapter: ChatAdapter
    private lateinit var repository: UserRepository
    private lateinit var messages: MutableList<ChatMessage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        val storage = FireBaseStorageImpl(firestoreDb = db)
        val sharedPrefs =
            SharedPreferencesStorageImpl(appContext = requireActivity().applicationContext)
        messages = mutableListOf()
        repository = UserRepositoryImpl(firebaseStorage = storage, sharedPrefsStorage = sharedPrefs)

        userSender = repository.getCachedUser()
        adapter = ChatAdapter(userReceiver.image, userSender.id)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //repository.observeMessages()
                repository.listenMessages(sender = userSender, receiver = userReceiver).collect { state ->
                    when (state) {
                        is ResultData.Success -> {
                            state.value.forEach {
                               Log.d("ChatFrag", it.message)
                                messages.add(it)
                            }
                            messages.sortBy { it.date }
                            adapter.messages = messages
                            binding.recyclerViewChat.visibility = View.VISIBLE
                        }
                        is ResultData.Loading -> {
                        }
                        is ResultData.Failure -> {
                            Toast.makeText(activity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
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
        setListeners()
        loadReceiverData()

    }

    private fun loadReceiverData() {
        binding.textName.text = userReceiver.name
        binding.recyclerViewChat.adapter = adapter
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.buttonSend.setOnClickListener { sendMessage() }
    }

    private fun sendMessage() {
        val messageString: String = binding.inputMessage.text.toString()
        val message = ChatMessage(senderId = userSender.id,
            receiverId = userReceiver.id,
            message = messageString,
            date = Date())
        repository.sendMessage(message)
        binding.inputMessage.text = null
    }


}