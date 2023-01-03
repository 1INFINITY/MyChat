package com.example.mychat.presentation.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mychat.R
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.presentation.viewmodels.SingInModelFactory
import com.example.mychat.presentation.viewmodels.SingInViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.w3c.dom.Text


class SingInFragment : Fragment() {

    private lateinit var vm: SingInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        val storage = FireBaseStorageImpl(firestoreDb = db)
        val sharedPrefs = SharedPreferencesStorageImpl(appContext = requireActivity().applicationContext)
        val repository = UserRepositoryImpl(firebaseStorage = storage, sharedPrefsStorage = sharedPrefs)

        val vmFactory: SingInModelFactory = SingInModelFactory(repository)
        vm = ViewModelProvider(this, vmFactory)
            .get(SingInViewModel::class.java)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    when (state) {
                        is ResultData.Success -> {
                            loading(false)
                            Toast.makeText(activity,
                                "Successful auth", Toast.LENGTH_SHORT).show()
                            switchPage(UserFragment())
                        }
                        is ResultData.Loading -> {
                            loading(true)
                        }
                        is ResultData.Failure -> {
                            loading(false)
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
        return inflater.inflate(R.layout.fragment_sing_in, container, false)
    }

    override fun onStart() {
        super.onStart()

        val textCreateNewAccount =
            requireView().findViewById<TextView>(R.id.text_create_new_account)
        textCreateNewAccount.setOnClickListener {
            switchPage(SignUpFragment())
        }

        val buttonSignIn = requireView().findViewById<Button>(R.id.button_sign_in)
        buttonSignIn.setOnClickListener {
                signIn()
        }
    }

    private fun signIn() {

        val emailField = requireView().findViewById<EditText>(R.id.input_email)
        val passwordField = requireView().findViewById<EditText>(R.id.input_password)

        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        vm.userAuthorization(
            email = email,
            password = password
        )
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            requireView().findViewById<Button>(R.id.button_sign_in).visibility = View.INVISIBLE
            requireView().findViewById<ProgressBar>(R.id.progress_bar).visibility = View.VISIBLE
        } else {
            requireView().findViewById<Button>(R.id.button_sign_in).visibility = View.VISIBLE
            requireView().findViewById<ProgressBar>(R.id.progress_bar).visibility = View.INVISIBLE
        }
    }

    private fun switchPage(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this.id, fragment)
            .addToBackStack(null)
            .commit()
    }
}