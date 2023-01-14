package com.example.mychat.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mychat.R
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.databinding.FragmentSignInBinding
import com.example.mychat.presentation.viewmodels.SignInModelFactory
import com.example.mychat.presentation.viewmodels.SignInViewModel
import com.example.mychat.presentation.viewmodels.сontracts.SignInContract
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest


class SignInFragment : Fragment() {

    private lateinit var vm: SignInViewModel
    private lateinit var binding: FragmentSignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        val storage = FireBaseStorageImpl(firestoreDb = db)
        val sharedPrefs =
            SharedPreferencesStorageImpl(appContext = requireActivity().applicationContext)
        val repository =
            UserRepositoryImpl(firebaseStorage = storage, sharedPrefsStorage = sharedPrefs)

        val vmFactory = SignInModelFactory(repository)
        vm = ViewModelProvider(this, vmFactory)
            .get(SignInViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.textCreateNewAccount.setOnClickListener {
            vm.setEvent(SignInContract.Event.OnSignUpTextClicked)
        }
        binding.buttonSignIn.setOnClickListener {
            signIn()
        }
        initObservers()
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            vm.uiState.collectLatest {
                when (it.fragmentViewState) {
                    is SignInContract.ViewState.Idle -> {
                        loading(false)
                    }
                    is SignInContract.ViewState.Loading -> {
                        loading(true)
                    }
                    is SignInContract.ViewState.Success -> {
                        loading(false)
                    }
                    is SignInContract.ViewState.Error -> {
                        loading(false)
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            vm.effect.collect {
                when (it) {
                    is SignInContract.Effect.ShowToast -> {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    is SignInContract.Effect.ToUserFragment -> {
                        switchPage(fragment = UserFragment())
                    }
                    is SignInContract.Effect.ToSignUpFragment -> {
                        switchPage(fragment = SignUpFragment())
                    }
                }
            }
        }

    }

    private fun signIn() {

        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()

        vm.setEvent(SignInContract.Event.OnSignInButtonClicked(email = email, password = password))
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.buttonSignIn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun switchPage(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this.id, fragment)
            .addToBackStack(null)
            .commit()
    }
}