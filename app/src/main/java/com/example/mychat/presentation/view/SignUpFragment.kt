package com.example.mychat.presentation.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.mychat.R
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.domain.models.User
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.io.FileDescriptor
import java.io.IOException


class SignUpFragment : Fragment() {
    private val db = Firebase.firestore
    private val storage = FireBaseStorageImpl(firestoreDb = db)
    private val repository = UserRepositoryImpl(firebaseStorage = storage)

    private var profileImageBitmap: Bitmap? = null

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
                    profileImageBitmap = uriToBitmap(result)
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
        signUpButton.setOnClickListener { addToFirestore() }

    }

    private fun addToFirestore() {
        val imageField = requireView().findViewById<ShapeableImageView>(R.id.image_profile)
        val nameField = requireView().findViewById<EditText>(R.id.input_name)
        val emailField = requireView().findViewById<EditText>(R.id.input_email)
        val passwordField = requireView().findViewById<EditText>(R.id.input_password)
        if (!isValidSignUpDetails()) {
            return
        }
        val user = User(
            image = profileImageBitmap!!,
            name = nameField.text.toString(),
            email = emailField.text.toString(),
            password = passwordField.text.toString()
        )
        repository.userRegistration(user)
    }
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun isValidSignUpDetails(): Boolean {
        loading(true)
        var result = true
        var message = ""
        val nameField = requireView().findViewById<EditText>(R.id.input_name)
        val emailField = requireView().findViewById<EditText>(R.id.input_email)
        val passwordField = requireView().findViewById<EditText>(R.id.input_password)
        val confirmPasswordField = requireView().findViewById<EditText>(R.id.input_confirm_password)
        if (profileImageBitmap == null) {
            message = "Select a profile image"
            result = false
        } else if (nameField.text.toString().trim().isEmpty()) {
            message = "Enter name"
            result = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailField.text.toString()).matches()) {
            message = "Enter email"
            result = false
        } else if (passwordField.text.toString().trim().isEmpty()) {
            message = "Enter password"
            result = false
        } else if (confirmPasswordField.text.toString().trim().isEmpty()) {
            message = "Confirm your password"
            result = false
        } else if (confirmPasswordField.text.toString() != passwordField.text.toString()) {
            message = "Enter password"
            result = false
        } else if (passwordField.text.toString() != confirmPasswordField.text.toString()) {
            message = "Password and confirm must be same"
            result = false
        } else {
            message = "Successful sign up"
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        if (!result)
            loading(false)
        return result
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