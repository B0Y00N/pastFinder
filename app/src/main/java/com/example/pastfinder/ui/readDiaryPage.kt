package com.example.pastfinder.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
    val date = if (day.length == 1) "$year-$month-0$day" else "$year-$month-$day"
    // DiaryUiState를 위한 상태 저장
    var diary by remember { mutableStateOf<DiaryUiState?>(null) }

    // LaunchedEffect를 사용해 suspend 함수 호출
    LaunchedEffect(date) {
        try {
            diary = diaryViewModel.getDiary(date)
        } catch (e: Exception) {
            Log.e("ReadDiaryPage", "Failed to fetch diary: ${e.message}")
        }    }

    // Diary를 읽는 동안 로딩 표시
    if (diary == null) {
        // 로딩 상태 표시
        CircularProgressIndicator()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        text = "${year}년 ${month}월 ${day}의 기록",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // 위도와 경도 리스트
                        //val locationList = diaryViewModel.uiState.value.placeEntries.map { it.latitude to it.longitude }

                        navController.navigate(route = "mapRoute")

                        /* 위 변수 활용해서 지도 페이지로 이동하는 네비게이션 액션 추가 */
                        /* 예시: navController.navigate("mapPage/@@@@")*/
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "지도",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }

        item {
            // 일기 제목
            diary?.let {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // 장소 정보 카드
        diary?.placeEntries?.let {
            items(it.size) { index ->
                PlaceCard(placeEntry = diary!!.placeEntries[index])
            }
        }

        item {
            // 총평 카드
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                diary?.let {
                    Text(
                        text = it.totalReview,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp, // 글씨 크기 증가
                            lineHeight = 28.sp // 읽기 편하게 행간 추가
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp) // 카드 안쪽 여백 추가
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceCard(placeEntry: PlaceEntry) {
    var isExpanded by remember { mutableStateOf(true) }
    var imagesClicked by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = placeEntry.placeName ,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Left
                )


            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"${ placeEntry.simpleReview } \"",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 이미지 표시
                if(placeEntry.images.isNotEmpty()) {
                    if(!imagesClicked) {
                        LazyRow(modifier = Modifier.height(250.dp)) {
                            items(placeEntry.images) { base64Image ->
                                val bitmap = base64ToBitmap(base64Image)
                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { imagesClicked = !imagesClicked }
                                    )
                                }
                            }
                        }
                    } else {
                        val size = 400 * placeEntry.images.size
                        LazyColumn(
                            modifier = Modifier
                                .height(size.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(placeEntry.images) { base64Image ->
                                val bitmap = base64ToBitmap(base64Image)
                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .size(it.width.dp, it.height.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { imagesClicked = !imagesClicked }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 장소 설명
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Text(
                        text = placeEntry.placeDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(5.dp)
                    )
                }

            }
        }
    }
}
