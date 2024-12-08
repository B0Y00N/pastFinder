package com.example.pastfinder.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pastfinder.api.ApiClient
import kotlinx.coroutines.launch

class LoginViewModel(private val apiClient: ApiClient) : ViewModel() {

    private val _loginStatus = mutableStateOf<LoginStatus>(LoginStatus.Idle)
    val loginStatus: State<LoginStatus> = _loginStatus
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginStatus.value = LoginStatus.Loading
                val jsonBody = """{"name": "$email", "password": "$password"}"""
                apiClient.post("/members/login", jsonBody) { success, response ->
                    if (success) {
                        _loginStatus.value = LoginStatus.Success("Login Successful!")
                    } else {
                        _loginStatus.value = LoginStatus.Error("Error: $response")
                    }
                }
            } catch (e: Exception) {
                _loginStatus.value = LoginStatus.Error("Error: ${e.message}")
                _loginStatus.value = LoginStatus.Idle
            }
        }
    }
}

sealed class LoginStatus {
    object Idle : LoginStatus()
    object Loading : LoginStatus()
    data class Success(val message: String) : LoginStatus()
    data class Error(val message: String) : LoginStatus()
}