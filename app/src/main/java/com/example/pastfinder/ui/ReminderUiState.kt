package com.example.pastfinder.ui

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class Goal(
    @SerializedName("id")val id:Long = 0,
    @SerializedName("contents")val content: String = "",
    @SerializedName("start_datetime")val startDate: String = "",
    @SerializedName("end_datetime")val endDate: String = ""
)