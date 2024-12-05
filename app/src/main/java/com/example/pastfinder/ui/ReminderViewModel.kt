package com.example.pastfinder.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

class ReminderViewModel: ViewModel() {

    var goalList by mutableStateOf<List<Goal>>(emptyList())

    private var _goalState = MutableStateFlow(Goal())
    var goalState: StateFlow<Goal> = _goalState.asStateFlow()

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
        //  sendGoalToLocalHost(goal)
        goalList = goalList.plus(goal)
    }

    // Goal 삭제하는 함수
    fun deleteGoal(index: Int) {
        sendDeleteRequestWithBodyToLocalhost(goalList[index])

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

fun sendGoalToLocalHost(goal: Goal) {
    val url = "" // 로컬 서버 URL, 나중에 추가
    val client = OkHttpClient()

    val jsonString = Json.encodeToString(goal)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = jsonString.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println("전송 실패: ${e.message}")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                println("전송 성공: ${response.body?.string()}")
            } else {
                println("서버 응답 실패: ${response.code}")
            }
        }
    })
}

fun sendDeleteRequestWithBodyToLocalhost(goal: Goal) {
    val url = ""    //나중 추가
    val client = OkHttpClient()

    // JSON 본문 생성
    val jsonString = Json.encodeToString(goal)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = jsonString.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(url)
        .delete(requestBody) // DELETE 요청에 본문 포함
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println("전송 실패: ${e.message}")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                println("삭제 성공: ${response.body?.string()}")
            } else {
                println("서버 응답 실패: ${response.code}")
            }
        }
    })
}

