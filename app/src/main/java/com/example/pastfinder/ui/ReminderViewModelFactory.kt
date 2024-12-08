package com.example.pastfinder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pastfinder.api.ApiClient

class ReminderViewModelFactory(private val apiClient: ApiClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderViewModel(apiClient) as T
    }
}