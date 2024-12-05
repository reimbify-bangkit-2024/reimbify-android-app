package com.example.reimbifyapp.admin.ui.component

import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.reimbifyapp.R
import com.example.reimbifyapp.auth.factory.RequestApprovalViewModelFactory
import com.example.reimbifyapp.auth.factory.RequestDetailViewModelFactory
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.ui.component.SuccessDialogFragment
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.auth.viewmodel.RequestApprovalViewModel
import com.example.reimbifyapp.auth.viewmodel.RequestDetailViewModel
import com.example.reimbifyapp.data.entities.Reimbursement
import com.example.reimbifyapp.databinding.FragmentApprovalBinding
import com.example.reimbifyapp.databinding.FragmentRequestDetailBinding
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.getValue

class ApprovalFragment : Fragment() {
    private var _binding: FragmentApprovalBinding? = null
    private val binding get() = _binding!!

    private val requestDetailViewModel by viewModels<RequestDetailViewModel> {
        RequestDetailViewModelFactory.getInstance(requireContext())
    }

    private val requestApprovalViewModel by viewModels<RequestApprovalViewModel> {
        RequestApprovalViewModelFactory.getInstance(requireContext())
    }

    private val authViewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(requireContext())
    }

    private var requestId: Int = -1
    private var isFetchingRequests = false
    private var lastToastTime = 0L
    private val toastDelay = 5000L

    private lateinit var request: Reimbursement

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovalBinding.inflate(inflater, container, false)
        requestId = arguments?.getInt("requestId") ?: -1
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requestId == -1) {
            showToast("Invalid Request ID")
            return
        }

        fetchRequest()
        setupObserver()
        setupAction()
    }

    private fun fetchRequest() {
        isFetchingRequests = true
        updateLoadingState()
        requestDetailViewModel.getRequestDetail(requestId)
    }

    private fun updateLoadingState() {
        showLoading(isFetchingRequests)
    }

    private fun setupObserver() {
        requestDetailViewModel.requestDetail.observe(viewLifecycleOwner) { result ->
            isFetchingRequests = false
            updateLoadingState()

            result.onSuccess { response ->
                request = response.receipts[0]
                setRequestDetail(request)
            }
            result.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
            }
        }

        requestApprovalViewModel.requestApproval.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                showSuccessDialog(if (response.status == "approved" || response.status == "rejected")
                    response.status.uppercase()
                else "Processed")
                navigateToReviewed()
            }
            result.onFailure { throwable ->
                val errorMessage = parseErrorMessage(throwable)
                showToast(errorMessage)
            }
        }
    }

    private fun setRequestDetail(request: Reimbursement) {
        binding.etRequestDate.setText(setDate(request.requestDate.toString()))
        binding.etName.setText(request.requester.userName)
        binding.etEmail.setText(request.requester.email)
        binding.etDepartment.setText(request.department.departmentName)
        binding.etAmount.setText(setCurrency(request.amount))
        binding.etReceiptDate.setText(setDate(request.receiptDate.toString()))
        binding.etDescription.setText(request.description)

        val imageUrl = request.receiptImageUrl
        Glide.with(binding.ivNotaImage.context)
            .load(imageUrl)
            .placeholder(R.drawable.image_svgrepo_com)
            .error(R.drawable.file_corrupted_svgrepo_com)
            .into(binding.ivNotaImage)
    }

    private fun setCurrency(amount: Double): String {
        val locale = Locale.getDefault()
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        return currencyFormat.format(amount)
    }

    private fun setDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("GMT")

            val outputFormat = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate ?: date)
        } catch (e: Exception) {
            val errorMessage = parseErrorMessage(e)
            Log.d("PARSE DATE", errorMessage)
            showToast("Failed to parse Date $errorMessage")
            date
        }
    }

    private fun setupAction() {
        binding.btnApprove.setOnClickListener {
            val warningMessage = """
        <b>Amount:</b> ${setCurrency(request.amount)}<br>
        <b>Description:</b> ${request.description}<br>
    """.trimIndent()

            ApprovalConfirmationDialog.newInstance(
                warningMessage = warningMessage,
                onApproveConfirmed = {
                    try {
                        onApproveRequest(requestId)
                        true
                    } catch (e: Exception) {
                        Log.e("Approval", "Failed to approve", e)
                        false
                    }
                },
                onRejectConfirmed = {
                    try {
                        onRejectRequest(requestId)
                        true
                    } catch (e: Exception) {
                        Log.e("Approval", "Failed to reject", e)
                        false
                    }
                },
                isApprove = true
            ).show(parentFragmentManager, ApprovalConfirmationDialog.TAG)
        }

        binding.btnReject.setOnClickListener {
            if (binding.etApprovalNotes.text.isNullOrEmpty()) {
                showToast("Approval notes cannot be empty.")
                return@setOnClickListener
            }

            val warningMessage = """
                <b>Amount:</b> ${setCurrency(request.amount)}<br>
                <b>Description:</b> ${request.description}<br>
            """.trimIndent()

            ApprovalConfirmationDialog.newInstance(
                warningMessage = warningMessage,
                onApproveConfirmed = {
                    try {
                        onApproveRequest(requestId)
                        true
                    } catch (e: Exception) {
                        Log.e("Approval", "Failed to approve", e)
                        false
                    }
                },
                onRejectConfirmed = {
                    try {
                        onRejectRequest(requestId)
                        true
                    } catch (e: Exception) {
                        Log.e("Approval", "Failed to reject", e)
                        false
                    }
                },
                isApprove = false
            ).show(parentFragmentManager, ApprovalConfirmationDialog.TAG)
        }
    }

    private fun onApproveRequest(requestId: Int) {
        lifecycleScope.launch {
            val adminId = authViewModel.getSession().first().userId.toInt()
            requestApprovalViewModel.requestApproval(
                requestId,
                "approved",
                adminId,
                binding.etApprovalNotes.text.toString()
            )
        }
    }

    private fun onRejectRequest(requestId: Int) {
        lifecycleScope.launch {
            val adminId = authViewModel.getSession().first().userId.toInt()
            requestApprovalViewModel.requestApproval(
                requestId,
                "rejected",
                adminId,
                binding.etApprovalNotes.text.toString()
            )
        }
    }

    private fun navigateToReviewed() {
        findNavController().popBackStack(R.id.navigation_to_review, false)
        findNavController().navigate(R.id.navigation_reviewed)
    }

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > toastDelay) {
            lastToastTime = currentTime
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _binding?.let { binding ->
            if (isLoading) {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = true
            } else {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.isIndeterminate = false
            }
        }
    }

    private fun showSuccessDialog(instance: String) {
        val successDialog = SuccessDialogFragment.Companion.newInstance(
            "Request $instance",
            "Success! Request has been $instance successfully!"
        )
        successDialog.show(parentFragmentManager, "SuccessDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}