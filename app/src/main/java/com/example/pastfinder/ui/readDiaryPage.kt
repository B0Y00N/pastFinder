package com.example.pastfinder.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadDiaryPage(
    navController: NavController,
    diaryViewModel: DiaryViewModel,
    year: String,
    month: String,
    day: String
) {

    val date = "$year-$month-$day"
    val diary = diaryViewModel.getDiary(date)

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TopAppBar(
                title = { Text("${year}년 ${month}월 ${day}의 기록") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    // 지도 아이콘 버튼 추가
                    IconButton(onClick = {
                        // 위도와 경도 리스트
                        val locationList = diaryViewModel.uiState.value.placeEntries.map { it.latitude to it.longitude }
                        /* 위 변수 활용해서 지도 페이지로 이동하는 네비게이션 액션 추가 */
                        /* 예시: navController.navigate("mapPage/@@@@")*/
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "지도"
                        )
                    }
                }
            )
        }

        item {
            // 일기 제목 입력 TextField (한글 입력 문제 해결 필요)
            Text(
                text = diary.title
            )
        }

        // Diary Entry Card
        items(diary.placeEntries.size) { index ->
            PlaceCard(placeEntry = diary.placeEntries[index])
        }

        item {
            // 일기 전체 내용 입력 TextField (총평)
            Text(
                text = diary.totalReview
            )
        }
    }
}

@Composable
fun PlaceCard(placeEntry: PlaceEntry) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 16.dp, 10.dp, 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${placeEntry.id}번째 장소",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Left
                )

                // 확장/축소 버튼
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "축소" else "확장"
                    )
                }
            }


            // 확장된 카드 내용
            if (isExpanded) {
                // 이미지 표시
                if (placeEntry.images.isNotEmpty()) {
                    LazyColumn (modifier = Modifier.height(200.dp)) {
                        items(placeEntry.images) { base64Image ->
                            val bitmap = base64ToBitmap(base64Image)
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = placeEntry.placeDescription
                )
            }
        }
    }
}