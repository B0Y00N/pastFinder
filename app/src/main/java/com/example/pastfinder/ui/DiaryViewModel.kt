package com.example.pastfinder.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DiaryViewModel : ViewModel() {

    private var diarySet by mutableStateOf<Set<DiaryUiState>>(emptySet())
    // 선택된 날짜의 Diary state
    private var _uiState = MutableStateFlow(DiaryUiState())
    var uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

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
    fun addImages(entryId: Int, images: List<Uri>) {
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

    // 날짜에 해당하는 Diary 있는지 확인하는 함수
    fun isDiaryWritten(date: String): Boolean = diarySet.any { it.date == date }

    // DB에 저장하는 함수 꼭!! 추가해야함
    fun saveDiary() {
        diarySet = diarySet.plus(_uiState.value)
    }

    // 일기 읽을 때
    fun getDiary(date: String): DiaryUiState {
        return diarySet.find { it.date == date }!!
    }

    // 일기 삭제
    fun deleteDiary(date: String) {
        val diaryToRemove = diarySet.find { it.date == date }
        diarySet = diarySet.minus(diaryToRemove!!)
    }
}
