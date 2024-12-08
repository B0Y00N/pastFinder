package com.example.pastfinder.ui

data class DiaryResponse (
    val title: String,
    val review: String,
    val map: List<Location>
)

data class Location(
    val locationKey: LocationKey,
    val name: String,
    val x: Double,
    val y: Double,
    val images: String,
    val placeDescription: String,
    val simpleReview: String
)

data class LocationKey(
    val seq: Int
)