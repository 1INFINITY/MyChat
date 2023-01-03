package com.example.mychat.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mychat.R
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.domain.repository.UserRepository
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class UserFragment : Fragment() {

    lateinit var repository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Firebase.firestore
        val storage = FireBaseStorageImpl(firestoreDb = db)
        val sharedPrefs = SharedPreferencesStorageImpl(appContext = requireActivity().applicationContext)
        repository = UserRepositoryImpl(firebaseStorage = storage, sharedPrefsStorage = sharedPrefs)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // TODO: Update UI with ViewModel
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onStart() {
        super.onStart()

        val imageSingOut = requireView().findViewById<AppCompatImageView>(R.id.image_sign_out)
        val imageProfile = requireView().findViewById<AppCompatImageView>(R.id.image_profile)
        val textName = requireView().findViewById<TextView>(R.id.text_name)

        val user = repository.getCachedUser()

        textName.text = user.name
        imageProfile.setImageBitmap(user.image)

        imageSingOut.setOnClickListener {
            repository.signOut()
            switchPage(SingInFragment())
        }
    }
    private fun switchPage(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this.id, fragment)
            .addToBackStack(null)
            .commit()
    }
}