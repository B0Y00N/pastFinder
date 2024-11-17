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

    private var diaryMap by mutableStateOf<Map<String, DiaryUiState>>(emptyMap())
    // 작성된 일기가 있는 날짜를 저장하는 Set
    private var dateSet by mutableStateOf<Set<String>>(emptySet())
    // 선택된 날짜의 Diary state
    private var _uiState = MutableStateFlow(DiaryUiState())
    var uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    // 날짜를 인자로 받아 Diary state를 업데이트하는 함수
    fun createDiary(date: String) {
        _uiState.update { currentState ->
            currentState.copy(
                date = date
            )
        }
        dateSet = dateSet.plus(date) // 날짜 추가
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

    // 날짜를 인자로 받아 해당 날짜를 삭제하는 함수
    fun deleteDate(date: String) {
        dateSet = dateSet.minus(date)
    }

    // 날짜에 해당하는 Diary 있는지 확인하는 함수
    fun isDiaryWritten(date: String): Boolean = dateSet.any { it == date }

    // DB에 저장하는 함수 꼭!! 추가해야함
    fun saveDiary() {
        diaryMap = diaryMap.plus(_uiState.value.date to _uiState.value)
    }

    // 일기 읽을 때...?
    fun getDiary(date: String): DiaryUiState {
        return diaryMap.get(date)!!
    }
}
