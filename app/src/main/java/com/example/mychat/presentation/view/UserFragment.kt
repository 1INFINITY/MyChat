package com.example.mychat.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mychat.R
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.databinding.FragmentSelectUserBinding
import com.example.mychat.databinding.FragmentUserBinding
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.adapters.RecentChatsAdapter
import com.example.mychat.presentation.adapters.UserAdapter
import com.example.mychat.presentation.listeners.UserListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserFragment : Fragment(), UserListener {

    private lateinit var binding: FragmentUserBinding
    private lateinit var repository: UserRepository
    private lateinit var chats: List<Chat>
    private lateinit var adapter: RecentChatsAdapter
    private lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Firebase.firestore
        val storage = FireBaseStorageImpl(firestoreDb = db)
        val sharedPrefs = SharedPreferencesStorageImpl(appContext = requireActivity().applicationContext)
        repository = UserRepositoryImpl(firebaseStorage = storage, sharedPrefsStorage = sharedPrefs)
        user = repository.getCachedUser()
        adapter = RecentChatsAdapter(mainUser = user, this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.fetchChats(user).collect { state ->
                    when (state) {
                        is ResultData.Success -> {
                            Toast.makeText(activity, "Successful list upload", Toast.LENGTH_SHORT)
                                .show()
                            adapter.chats = state.value
                            loading(false)
                        }
                        is ResultData.Loading -> {
                            loading(true)
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
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.textName.text = user.name
        binding.imageProfile.setImageBitmap(user.image)
        binding.recyclerViewChats.adapter = adapter

        binding.floatingButtonNewChat.setOnClickListener {
            switchPage(SelectUserFragment())
        }
        binding.imageSignOut.setOnClickListener {
            repository.signOut()
            switchPage(SingInFragment())
        }
    }
    override fun onUserClicked(user: User) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this.id, ChatFragment(user))
            .addToBackStack(null)
            .commit()
    }
    private fun switchPage(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this.id, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.recyclerViewChats.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            binding.textError.visibility = View.INVISIBLE
        } else {
            binding.recyclerViewChats.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
            binding.textError.visibility = View.INVISIBLE
        }
    }
}