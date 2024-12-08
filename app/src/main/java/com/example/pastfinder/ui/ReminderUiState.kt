package com.example.pastfinder.ui

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class Goal(
    @SerializedName("contents")val content: String = "",
    @SerializedName("start_datetime")val startDate: String = "",
    @SerializedName("end_datetime")val endDate: String = ""
)