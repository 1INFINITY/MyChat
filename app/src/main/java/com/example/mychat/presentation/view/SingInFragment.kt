package com.example.mychat.presentation.view

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.mychat.R
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.w3c.dom.Text


class SingInFragment : Fragment() {
    val db = Firebase.firestore
    val storage = FireBaseStorageImpl(firestoreDb = db)
    val repository = UserRepositoryImpl(firebaseStorage = storage)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sing_in, container, false)
    }

    override fun onStart() {
        super.onStart()

        val textCreateNewAccount =
            requireView().findViewById<TextView>(R.id.text_create_new_account)
        textCreateNewAccount.setOnClickListener {
            val signUpFragment = SignUpFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(this.id, signUpFragment)
                .addToBackStack(null)
                .commit()
        }
        val buttonSignIn = requireView().findViewById<Button>(R.id.button_sign_in)
        buttonSignIn.setOnClickListener {
            if (isValidSignUpDetails()) {
                signIn()
            }
        }
        lifecycleScope.launch {
            repository.observeAuthResult().collectLatest { result ->

                when(result) {
                    is ResultData.Success -> {
                        loading(false)
                        Toast.makeText(activity, "Success auth", Toast.LENGTH_SHORT).show()
                        // Switch page
                    }
                    is ResultData.Loading -> {
                        loading(true)
                    }
                    is ResultData.Failure -> {
                        loading(false)
                        Toast.makeText(activity, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun signIn() {

        val emailField = requireView().findViewById<EditText>(R.id.input_email)
        val passwordField = requireView().findViewById<EditText>(R.id.input_password)

        if (isValidSignUpDetails()) {
            val authData = AuthData(
                email = emailField.text.toString(),
                password = passwordField.text.toString())
            repository.dopelUserAuthorization(authData)
        }
    }

    private fun isValidSignUpDetails(): Boolean {
        var result = true
        var message = ""
        val emailField = requireView().findViewById<EditText>(R.id.input_email)
        val passwordField = requireView().findViewById<EditText>(R.id.input_password)

        if (!Patterns.EMAIL_ADDRESS.matcher(emailField.text.toString()).matches()) {
            message = "Enter email"
            result = false
        } else if (passwordField.text.toString().trim().isEmpty()) {
            message = "Enter password"
            result = false
        }
        if (!result)
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        if (!result)
            loading(false)
        return result
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
}