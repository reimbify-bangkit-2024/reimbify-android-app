package com.example.reimbifyapp.user.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.reimbifyapp.databinding.DialogSuccessBinding

class SuccessDialogFragment : DialogFragment() {

    private var _binding: DialogSuccessBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_MESSAGE = "message"

        fun newInstance(message: String): SuccessDialogFragment {
            val fragment = SuccessDialogFragment()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = arguments?.getString(ARG_MESSAGE) ?: "Success!"
        binding.tvDialogMessage.text = message

        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}