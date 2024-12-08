package com.example.pastfinder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pastfinder.api.ApiClient

class LoginViewModelFactory(private val apiClient: ApiClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(apiClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
