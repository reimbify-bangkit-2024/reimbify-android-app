package com.example.reimbifyapp.user.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.FragmentAddRequestUserBinding
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.ui.component.SuccessDialogFragment
import com.example.reimbifyapp.user.viewmodel.AddRequestViewModel
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.entities.Account
import com.example.reimbifyapp.data.entities.Department
import com.example.reimbifyapp.data.network.request.RequestData
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class AddRequestFragment : Fragment() {

    private var _binding: FragmentAddRequestUserBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private lateinit var viewModel: AddRequestViewModel
    private lateinit var loginViewModel: LoginViewModel
    private var departmentId: Int? = null
    private var accountId: Int? = null
    private var selectedDate: String? = null
    private var description: String?= null
    private var amount: Int? = null
    private var userId: Int? = null
    private var goodImage = false

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_GALLERY = 200
        private const val TAG = "AddRequestFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRequestUserBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = ViewModelProvider(this, UserViewModelFactory.getInstance(requireContext()))[AddRequestViewModel::class.java]
        loginViewModel = ViewModelProvider(this, UserViewModelFactory.getInstance(requireContext()))[LoginViewModel::class.java]

        setupUI()

        viewModel.getAllDepartments()

        viewModel.departmentResponse.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { setDepartmentSpinner(it.departments) }
        }

        setupBankAccountSpinner()

        viewModel.predictionResponse.observe(viewLifecycleOwner) { predictionResponse ->
            if (predictionResponse == null) {
                showLoading(false)
                Toast.makeText(context, "Failed to detect your receipt. Please try again.", Toast.LENGTH_SHORT).show()
                goodImage = false
                return@observe
            }
            
            predictionResponse?.let { prediction ->
                showLoading(false)
                Log.d(TAG, "Prediction Response: $prediction")
                val isCropValid = !prediction.crop.cropped
                val isRotateValid = !prediction.rotate.rotated

                if (isCropValid && isRotateValid) {
                    viewModel.statusIconColor.value = R.color.green_500
                    binding.ivBlurStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green_500))
                    binding.ivRotationStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green_500))
                    binding.ivCropStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green_500))
                    Toast.makeText(context, "Success detect your receipt", Toast.LENGTH_SHORT).show()

                    goodImage = true
                } else {
                    val errorMessage = buildErrorMessage(isCropValid, isRotateValid)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    updateStatusIcons(isCropValid, isRotateValid)
                    goodImage = false
                }
            }
        }

        viewModel.uploadResponse.observe(viewLifecycleOwner) { response ->
            showLoading(false)
            if (response != null) {
                Log.d(TAG, "Upload success: Status - ${response.message}, Message - ${response.message}, File URL - ${response.receiptImageUrl}")
                val imageUrl = response.receiptImageUrl
                val userId = userId
                Log.d("AddRequestFragment", "UserId: $userId, DepartmentId: $departmentId, AccountId: $accountId, SelectedDate: $selectedDate, Description: $description, Amount: $amount")
                val requestData = imageUrl?.let {
                    userId?.let { it1 ->
                        RequestData(
                            requesterId = it1,
                            departmentId = departmentId ?: 0,
                            accountId = accountId ?: 0,
                            receiptDate = selectedDate ?: "",
                            description = description ?: "",
                            amount = amount?.toDouble() ?: 0.0,
                            receiptImageUrl = it
                        )
                    }
                }
                Log.d("RequestData", "Request Data: $requestData")
                if (requestData != null) {
                    viewModel.submitRequest(requestData)
                    showSuccessDialog(true)
                }
            } else {
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                showSuccessDialog(false)
            }

            goodImage = false
        }

        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }

        return root
    }

    private fun buildErrorMessage(isCropValid: Boolean, isRotateValid: Boolean): String {
        val errorParts = mutableListOf<String>()
        if (!isCropValid) errorParts.add("Image is not cropped correctly")
        if (!isRotateValid) errorParts.add("Image is rotated incorrectly")
        return "Please adjust the image: ${errorParts.joinToString(", ")}"
    }

    private fun updateStatusIcons(isCropValid: Boolean, isRotateValid: Boolean) {
        binding.ivCropStatusIcon.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                if (isCropValid) R.color.green_500 else R.color.red_500
            )
        )

        binding.ivRotationStatusIcon.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                if (isRotateValid) R.color.green_500 else R.color.red_500
            )
        )
    }

    private fun resetForm() {
        binding.etDescription.text.clear()
        binding.etAmount.text.clear()
        binding.etDate.text.clear()

        selectedImageUri = null
        binding.ivReceipt.setImageResource(0)
        binding.ivReceipt.visibility = View.GONE
        binding.ivPlaceholderIcon.visibility = View.VISIBLE

        resetSpinner()
        resetIcons()

        goodImage = false
        selectedDate = null
        accountId = null
        departmentId = null
        description = null
        amount = null
    }


    private fun resetIcons() {
        viewModel.statusIconColor.value = R.color.red_500
        binding.ivBlurStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red_500))
        binding.ivRotationStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red_500))
        binding.ivCropStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red_500))
    }


    private fun resetSpinner() {
        binding.spDepartment.setSelection(0)
        binding.spBankAccount.setSelection(0)
    }

    private fun showSuccessDialog(isSuccess: Boolean) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        showLoading(false)
        if (isSuccess) {
            val dialogViewSuccess = layoutInflater.inflate(R.layout.dialog_success, null)
            val ivSuccessStatusIcon: ImageView = dialogViewSuccess.findViewById(R.id.ivSuccessIcon)
            val tvDialogSuccessTitle: TextView = dialogViewSuccess.findViewById(R.id.tvDialogTitle)
            val tvDialogSuccessMessage: TextView = dialogViewSuccess.findViewById(R.id.tvDialogMessage)
            val btnCloseSuccessDialog: ImageView = dialogViewSuccess.findViewById(R.id.btnCloseDialog)

            ivSuccessStatusIcon.setImageResource(R.drawable.correct_success_tick_svgrepo_com)
            tvDialogSuccessTitle.text = getString(R.string.request_sent)
            tvDialogSuccessMessage.text = getString(R.string.success_message)

            val dialog = dialogBuilder
                .setView(dialogViewSuccess)
                .create()

            btnCloseSuccessDialog.setOnClickListener {
                dialog.dismiss()
            }

            dialog.setOnDismissListener {
                resetForm()
            }

            dialog.show()
        } else {
            val dialogFailedView = layoutInflater.inflate(R.layout.dialog_failed, null)
            val ivFailedStatusIcon: ImageView = dialogFailedView.findViewById(R.id.ivFailedIcon)
            val tvDialogFailedTitle: TextView = dialogFailedView.findViewById(R.id.tvDialogTitle)
            val tvDialogFailedMessage: TextView = dialogFailedView.findViewById(R.id.tvDialogMessage)
            val btnCloseDialogFailed: ImageView = dialogFailedView.findViewById(R.id.btnCloseDialog)

            ivFailedStatusIcon.setImageResource(R.drawable.cancel_delete_remove_svgrepo_com)
            tvDialogFailedTitle.text = getString(R.string.failed_upload)
            tvDialogFailedMessage.text = getString(R.string.error_message_upload)

            val dialog = dialogBuilder
                .setView(dialogFailedView)
                .create()

            btnCloseDialogFailed.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun setupBankAccountSpinner(){
        lifecycleScope.launch {
            try {
                loginViewModel.getSession().collect { user ->
                    if (user.isLogin) {
                        userId = user.userId.toInt()
                        viewModel.getAllBankAccounts(userId!!)
                    } else {
                        Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error retrieving session: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.bankAccountResponse.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { response ->
                val accounts = response.accounts.ifEmpty { null }
                Log.d("BankAccountResponse", "Received bank accounts: ${response.accounts}")
                if (!accounts.isNullOrEmpty()) {
                    setBankAccountSpinner(accounts)
                } else {
                    binding.spBankAccount.adapter = null
                }
            }
        }
    }

    private fun setBankAccountSpinner(accounts: List<Account>) {
        val accountNames = listOf("Select Bank Account") + accounts.map { it.accountTitle }
        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            accountNames
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                return view
            }
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spBankAccount.adapter = spinnerAdapter

        binding.spBankAccount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (parent.getChildAt(0) as? TextView)?.setTextColor(Color.BLACK)
                accountId = if (position == 0) null else accounts[position - 1].accountId
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay" + "T$hour:$minute:$second"
                binding.etDate.setText(selectedDate)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = selectedDate?.let { dateFormat.parse(it) }
                val timestamp = date?.time
                Log.d("Selected Timestamp", "Timestamp: $timestamp")
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setDepartmentSpinner(departments: List<Department>) {
        if (departments.isEmpty()) {
            binding.spDepartment.adapter = null
        } else {
            val departmentNames = listOf("Select Department") + departments.map { it.departmentName }
            val spinnerAdapter = object : ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                departmentNames
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as TextView
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent) as TextView
                    return view
                }
            }
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spDepartment.adapter = spinnerAdapter

            binding.spDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    (parent.getChildAt(0) as? TextView)?.setTextColor(Color.BLACK)
                    departmentId = if (position == 0) null else departments[position - 1].departmentId
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun setupUI() {
        binding.btnSubmitRequest.setOnClickListener {
            lifecycleScope.launch {
                try {
                    loginViewModel.getSession().collect { user ->
                        if (user.isLogin) {
                            selectedImageUri?.let { uri ->
                                showLoading(true)
                                description = binding.etDescription.text.toString()
                                amount = binding.etAmount.text.toString().toIntOrNull()

                                when {
                                    description.isNullOrEmpty() -> {
                                        showLoading(false)
                                        Toast.makeText(context, "Description cannot be empty.", Toast.LENGTH_SHORT).show()
                                        return@let
                                    }
                                    amount == null -> {
                                        showLoading(false)
                                        Toast.makeText(context, "Amount must be a valid number.", Toast.LENGTH_SHORT).show()
                                        return@let
                                    }
                                    selectedDate.isNullOrEmpty() -> {
                                        showLoading(false)
                                        Toast.makeText(context, "Please select a date.", Toast.LENGTH_SHORT).show()
                                        return@let
                                    }
                                    accountId == null -> {
                                        showLoading(false)
                                        Toast.makeText(context, "Please select a bank account.", Toast.LENGTH_SHORT).show()
                                        return@let
                                    }
                                    !goodImage -> {
                                        showLoading(false)
                                        Toast.makeText(
                                            context,
                                            "The image does not meet the required criteria. Please ensure the image is not blurry, cropped incorrectly, or rotated.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@let
                                    }
                                    else -> {
                                        viewModel.uploadImage(uri, user.userId)
                                    }
                                }
                            } ?: run {
                                showLoading(false)
                                Toast.makeText(context, "Please select an image first.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showLoading(false)
                    Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnOpenCamera.setOnClickListener { openCamera() }
        binding.btnUploadGallery.setOnClickListener { openGallery() }
        binding.btnSubmitModel.setOnClickListener {
            lifecycleScope.launch {
                try {
                    selectedImageUri?.let { uri ->
                        showLoading(true)
                        viewModel.predictImage(uri)
                    } ?: run {
                        showLoading(false)
                        Toast.makeText(context, "Please select an image first.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    showLoading(false)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    private fun showLoading(isLoading: Boolean) {
        Log.d(TAG, "showLoading called with isLoading = $isLoading")
        _binding?.let { binding ->
            if (isLoading) {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = true
                binding.btnSubmitRequest.isEnabled = false
                binding.btnCancel.isEnabled = false
            } else {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.isIndeterminate = false
                binding.btnSubmitRequest.isEnabled = true
                binding.btnCancel.isEnabled = true
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    if (photo != null) {
                        selectedImageUri = getImageUri(photo)
                        displayImage(uri = selectedImageUri)
                    } else {
                        Toast.makeText(context, "Failed to capture image.", Toast.LENGTH_SHORT).show()
                    }
                }
                REQUEST_GALLERY -> {
                    val uri = data?.data
                    if (uri != null) {
                        selectedImageUri = uri
                        displayImage(uri = uri)
                    } else {
                        Toast.makeText(context, "Failed to select image.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Failed to get image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return FileProvider.getUriForFile(
            requireContext(),
            "com.example.reimbifyapp.fileprovider",
            file
        )
    }

    private fun displayImage(uri: Uri?) {
        if (uri != null) {
            binding.ivReceipt.setImageURI(uri)
            binding.ivReceipt.visibility = View.VISIBLE
            binding.ivPlaceholderIcon.visibility = View.GONE
        } else {
            Toast.makeText(context, "Failed to load image.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        resetIcons()
        resetSpinner()
        binding.etDescription.text.clear()
        binding.etAmount.text.clear()
        binding.etDate.text.clear()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
