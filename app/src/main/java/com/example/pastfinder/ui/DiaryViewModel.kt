package com.example.pastfinder.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pastfinder.api.ApiClient
import com.google.android.libraries.places.api.model.kotlin.place
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

class DiaryViewModel(private val apiClient: ApiClient) : ViewModel() {

    private var diaryList by mutableStateOf<List<String>>(emptyList())
    private var diarySet by mutableStateOf<Set<DiaryUiState>>(emptySet())
    // 선택된 날짜의 Diary state
    private var _uiState = MutableStateFlow(DiaryUiState())
    var uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    var placeFinderState by mutableStateOf(PlaceFinderEntry())
        private set

    fun fetchDiaries() {
        apiClient.getElements("/diary/list") { success, response ->
            if (success) {
                val gson = Gson()
                val diaryResponseList: List<String> =
                    gson.fromJson(response, Array<String>::class.java).toList()
                diaryList = diaryResponseList
            } else {

            }
        }
    }

    // 날짜를 인자로 받아 Diary state를 업데이트하는 함수
    fun createDiary(date: String) {
        _uiState.update { currentState ->
            currentState.copy(
                date = date,
                title = "",
                totalReview = "",
                placeEntries = emptyList()
            )
        }
    }

    // title을 업데이트하는 함수
    fun updateTitle(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = newTitle
            )
        }
    }

    // totalReview를 업데이트하는 함수
    fun updateTotalReview(newReview: String) {
        _uiState.update { currentState ->
            currentState.copy(
                totalReview = newReview
            )
        }
    }

    // placeEntry 추가하는 함수
    fun addPlaceEntry(placeEntry: PlaceEntry) {
        _uiState.update { currentState ->
            currentState.copy(
                placeEntries = currentState.placeEntries + placeEntry
            )
        }
    }

    // 장소 검색 페이지에서 호출하는 함수
    fun updatePlaceInfo(id: Int, place: String, latitude: Double, longitude: Double) {
        _uiState.update { currentState ->
            val updatedPlaceEntries = currentState.placeEntries.map { placeEntry ->
                if (placeEntry.id == id) {
                    placeEntry.copy(
                        placeName = place,
                        latitude = latitude,
                        longitude = longitude
                    )
                } else {
                    placeEntry
                }
            }
            currentState.copy(
                placeEntries = updatedPlaceEntries
            )
        }
    }

    // placeEntry에 이미지 추가하는 함수
    fun addImages(entryId: Int, images: List<String>) {
        _uiState.update { currentState ->
            val updatedPlaceEntries = currentState.placeEntries.map { placeEntry ->
                if (placeEntry.id == entryId) {
                    placeEntry.copy(images = placeEntry.images + images)
                } else {
                    placeEntry
                }
            }
            currentState.copy(
                placeEntries = updatedPlaceEntries
            )
        }
    }

    // 장소 설명을 업데이트하는 함수
    fun updatePlaceDescription(entryId: Int, description: String) {
        _uiState.update { currentState ->
            currentState.copy(
                placeEntries = currentState.placeEntries.map {
                    if (it.id == entryId) it.copy(placeDescription = description) else it
                }
            )
        }
    }

    fun updateSimpleReview(entryId: Int, review: String) {
        _uiState.update { currentState ->
            currentState.copy(
                placeEntries = currentState.placeEntries.map {
                    if (it.id == entryId) it.copy(simpleReview = review) else it
                }
            )
        }
    }

    // 장소 항목을 삭제하는 함수
    fun deletePlaceEntry(entryId: Int) {
        _uiState.update { currentState ->
            var updatedPlaceEntries = currentState.placeEntries.filterNot { it.id == entryId }
            updatedPlaceEntries = updatedPlaceEntries.map {  placeEntry ->
                if(placeEntry.id < entryId) {
                    placeEntry
                } else if (placeEntry.id > entryId) {
                    placeEntry.copy(
                        id = placeEntry.id - 1
                    )
                } else {
                    placeEntry
                }
            }
            currentState.copy(
                placeEntries = updatedPlaceEntries
            )
        }
        //entryId update 해야함
    }

    // 지도 검색 화면에 해당하는 부분
    fun updatePlaceFinder(newQuery: String) {
        placeFinderState = placeFinderState.copy(query = newQuery)
    }

    fun searchingPlaces(context: Context) {
        if (placeFinderState.query.isNotBlank()){
            placeFinderState = placeFinderState.copy(loading = true)
            viewModelScope.launch {
                searchPlaces(context, placeFinderState.query) { result ->
                    placeFinderState = placeFinderState.copy(places = result, loading = false)
                }
            }
        }
    }

    fun resetPlaceFinder() {
        placeFinderState = PlaceFinderEntry()
    }

    // 날짜에 해당하는 Diary 있는지 확인하는 함수
    fun isDiaryWritten(date: String): Boolean = diaryList.any { it == date }

    // DB에 저장하는 함수 꼭!! 추가해야함
    fun saveDiary() {
        viewModelScope.launch {
            try {
                val jsonBody = mapDiaryUiStateToContents(_uiState.value)
                apiClient.postElement(
                    endpoint = "/diary/write",
                    jsonBody = jsonBody,
                    date = _uiState.value.date,
                    callback = {
                            success, response ->
                        if (success) {

                        } else {

                        }
                    })
            } catch (e: Exception) {

            }
        }
        diarySet = diarySet.plus(_uiState.value)
    }

    // 일기 읽을 때
    fun getDiary(date: String): DiaryUiState {
        val diary: DiaryUiState = DiaryUiState()
        val places: List<PlaceEntry> = emptyList()
        apiClient.getElements(
            endpoint = "/diary/read",
            date = date
        ) { success, response ->
            if (success) {
                val gson = Gson()
                val diaryResponseList: List<DiaryResponse> =
                    gson.fromJson(response, Array<DiaryResponse>::class.java).toList()

                for ((index, item) in diaryResponseList[0].map.withIndex()) {
                    var images = emptyList<String>()
                    if (item.images.isNotEmpty()) {

                    }
                    val place = PlaceEntry(index)
                    place.copy(
                        placeName = item.name,
                        placeDescription = item.placeDescription,
                        simpleReview = item.simpleReview,
                        latitude = item.x,
                        longitude = item.y,
                        images = images
                    )
                    places.plus(place)
                }
                diary.copy(
                    date = date,
                    title = diaryResponseList[0].title,
                    totalReview = diaryResponseList[0].review,
                    placeEntries = places
                )


            } else {

            }
        }
        return diary
    }

    // 일기 삭제
    fun deleteDiary(date: String) {
        val diaryToRemove = diarySet.find { it.date == date }
        val idToRemove = diaryList.find { it == date }
        if (idToRemove != null) {
            // endpoint 수정해야함
            apiClient.delete(
                endpoint = "/diary/delete",
                date = diaryToRemove!!.date,
                callback = { success, response->
                    if (success) {

                    } else {

                    }
                })
        }
        fetchDiaries()

        diarySet = diarySet.minus(diaryToRemove!!)
    }
}

fun mapDiaryUiStateToContents(diaryUiState: DiaryUiState): String {
// Contents 객체 생성

    var locations = ""

    for (location in diaryUiState.placeEntries) {
        var images = ""
        for(image in location.images) {
            images += """
                $image /parsing/
            """.trimIndent()
        }

        locations += """
        {
        "locationKey": {
            "seq": ${location.id}
        },
        "name": "${location.placeName}",
        "x": ${location.latitude},
        "y": ${location.longitude},
        "images": "$images",
        "placeDescription": "${location.placeDescription}",
        "simpleReview": "${location.simpleReview}"
        },
    """.trimIndent()
    }

    if (locations.length >= 2) {
        locations = locations.dropLast(1)
    }

    val contents = """
        {
        "title": "${diaryUiState.title}",
        "review": "${diaryUiState.totalReview}",
        "date": "${diaryUiState.date}",
        "map": [$locations]
        }
    """.trimIndent()

    return contents
}