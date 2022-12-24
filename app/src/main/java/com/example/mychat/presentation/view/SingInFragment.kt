package com.example.mychat.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mychat.R
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

        val textCreateNewAccount = requireView().findViewById<TextView>(R.id.text_create_new_account)
        textCreateNewAccount.setOnClickListener {
            val signUpFragment = SignUpFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(this.id, signUpFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}