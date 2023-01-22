package com.example.mychat.presentation.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mychat.R
import com.example.mychat.databinding.FragmentSignUpBinding
import com.example.mychat.presentation.app.App
import com.example.mychat.presentation.viewmodels.SignUpViewModel
import com.example.mychat.presentation.viewmodels.ViewModelFactory
import com.example.mychat.presentation.viewmodels.сontracts.SignUpContract
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.flow.collectLatest
import java.io.FileDescriptor
import java.io.IOException
import javax.inject.Inject


class SignUpFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var vm: SignUpViewModel
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().applicationContext as App).appComponent.inject(this)
        vm = ViewModelProvider(this, vmFactory)
            .get(SignUpViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root

        // TODO: Find out how to do it better
        val imagePicker =
            registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
                val imageBitmap = uriToBitmap(selectedFileUri = result)
                vm.setEvent(SignUpContract.Event.OnImageProfileSelected(uri = result, image = imageBitmap))
            }
        binding.imageProfile.setOnClickListener {
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
                        profileImageUpdate(uri = it.profileImage)
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
                        findNavController().popBackStack()
                    }
                }
            }
        }

    }

    private fun profileImageUpdate(uri: Uri?) {
        binding.imageProfile.setImageURI(uri)
        if (uri == null)
            binding.textAddImage.visibility = View.VISIBLE
        else
            binding.textAddImage.visibility = View.INVISIBLE
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

    private fun uriToBitmap(selectedFileUri: Uri?): Bitmap? {
        if (selectedFileUri == null) {
            return null
        }
        try {
            val parcelFileDescriptor =
                requireContext().contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}