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
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.databinding.FragmentSelectUserBinding
import com.example.mychat.databinding.FragmentSignUpBinding
import com.example.mychat.domain.repository.ResultData
import com.example.mychat.presentation.viewmodels.SingUpModelFactory
import com.example.mychat.presentation.viewmodels.SingUpViewModel
import com.example.mychat.presentation.viewmodels.сontracts.SelectUserContract
import com.example.mychat.presentation.viewmodels.сontracts.SignUpContract
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SignUpFragment : Fragment() {

    private lateinit var vm: SingUpViewModel
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val db = Firebase.firestore
        val storage = FireBaseStorageImpl(firestoreDb = db)
        val sharedPrefs =
            SharedPreferencesStorageImpl(appContext = requireActivity().applicationContext)
        val repository =
            UserRepositoryImpl(firebaseStorage = storage, sharedPrefsStorage = sharedPrefs)

        val vmFactory: SingUpModelFactory = SingUpModelFactory(repository)

        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this, vmFactory)
            .get(SingUpViewModel::class.java)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root

        // TODO: Find out how to do it better
        val imageField = view.findViewById<ShapeableImageView>(R.id.image_profile)
        val textAddImage = view.findViewById<TextView>(R.id.text_add_image)
        val imagePicker =
            registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
                if (result != null)
                    vm.setProfileImage(result, requireContext())
                imageField.setImageURI(result)
                textAddImage.visibility = View.INVISIBLE
            }
        imageField.setOnClickListener {
            imagePicker.launch("image/*")
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        binding.buttonSignUp.setOnClickListener { userRegistration() }
        binding.textSignIn.setOnClickListener { vm.setEvent(SignUpContract.Event.OnSignInTextClicked) }
        initObservers()
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            vm.uiState.collectLatest {
                when (it.fragmentViewState) {
                    is SignUpContract.ViewState.Idle -> {
                        loading(false)
                    }
                    is SignUpContract.ViewState.Loading -> {
                        loading(true)
                    }
                    is SignUpContract.ViewState.Success -> {
                        loading(false)
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            vm.effect.collect {
                when (it) {
                    is SignUpContract.Effect.ShowToast -> {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    is SignUpContract.Effect.ToSignInFragment -> {
                        switchPage(fragment = SingInFragment())
                    }
                }
            }
        }

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

    private fun userRegistration() {
        val nameField = requireView().findViewById<EditText>(R.id.input_name)
        val emailField = requireView().findViewById<EditText>(R.id.input_email)
        val passwordField = requireView().findViewById<EditText>(R.id.input_password)
        val confirmPasswordField = requireView().findViewById<EditText>(R.id.input_confirm_password)

        val name = nameField.text.toString()
        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        val confirmPassword = confirmPasswordField.text.toString()

        vm.setEvent(SignUpContract.Event.OnSignUpButtonClicked(
            name = name,
            email = email,
            password = password,
            confirmPassword = confirmPassword
        ))
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