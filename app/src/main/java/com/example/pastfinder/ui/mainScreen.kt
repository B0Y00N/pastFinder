package com.example.pastfinder.ui


import android.widget.CalendarView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    diaryViewModel: DiaryViewModel = viewModel(),
    reminderViewModel: ReminderViewModel = viewModel()
) {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale("ko", "KR"))
    val date = Date()

    var yearState by rememberSaveable { mutableStateOf(formatter.format(date).split("-").first()) }
    var monthState by rememberSaveable { mutableStateOf(formatter.format(date).split("-")[1]) }
    var dayState by rememberSaveable { mutableStateOf(formatter.format(date).split("-").last()) }
    var isLoggedIn by rememberSaveable { mutableStateOf(true) }

    var isDeleteDiaryClicked by rememberSaveable { mutableStateOf(false) }
    var isLogoutClicked by rememberSaveable { mutableStateOf(false) }

    var isReminderClicked by rememberSaveable { mutableStateOf(false) }
    var reminderIndex by rememberSaveable { mutableStateOf(-1) }
    var isReminderEndDate by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "PASTFINDER") },
                actions = {
                    IconButton(onClick = {
                        isLogoutClicked = true
                    }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 32.dp),
                onClick = {
                navController.navigate("reminderPage")
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add remind")
            }
        }
        //bottom에 리마인더들 표시해야함
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )  {
            LazyColumn{
                item {
                    //preview에서는 한글이나 device에서는 영어로 나타나는 오류 있음
                    //추후 다른 툴 찾거나 수정 필요
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { CalendarView(it) }
                    ) { calendarView ->

                        val selectDate = "${yearState}-${monthState}-${dayState}"
                        val selectedDate = formatter.parse(selectDate) ?: Date()

                        calendarView.date = selectedDate.time

                        calendarView.setOnDateChangeListener { _, year, month, day ->
                            yearState = year.toString()
                            monthState = (month + 1).toString()
                            dayState = day.toString()
                        }
                    }
                }

                stickyHeader {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "${monthState}월 ${dayState}일의 기록"
                    )
                }

                item {
                    val currentDate = if (dayState.length == 1) "$yearState-$monthState-0$dayState" else "$yearState-$monthState-$dayState"
                    if(!diaryViewModel.isDiaryWritten(currentDate)) {
                        FilledTonalButton(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            shape = RoundedCornerShape(10.dp),
                            onClick = {
                                diaryViewModel.createDiary(currentDate)
                                navController.navigate("writeDiaryPage/${yearState}/${monthState}/${dayState}")
                            }
                        ) {
                            Text(
                                modifier = Modifier.padding(32.dp),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                text = "오늘의 일기 쓰기"
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            FilledTonalButton(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White
                                ),
                                onClick = {
                                    navController.navigate("readDiaryPage/${yearState}/${monthState}/${dayState}")
                                }
                            ) {
                                Text(
                                    modifier = Modifier.padding(32.dp),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    text = "일기 읽어보기"
                                )
                            }
                            TextButton(
                                modifier = Modifier.align(Alignment.End),
                                onClick = { isDeleteDiaryClicked = true}
                            ) {
                                Text(text = "삭제할래요")
                            }
                        }

                    }
                    Spacer(modifier = Modifier.padding(vertical = 40.dp))
                }

                // 수정 좀 보자...
                stickyHeader {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        if(reminderViewModel.goalList.isNotEmpty()) {
                            reminderViewModel.goalList.forEachIndexed { index, goal ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .background(Color.LightGray)
                                        .clickable {
                                            isReminderClicked = true
                                            reminderIndex = index
                                        }
                                ) {
                                    Text(
                                        modifier = Modifier.padding(5.dp),
                                        text = goal.content
                                    )
                                }
                            }
                        }
                    }



                }
            }
        }
    }

    if(reminderViewModel.goalList.isNotEmpty()) {
        reminderViewModel.goalList.forEachIndexed{ index, goal ->
            // 종료 날짜가 오늘인 goal 존재하는지 검사
            if(goal.endDate.equals(reminderViewModel.getCurrentDate())) {
                isReminderEndDate = true
                reminderIndex = index
            }
        }
    }

    if(isReminderEndDate) {
        GoalEndPopUp(
            goal = reminderViewModel.goalList[reminderIndex],
            onDismiss = {
                reminderViewModel.deleteGoal(reminderIndex)
                isReminderEndDate = false
                reminderIndex =-1
            }
        )
    }

    if(isReminderClicked) {
        ReadGoalPopUp(
            goal = reminderViewModel.goalList[reminderIndex],
            onDismiss = {
                isReminderClicked = false
                reminderIndex =-1
            }
        )
    }

    if(isDeleteDiaryClicked) {
        val currentDate = if (dayState.length == 1) "$yearState-$monthState-0$dayState" else "$yearState-$monthState-$dayState"

        DeleteDiaryPopUp(
            onDismiss = { isDeleteDiaryClicked = false },
            onDelete = {
                diaryViewModel.deleteDiary(currentDate)
                isDeleteDiaryClicked = false
            }
        )
    }

    if(isLogoutClicked) {
        LogoutPopUp(
            onDismiss = { isLogoutClicked = false},
            onDelete = {
                isLogoutClicked = false
                isLoggedIn = false
                logout()
            }
        )
    }
}

@Composable
fun ReadGoalPopUp(
    goal: Goal,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "확인")
            }
        },
        title = {
            Text(text = goal.content)
        },
        text = {
            Column {
                Text(text = "시작 날짜")
                Text(text = goal.startDate)
                Spacer(modifier = Modifier.padding(vertical = 16.dp))
                Text(text = "종료 날짜")
                Text(text = goal.endDate)
            }
        }
    )
}

@Composable
fun GoalEndPopUp(
    goal: Goal,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "종료하기")
            }
        },
        title = {
            Text(text = "\"${goal.content}\"의 종료 날짜가 되었어요!")
        },
        text = {
            Column {
                Text(text = "시작 날짜")
                Text(text = goal.startDate)
                Spacer(modifier = Modifier.padding(vertical = 16.dp))
                Text(text = "종료 날짜")
                Text(text = goal.endDate)
            }
        }
    )
}

@Composable
fun DeleteDiaryPopUp(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "취소")
            }
        },
        confirmButton = {
            Button(
                onClick = onDelete
            ) {
                Text(text = "삭제하기")
            }
        },
        title = {
            Text(text = "정말 삭제하시겠어요?")
        },
        text = {
            Column {
                Text(text = "삭제된 일기는 되돌릴 수 없어요.")
            }
        }
    )
}

@Composable
fun LogoutPopUp(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "취소")
            }
        },
        confirmButton = {
            Button(
                onClick = onDelete
            ) {
                Text(text = "로그아웃")
            }
        },
        title = {
            Text(text = "로그아웃하시겠어요?")
        }
    )
}


fun logout() {
    /* 추후 구현 */
}