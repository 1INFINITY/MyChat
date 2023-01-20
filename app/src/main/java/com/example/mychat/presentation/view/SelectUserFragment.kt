package com.example.mychat.presentation.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
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
import com.example.mychat.presentation.app.App
import com.example.mychat.presentation.listeners.UserListener
import com.example.mychat.presentation.viewmodels.*
import com.example.mychat.presentation.viewmodels.сontracts.ChatContract
import com.example.mychat.presentation.viewmodels.сontracts.SelectUserContract
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class SelectUserFragment : Fragment(), UserListener {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var binding: FragmentSelectUserBinding
    private lateinit var adapter: UserAdapter
    private lateinit var vm: SelectUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().applicationContext as App).appComponent.inject(this)
        vm = ViewModelProvider(this, vmFactory)[SelectUserViewModel::class.java]
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

        adapter = UserAdapter(this)
        binding.recyclerViewUsers.adapter = adapter

        binding.imageBack.setOnClickListener {
            vm.setEvent(SelectUserContract.Event.OnBackButtonClicked)
        }

        initObservers()
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            vm.uiState.collectLatest {
                when (it.recyclerViewState) {
                    is SelectUserContract.RecyclerViewState.Idle -> {
                        loading(false)
                    }
                    is SelectUserContract.RecyclerViewState.Loading -> {
                        loading(true)
                    }
                    is SelectUserContract.RecyclerViewState.Success -> {
                        loading(false)
                        adapter.users = it.recyclerViewState.users
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            vm.effect.collect {
                when (it) {
                    is SelectUserContract.Effect.ShowToast -> {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    is SelectUserContract.Effect.ToBackFragment -> {
                        findNavController().popBackStack()
                    }
                    is SelectUserContract.Effect.ToChatFragment -> {
                        findNavController().navigate(R.id.action_selectUserFragment_to_chatFragment)
                    }
                }
            }
        }

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

    override fun onUserClicked(user: User) {
        vm.setEvent(SelectUserContract.Event.OnUserClicked(user = user))
    }
}