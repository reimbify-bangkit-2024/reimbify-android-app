package com.example.reimbifyapp.user.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reimbifyapp.R
import com.example.reimbifyapp.data.network.api.ApiConfig
import com.example.reimbifyapp.data.network.request.RequestData
import com.example.reimbifyapp.data.network.response.GetBankAccountByUserIdResponse
import com.example.reimbifyapp.data.network.response.GetDepartmentResponse
import com.example.reimbifyapp.data.network.response.SubmitRequestResponse
import com.example.reimbifyapp.data.network.response.UploadResponse
import com.example.reimbifyapp.data.preferences.UserPreference
import com.example.reimbifyapp.data.repositories.DepartmentRepository
import com.example.reimbifyapp.data.repositories.ImageRepository
import com.example.reimbifyapp.data.repositories.ProfileRepository
import com.example.reimbifyapp.data.repositories.RequestRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class AddRequestViewModel(
    private val userPreference: UserPreference,
    private val profileRepository: ProfileRepository,
    private val imageRepository: ImageRepository,
    private val departmentRepository: DepartmentRepository,
    private val requestRepository: RequestRepository
) : ViewModel() {

    val statusIconColor = MutableLiveData<Int>().apply { value = R.color.red_500 }

    private val _uploadResponse = MutableLiveData<UploadResponse?>()
    val uploadResponse: LiveData<UploadResponse?> = _uploadResponse

    private val _departmentResponse = MutableLiveData<Result<GetDepartmentResponse>>()
    val departmentResponse: LiveData<Result<GetDepartmentResponse>> = _departmentResponse

    private val _bankAccountResponse = MutableLiveData<Result<GetBankAccountByUserIdResponse>>()
    val bankAccountResponse: LiveData<Result<GetBankAccountByUserIdResponse>> = _bankAccountResponse

    private val _submitRequestResponse = MutableLiveData<Result<SubmitRequestResponse>>()
    val submitRequestResponse: LiveData<Result<SubmitRequestResponse>> = _submitRequestResponse

    fun submitRequest(requestData: RequestData) {
        viewModelScope.launch {
            try {
                val response: Response<SubmitRequestResponse> = requestRepository.submitRequest(requestData)
                if (response.isSuccessful) {
                    _submitRequestResponse.value = Result.success(response.body() ?: SubmitRequestResponse("No message"))
                    Log.d("AddRequestViewModel", "Request submitted successfully: ${response.body()}")
                } else {
                    _submitRequestResponse.value = Result.failure(Exception("Failed to submit request: ${response.message()}"))
                    Log.e("AddRequestViewModel", "Request submission failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _submitRequestResponse.value = Result.failure(e)
                Log.e("AddRequestViewModel", "Request submission error: ${e.message}", e)
            }
        }
    }

    fun uploadImage(imageUri: Uri, userId: String) {
        viewModelScope.launch {
            try {
                val apiService = ApiConfig.createAuthenticatedApiService(userPreference)
                val response = imageRepository.uploadImage(apiService, imageUri, userId)
                _uploadResponse.value = response
            } catch (e: Exception) {
                Log.e("AddRequestViewModel", "Upload image failed: ${e.message}", e)
                _uploadResponse.value = null
            }
        }
    }

    fun getAllDepartments() {
        viewModelScope.launch {
            try {
                val response = departmentRepository.getAllDepartment()
                _departmentResponse.postValue(Result.success(response))
            } catch (e: Exception) {
                _departmentResponse.postValue(Result.failure(e))
            }
        }
    }

    fun getAllBankAccounts(userId: Int) {
        viewModelScope.launch {
            try {
                val response = profileRepository.getBankAccountByUserId(userId)
                _bankAccountResponse.postValue(Result.success(response))
            } catch (e: Exception) {
                _bankAccountResponse.postValue(Result.failure(e))
            }
        }
    }
}
