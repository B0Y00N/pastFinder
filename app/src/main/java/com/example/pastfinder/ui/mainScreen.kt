package com.example.pastfinder.ui


import android.widget.CalendarView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    diaryViewModel: DiaryViewModel = viewModel()
) {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    val date = Date()

    var yearState by rememberSaveable { mutableStateOf(formatter.format(date).split("-").first()) }
    var monthState by rememberSaveable { mutableStateOf(formatter.format(date).split("-")[1]) }
    var dayState by rememberSaveable { mutableStateOf(formatter.format(date).split("-").last()) }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(text = "PASTFINDER") })
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                if(!diaryViewModel.isDiaryWritten("$yearState-$monthState-$dayState")) {
                    FilledTonalButton(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            diaryViewModel.createDiary("$yearState-$monthState-$dayState")
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
                    FilledTonalButton(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
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
                }

            }
        }
    }
}
