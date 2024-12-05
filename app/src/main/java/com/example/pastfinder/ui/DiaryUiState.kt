package com.example.pastfinder.ui

import com.google.android.libraries.places.api.model.Place

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
    val longitude: Double = -1.0,
    val simpleReview: String = ""
    // 한줄평
)

// 지도 검색 기록을 저장하는 엔트리
data class PlaceFinderEntry(
    val query: String = "",
    val places: List<Place> = emptyList(),
    val loading: Boolean = false
)