package com.example.mychat.presentation.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.mychat.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text


class SingInFragment : Fragment() {
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

        val signInButton =
            requireView().findViewById<Button>(R.id.button_sign_in)
        signInButton.setOnClickListener{ addToFirestore() }
    }

    private fun addToFirestore() {
        val db = Firebase.firestore
        // Create a new user with a first and last name
        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(requireContext(),
                    "DocumentSnapshot added with ID: ${documentReference.id}",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
    }
}