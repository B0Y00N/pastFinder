package com.example.pastfinder.ui

data class DiaryUiState(
    var date: String = "",
    var title: String = "",
    var totalReview: String = "",
    var placeEntries: List<PlaceEntry> = emptyList()
)

data class PlaceEntry(
    val id: Int,
    val images: List<String> = emptyList(),
    val placeDescription: String = "",
    val placeName: String = "",
    val latitude: Double = -1.0,
    val longitude: Double = -1.0
    // val simpleReview: String = ""
    // 한줄평
)