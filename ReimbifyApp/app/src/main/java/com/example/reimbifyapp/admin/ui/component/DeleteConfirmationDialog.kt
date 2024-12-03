package com.example.reimbifyapp.admin.ui.component

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.reimbifyapp.auth.ui.component.SuccessDialogFragment
import com.example.reimbifyapp.databinding.DialogDeleteConfirmationBinding

class DeleteConfirmationDialog(
    private val warningMessage: String,
    private val onDeleteConfirmed: () -> Unit,
    private val instance: String
) : DialogFragment() {

    private var _binding: DialogDeleteConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDeleteConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvWarningMessage.text = Html.fromHtml(warningMessage, Html.FROM_HTML_MODE_LEGACY)
        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            onDeleteConfirmed.invoke()
            showSuccessDialog(instance)
            dismiss()
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

    private fun showSuccessDialog(instance: String) {
        val successDialog = SuccessDialogFragment.Companion.newInstance(
            "$instance Deleted",
            "$instance has been deleted successfully!"
        )
        successDialog.show(parentFragmentManager, "SuccessDialog")
    }

    companion object {
        const val TAG = "ConfirmationDeleteDialog"

        fun newInstance(
            warningMessage: String,
            onDeleteConfirmed: () -> Unit,
            instance: String
        ): DeleteConfirmationDialog {
            return DeleteConfirmationDialog(warningMessage, onDeleteConfirmed, instance)
        }
    }
}
