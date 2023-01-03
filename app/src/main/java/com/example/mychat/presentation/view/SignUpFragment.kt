package com.example.mychat.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mychat.R
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.presentation.viewmodels.SingUpModelFactory
import com.example.mychat.presentation.viewmodels.SingUpViewModel
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class SignUpFragment : Fragment() {
    private val db = Firebase.firestore
    private val storage = FireBaseStorageImpl(firestoreDb = db)
    private val repository = UserRepositoryImpl(firebaseStorage = storage)

    private val vmFactory: SingUpModelFactory = SingUpModelFactory(repository)

    private lateinit var vm: SingUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this, vmFactory)
            .get(SingUpViewModel::class.java)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    when (state) {
                        is ResultData.Success -> {
                            loading(false)
                            Toast.makeText(activity, state.value, Toast.LENGTH_SHORT).show()
                            // Switch page
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
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        // TODO: Find out how to do it better
        val imageField = view.findViewById<ShapeableImageView>(R.id.image_profile)
        val textAddImage = view.findViewById<TextView>(R.id.text_add_image)
        val imagePicker =
            registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
                if (result != null)
                    vm.setProfileImage(result, requireContext())
                imageField.setImageURI(result)
                textAddImage.visibility = View.INVISIBLE
                Log.d("Fragment", result.toString())
            }
        imageField.setOnClickListener {
            imagePicker.launch("image/*")
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        val signUpButton =
            requireView().findViewById<Button>(R.id.button_sign_up)
        signUpButton.setOnClickListener { userRegistration() }

    }

    private fun userRegistration() {
        val nameField = requireView().findViewById<EditText>(R.id.input_name)
        val emailField = requireView().findViewById<EditText>(R.id.input_email)
        val passwordField = requireView().findViewById<EditText>(R.id.input_password)
        val confirmPasswordField = requireView().findViewById<EditText>(R.id.input_confirm_password)

        val name = nameField.text.toString()
        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        val confirmPassword = confirmPasswordField.text.toString()

        vm.userRegistration(
            name = name,
            email = email,
            password = password,
            confirmPassword = confirmPassword
        )
    }


    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            requireView().findViewById<Button>(R.id.button_sign_up).visibility = View.INVISIBLE
            requireView().findViewById<ProgressBar>(R.id.progress_bar).visibility = View.VISIBLE
        } else {
            requireView().findViewById<Button>(R.id.button_sign_up).visibility = View.VISIBLE
            requireView().findViewById<ProgressBar>(R.id.progress_bar).visibility = View.INVISIBLE
        }
    }

}