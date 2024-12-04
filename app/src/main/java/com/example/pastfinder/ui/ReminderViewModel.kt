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
        goalList = goalList.plus(goal)
    }

    // Goal 삭제하는 함수
    fun deleteGoal(index: Int) {
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

