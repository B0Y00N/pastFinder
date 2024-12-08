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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DiaryViewModel(private val apiClient: ApiClient) : ViewModel() {

    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationStatus: StateFlow<OperationStatus> = _operationStatus.asStateFlow()

    private var diaryList by mutableStateOf<List<String>>(emptyList())
    // 선택된 날짜의 Diary state
    private var _uiState = MutableStateFlow(DiaryUiState())
    var uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    var placeFinderState by mutableStateOf(PlaceFinderEntry())
        private set

    // 서버로부터 일기 적힌 날짜 받아오는 함수
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
                _operationStatus.value = OperationStatus.Loading
                val jsonBody = mapDiaryUiStateToContents(_uiState.value)
                apiClient.postElement(
                    endpoint = "/diary/write",
                    jsonBody = jsonBody,
                    date = _uiState.value.date,
                    callback = { success, _ ->
                        _operationStatus.value = if (success) OperationStatus.Success else OperationStatus.Error
                    }
                )
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error
            }
        }
    }

    // 일기 읽을 때
    suspend fun getDiary(date: String): DiaryUiState {
        return suspendCancellableCoroutine { continuation ->
            apiClient.getElements(
                endpoint = "/diary/read",
                date = date
            ) { success, response ->
                if (success) {
                    try {
                        val gson = Gson()
                        val diaryResponse: DiaryResponse =
                            gson.fromJson(response, DiaryResponse::class.java)

                        // PlaceEntry 리스트 생성
                        val places = diaryResponse.map.mapIndexed { index, item ->
                            val images = if (item.images.isNotEmpty()) {
                                item.images.split(" /parsing/").filter { it.isNotEmpty() } // 이미지가 쉼표로 구분된 문자열이라고 가정
                            } else {
                                emptyList()
                            }
                            PlaceEntry(
                                id = index,
                                placeName = item.name,
                                placeDescription = item.placeDescription,
                                simpleReview = item.simpleReview,
                                latitude = item.x,
                                longitude = item.y,
                                images = images
                            )
                        }

                        // DiaryUiState 생성
                        val diary = DiaryUiState(
                            date = date,
                            title = diaryResponse.title,
                            totalReview = diaryResponse.review,
                            placeEntries = places
                        )

                        _uiState.update { currentState ->
                            currentState.copy(
                                placeEntries = diary.placeEntries
                            )
                        }

                        // 결과 반환
                        continuation.resume(diary)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                } else {
                    continuation.resumeWithException(Exception("Failed to fetch diary"))
                }
            }
        }
    }

    // 일기 삭제
    fun deleteDiary(date: String) {

        val idToRemove = diaryList.find { it == date }
        if (idToRemove != null) {
            viewModelScope.launch {
                try {
                    _operationStatus.value = OperationStatus.Loading
                    apiClient.delete(
                        endpoint = "/diary/delete",
                        date = idToRemove,
                        callback = { success, _ ->
                            _operationStatus.value = if (success) OperationStatus.Success else OperationStatus.Error
                        }
                    )
                } catch (e: Exception) {
                    _operationStatus.value = OperationStatus.Error
                }
            }
        }

    }

    fun resetOperationStatus() {
        _operationStatus.value = OperationStatus.Idle
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
        "placeDescription": "${location.placeDescription.trimIndent()}",
        "simpleReview": "${location.simpleReview}"
        },
    """.trimIndent()
    }

    if (locations.length >= 2) {
        locations = locations.dropLast(1)
    }

    val contents = """
        {
        "title": "${diaryUiState.title.trimIndent()}",
        "review": "${diaryUiState.totalReview.trimIndent()}",
        "date": "${diaryUiState.date}",
        "map": [$locations]
        }
    """.trimIndent()

    return contents
}

sealed class OperationStatus {
    object Idle : OperationStatus()
    object Loading : OperationStatus()
    object Success : OperationStatus()
    object Error : OperationStatus()
}