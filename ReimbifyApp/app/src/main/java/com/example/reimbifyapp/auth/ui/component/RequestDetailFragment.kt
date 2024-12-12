package com.example.reimbifyapp.auth.ui.component

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
import com.bumptech.glide.Glide
import com.example.reimbifyapp.R
import com.example.reimbifyapp.auth.factory.RequestDetailViewModelFactory
import com.example.reimbifyapp.auth.viewmodel.RequestDetailViewModel
import com.example.reimbifyapp.data.entities.Reimbursement
import com.example.reimbifyapp.databinding.FragmentRequestDetailBinding
import com.example.reimbifyapp.utils.ErrorUtils.parseErrorMessage
import java.util.Locale
import kotlin.getValue

class RequestDetailFragment : Fragment() {
    private var _binding: FragmentRequestDetailBinding? = null
    private val binding get() = _binding!!

    private val requestDetailViewModel by viewModels<RequestDetailViewModel> {
        RequestDetailViewModelFactory.getInstance(requireContext())
    }

    private var requestId: Int = -1
    private var isFetchingRequests = false
    private var lastToastTime = 0L
    private val toastDelay = 5000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestDetailBinding.inflate(inflater, container, false)
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
                val request = response.receipts[0]

                when(request.status) {
                    "under_review" -> {
                        binding.statusUnderReview.visibility = View.VISIBLE
                        binding.statusApproved.visibility = View.GONE
                        binding.statusRejected.visibility = View.GONE

                        binding.tvDetailsLabel.visibility = View.GONE
                        binding.llDetailsSection.visibility = View.GONE

                        setRequestDetail(request)
                    }
                    "rejected" -> {
                        binding.statusUnderReview.visibility = View.GONE
                        binding.statusApproved.visibility = View.GONE
                        binding.statusRejected.visibility = View.VISIBLE

                        binding.tvDetailsLabel.visibility = View.VISIBLE
                        binding.llDetailsSection.visibility = View.VISIBLE

                        setRequestDetail(request)
                        setReviewSummary(request)
                    }
                    "approved" -> {
                        binding.statusUnderReview.visibility = View.GONE
                        binding.statusApproved.visibility = View.VISIBLE
                        binding.statusRejected.visibility = View.GONE

                        binding.tvDetailsLabel.visibility = View.VISIBLE
                        binding.llDetailsSection.visibility = View.VISIBLE

                        setRequestDetail(request)
                        setReviewSummary(request)
                    }
                }
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
            showToast("Using default date format")
            date
        }
    }

    private fun setReviewSummary(request: Reimbursement) {
        binding.etReviewer.setText(request.approval?.admin?.userName)
        binding.etReviewDate.setText(setDate(request.approval?.responseDate.toString()))
        binding.etResponseNotes.setText(request.approval?.responseDescription)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}