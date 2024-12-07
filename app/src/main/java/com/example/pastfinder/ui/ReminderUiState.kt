package com.example.pastfinder.ui

import kotlinx.serialization.Serializable

data class Goal(
    val content: String = "",
    val startDate: String = "",
    val endDate: String = ""
)