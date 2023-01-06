package com.example.mychat.presentation.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mychat.R
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.databinding.FragmentSelectUserBinding
import com.example.mychat.databinding.ItemContainerUserBinding
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.adapters.UserAdapter
import com.example.mychat.presentation.listeners.UserListener
import com.example.mychat.presentation.viewmodels.SingUpModelFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SelectUserFragment : Fragment(), UserListener {

    private lateinit var binding: FragmentSelectUserBinding
    private lateinit var adapter: UserAdapter

    private lateinit var repository: UserRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        val storage = FireBaseStorageImpl(firestoreDb = db)
        val sharedPrefs = SharedPreferencesStorageImpl(appContext = requireActivity().applicationContext)
        repository = UserRepositoryImpl(firebaseStorage = storage, sharedPrefsStorage = sharedPrefs)
        adapter = UserAdapter(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeUserListResult().collectLatest { state ->
                    when (state) {
                        is ResultData.Success -> {
                            Toast.makeText(activity, "Successful list upload", Toast.LENGTH_SHORT)
                                .show()
                            adapter.users = state.value
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
        binding = FragmentSelectUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.imageBack.setOnClickListener {
            switchPage()
        }

        binding.recyclerViewUsers.adapter = adapter
        repository.uploadUserList()
    }
    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.textError.visibility = View.INVISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.textError.visibility = View.VISIBLE
        }
    }
    private fun switchPage(){
        requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onUserClicked(user: User) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this.id, ChatFragment(user))
            .addToBackStack(null)
            .commit()
    }
}