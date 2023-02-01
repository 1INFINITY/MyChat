package com.example.mychat.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mychat.R
import com.example.mychat.databinding.FragmentUserBinding
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.usecase.GetCachedUserUseCase
import com.example.mychat.presentation.adapters.RecentChatsAdapter
import com.example.mychat.presentation.app.App
import com.example.mychat.presentation.listeners.ChatListener
import com.example.mychat.presentation.viewmodels.UserViewModel
import com.example.mychat.presentation.viewmodels.ViewModelFactory
import com.example.mychat.presentation.viewmodels.сontracts.UserContract.*
import javax.inject.Inject

class UserFragment : Fragment(), ChatListener {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    @Inject
    lateinit var getCachedUserUseCase: GetCachedUserUseCase

    private lateinit var vm: UserViewModel
    private lateinit var binding: FragmentUserBinding
    private lateinit var adapter: RecentChatsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as App).appComponent.inject(this)
        vm = ViewModelProvider(this, vmFactory)[UserViewModel::class.java]
        adapter = RecentChatsAdapter(mainUser = getCachedUserUseCase.execute(), this)
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

        binding.recyclerViewChats.adapter = adapter

        binding.floatingButtonNewChat.setOnClickListener {
            vm.setEvent(Event.OnFloatingButtonClicked)
        }
        binding.imageSignOut.setOnClickListener {
            vm.setEvent(Event.OnSignOutClicked)
        }

        initObservers()
    }

    override fun onChatClicked(chat: Chat) {
        vm.setEvent(Event.OnChatClicked(chat = chat))
    }

    /**
     * Initialize Observers
     */
    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            vm.uiState.collect {
                binding.textName.text = it.userName ?: "Default name"
                it.profileImage?.also { image -> binding.imageProfile.setImageBitmap(image) }
                when (it.recyclerViewState) {
                    is RecyclerViewState.Idle -> {
                        loading(false)
                    }
                    is RecyclerViewState.Loading -> {
                        loading(true)
                    }
                    is RecyclerViewState.Success -> {
                        loading(false)
                        adapter.submitList(it.recyclerViewState.chatsList)
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            vm.effect.collect {
                when (it) {
                    is Effect.ShowToast -> {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    is Effect.ToChatFragment -> {
                        val action = UserFragmentDirections.actionUserFragmentToChatFragment(it.chatId)
                        findNavController().navigate(action)
                    }
                    is Effect.ToBackFragment -> {
                        findNavController().popBackStack()
                    }
                    is Effect.ToSelectUserFragment -> {
                        findNavController().navigate(R.id.action_userFragment_to_selectUserFragment)
                    }
                }
            }
        }

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