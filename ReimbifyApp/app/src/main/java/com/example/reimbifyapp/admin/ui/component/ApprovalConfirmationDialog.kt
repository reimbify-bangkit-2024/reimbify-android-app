package com.example.reimbifyapp.admin.ui.component

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.auth.ui.component.SuccessDialogFragment
import com.example.reimbifyapp.databinding.DialogApprovalConfirmationBinding
import com.example.reimbifyapp.R
import kotlinx.coroutines.launch

class ApprovalConfirmationDialog(
    private val warningMessage: String,
    private val onApproveConfirmed: suspend () -> Boolean,
    private val onRejectConfirmed: suspend () -> Boolean,
    private val isApprove: Boolean
) : DialogFragment() {

    private var _binding: DialogApprovalConfirmationBinding? = null
    private val binding get() = _binding!!
    private var lastToastTime = 0L
    private val toastDelay = 5000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogApprovalConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isApprove) {
            binding.tvDialogTitle.text = getString(R.string.are_you_sure_want_to_approve)
            binding.ivApprovalIcon.setImageResource(R.drawable.gui_approve_svgrepo_com)
            binding.btnApproval.text = getString(R.string.approve)
            binding.btnApproval.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_500))

        } else {
            binding.tvDialogTitle.text = getString(R.string.are_you_sure_want_to_reject)
            binding.ivApprovalIcon.setImageResource(R.drawable.gui_check_no_svgrepo_com)
            binding.btnApproval.text = getString(R.string.reject)
            binding.btnApproval.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.cancel_red))
        }

        binding.tvWarningMessage.text = Html.fromHtml(warningMessage, Html.FROM_HTML_MODE_LEGACY)
        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnApproval.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val success = if (isApprove) {
                        onApproveConfirmed.invoke()
                    } else {
                        onRejectConfirmed.invoke()
                    }

                    if (success) {
                        dismiss()
                    } else {
                        showToast("Failed to ${if (isApprove) "approve" else "reject"} the request.")
                    }
                } catch (e: Exception) {
                    Log.e("ApprovalDialog", "Error during approval/rejection", e)
                    showToast("Failed to ${if (isApprove) "approve" else "reject"} the request.")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > toastDelay) {
            lastToastTime = currentTime
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "ConfirmationApprovalDialog"

        fun newInstance(
            warningMessage: String,
            onApproveConfirmed: suspend () -> Boolean,
            onRejectConfirmed: suspend () -> Boolean,
            isApprove: Boolean
        ): ApprovalConfirmationDialog {
            return ApprovalConfirmationDialog(warningMessage, onApproveConfirmed, onRejectConfirmed, isApprove)
        }
    }
}
