package com.example.pastfinder.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pastfinder.api.ApiClient
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
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderViewModel(private val apiClient: ApiClient): ViewModel() {

    private var goalIdList by mutableStateOf<List<Long>>(emptyList())
    var goalList by mutableStateOf<List<Goal>>(emptyList())

    private var _goalState = MutableStateFlow(Goal())
    var goalState: StateFlow<Goal> = _goalState.asStateFlow()

    fun fetchReminders() {
        apiClient.getElements("/reminder/get") { success, response ->
            if (success) {
                val gson = Gson()
                val reminderIdList: List<Long> =
                    gson.fromJson(response, Array<Long>::class.java).toList()
                goalIdList = reminderIdList
            } else {

            }
        }
    }

    // Goal 추가 버튼 누를 때 호출
    fun initGoalState() {
        _goalState.update { currentState ->
            currentState.copy(
                content = "",
                startDate = getCurrentDate(),
                endDate = getCurrentDate()
            )
        }
    }

    // Goal 저장하는 함수
    fun saveGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                val jsonBody = """
                    {
                    "start_datetime": "${goal.startDate}",
                    "end_datetime": "${goal.endDate}",
                    "contents": "${goal.content}"
                    }
                """.trimIndent()
                apiClient.postElement("/reminder/write", jsonBody) { success, response ->
                    if (success) {

                    } else {

                    }
                }
            } catch (e: Exception) {

            }
        }
        goalList = goalList.plus(goal)
    }

    // Goal 삭제하는 함수
    fun deleteGoal(index: Int) {
        viewModelScope.launch {
            val jsonBody = """
                {
                "id": ${goalIdList.get(index)}
                }
            """.trimIndent()
            try {
                apiClient.delete("/reminder/delete", jsonBody) { success, response ->
                    if (success) {

                    } else {

                    }
                }
            } catch (e: Exception) {

            }
        }
        fetchReminders()

        val updatedGoals = goalList.toMutableList().apply {
            removeAt(index)
        }
        if(updatedGoals.isEmpty()){
            goalList = emptyList()
        } else {
            goalList = updatedGoals
        }
    }

    // 목표 내용 update하는 함수
    fun updateContent(newContent: String) {
        _goalState.update { currentState ->
            currentState.copy(
                content = newContent
            )
        }
    }

    fun updateDate(newDate: String, id: Int) { // 0이면 startDate, 1이면 endDate
        if (id == 0) {
            _goalState.update { currentState ->
                currentState.copy(
                    startDate = newDate
                )
            }
        } else if (id == 1) {
            _goalState.update { currentState ->
                currentState.copy(
                    endDate = newDate
                )
            }
        }
    }

    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        return formatter.format(Date())
    }
}