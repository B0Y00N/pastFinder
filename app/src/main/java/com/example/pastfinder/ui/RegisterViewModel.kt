package com.example.pastfinder.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pastfinder.api.ApiClient
import kotlinx.coroutines.launch

class RegisterViewModel(private val apiClient: ApiClient) : ViewModel() {
    private val _registrationStatus = mutableStateOf<RegistrationStatus>(RegistrationStatus.Idle)
    val registrationStatus: State<RegistrationStatus> get() = _registrationStatus


    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _registrationStatus.value = RegistrationStatus.Loading
                val jsonBody = """{"name": "$email", "password": "$password"}""".trimIndent()
                apiClient.post("/members/register", jsonBody) { success, response ->
                    if (success) {
                        _registrationStatus.value = RegistrationStatus.Success(response)
                    } else {
                        _registrationStatus.value = RegistrationStatus.Error("Error: $response")
                    }
                }
            } catch (e: Exception) {
                _registrationStatus.value = RegistrationStatus.Error("Error: ${e.message}")
            }
        }
    }
}

sealed class RegistrationStatus {
    object Idle : RegistrationStatus()
    object Loading : RegistrationStatus()
    data class Success(val message: String) : RegistrationStatus()
    data class Error(val message: String) : RegistrationStatus()
}