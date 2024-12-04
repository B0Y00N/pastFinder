package com.example.pastfinder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPage(
    navController: NavController,
    reminderViewModel: ReminderViewModel
) {
    var showAddGoalPopup by remember { mutableStateOf(false) } // 팝업 표시 여부 상태
    val goalExist = if (reminderViewModel.goalList.isNotEmpty()) Arrangement.Top else Arrangement.Center

    // "목표 추가" 버튼 클릭 시 팝업 표시
    if (showAddGoalPopup) {
        AddGoalPopup(
            reminderViewModel = reminderViewModel,
            onDismiss = { showAddGoalPopup = false },
            onSave = { goal ->
                reminderViewModel.saveGoal(goal) // 목표 추가
                showAddGoalPopup = false // 팝업 닫기
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        reminderViewModel.initGoalState()
                        showAddGoalPopup = true
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "목표 추가")
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = goalExist

            ) {
                if (reminderViewModel.goalList.size == 0) {
                    items(1){
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = " + 버튼을 눌러",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "목표를 추가해보세요!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    items(reminderViewModel.goalList.size) { index ->
                        GoalCard(
                            goal = reminderViewModel.goalList[index],
                            onDelete = {
                                reminderViewModel.deleteGoal(index)
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun GoalCard(
    goal: Goal,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // 삭제 버튼
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }
                Column {
                    Text(text = goal.content, fontWeight = FontWeight.Bold)
                    Text(text = "시작: ${goal.startDate} / 종료: ${goal.endDate}")
                }
            }
        }
    }
}

@Composable
fun AddGoalPopup(
    reminderViewModel: ReminderViewModel,
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit
) {

    val reminderUiState by reminderViewModel.goalState.collectAsState()
    var isStartDatePickerVisible by remember { mutableStateOf(false) }
    var isEndDatePickerVisible by remember { mutableStateOf(false) }


    // AddGoalPopup UI
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("목표 추가") },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                // 목표 내용 입력
                TextField(
                    value = reminderUiState.content,
                    onValueChange = { reminderViewModel.updateContent(it) },
                    label = { Text("목표 내용") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 시작 날짜 선택
                Text(text = "시작 날짜: ${reminderUiState.startDate}", fontWeight = FontWeight.Bold)
                TextButton(onClick = { isStartDatePickerVisible = true }) {
                    Text("시작 날짜 선택")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 종료 날짜 선택
                Text(text = "종료 날짜: ${reminderUiState.endDate}", fontWeight = FontWeight.Bold)
                TextButton(onClick = { isEndDatePickerVisible = true }) {
                    Text("종료 날짜 선택")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(reminderUiState)
                    onDismiss() // 저장 후 팝업 닫기
                },
                enabled = reminderUiState.content.isNotEmpty()
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("취소")
            }
        }
    )

    if(isStartDatePickerVisible) {
        ShowDatePickerDialog(
            selectedDate = reminderUiState.startDate,
            onClickCancel = { isStartDatePickerVisible = false},
            onClickConfirm = { selectedDate ->
                reminderViewModel.updateDate(newDate = selectedDate, id = 0)
                isStartDatePickerVisible = false
            }
        )
    }
    if(isEndDatePickerVisible) {
        ShowDatePickerDialog(
            selectedDate = reminderUiState.endDate,
            onClickCancel = { isEndDatePickerVisible = false},
            onClickConfirm = { selectedDate ->
                reminderViewModel.updateDate(newDate = selectedDate, id = 1)
                isEndDatePickerVisible = false
            }
        )
    }
}

// 날짜 선택기 클릭 시 호출되는 함수
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDatePickerDialog(
    selectedDate: String?, // 선택된 날짜
    onClickCancel: () -> Unit, // 취소 버튼 클릭 시 처리
    onClickConfirm: (yyyyMMdd: String) -> Unit // 확인 버튼 클릭 시 처리, 선택된 날짜 반환
) {
    // DatePickerDialog 컴포저블
    DatePickerDialog(
        onDismissRequest = { onClickCancel() }, // 다이얼로그 외부 클릭 시 취소 처리
        confirmButton = {}, // 확인 버튼은 아래에서 커스터마이징
        colors = DatePickerDefaults.colors(
            containerColor = Color.White // 배경 색상
        ),
        shape = RoundedCornerShape(6.dp) // 모서리 둥글게 처리
    ) {
        // 날짜 피커 상태 설정
        val datePickerState = rememberDatePickerState(
            yearRange = 2024..2025, // 연도 범위
            initialDisplayMode = DisplayMode.Picker, // 피커 모드로 초기화
            initialSelectedDateMillis = selectedDate?.let {
                // 선택된 날짜가 있을 경우 해당 날짜를 밀리초로 변환
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC") // UTC로 처리
                }
                formatter.parse(it)?.time ?: System.currentTimeMillis() // 날짜 파싱 실패 시 현재 시간
            } ?: System.currentTimeMillis(), // 초기 선택 날짜가 없을 경우 현재 시간
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // 날짜가 선택 가능한지 여부를 결정하는 조건
                    return true
                }
            }
        )

        // DatePicker
        DatePicker(
            state = datePickerState, // 상태 전달
        )

        // 버튼 행(Row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            // 취소 버튼
            Button(onClick = {
                onClickCancel() // 취소 시 호출
            }) {
                Text(text = "취소")
            }

            Spacer(modifier = Modifier.width(5.dp)) // 버튼 사이의 간격

            // 확인 버튼
            Button(onClick = {
                // 선택된 날짜 밀리초 값 가져오기
                datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                    // 밀리초 값을 yyyyMMdd 형식으로 변환
                    val yyyyMMdd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateMillis))

                    onClickConfirm(yyyyMMdd) // 선택된 날짜를 반환
                }
            }) {
                Text(text = "확인")
            }
        }
    }
}