package com.example.reimbifyapp.user.ui.add_user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.databinding.FragmentAddRequestUserBinding

class AddUserFragment : Fragment() {

    private var _binding: FragmentAddRequestUserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val addUserViewModel =
            ViewModelProvider(this).get(AddUserViewModel::class.java)

        _binding = FragmentAddRequestUserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textAddrequest
        addUserViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}