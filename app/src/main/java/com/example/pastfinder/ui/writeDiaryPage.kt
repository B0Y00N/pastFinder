package com.example.pastfinder.ui

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteDiaryPage(
    navController: NavController,
    diaryViewModel: DiaryViewModel = viewModel(),
    year: String,
    month: String,
    day: String
) {
    val date = "$year-$month-$day"

    // 상태 변수 선언
    val diaryUiState by diaryViewModel.uiState.collectAsState()

    var selectedID by rememberSaveable { mutableStateOf(-1) }

    val pickMultipleImagesLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty() && selectedID != -1) {
                diaryViewModel.addImages(entryId = selectedID, images = uris)
            }
        }


    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TopAppBar(
                title = { Text("일기 쓰기") },
                navigationIcon = {
                    IconButton(onClick = {
                        //임시저장 할 것인지 고민 필요
                        //왜 제대로 삭제가 안되냐...ㅠㅠ
                        diaryViewModel.deleteDate(date)
                        navController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    // 지도 아이콘 버튼 추가
                    IconButton(onClick = {
                        //지도 페이지로 이동하는 네비게이션 액션 추가
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
            OutlinedTextField(
                value = diaryUiState.title,
                onValueChange = {
                    diaryViewModel.updateTitle(it)
                },
                label = { Text("일기 제목") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // "장소 추가" 버튼
        item {
            Button(
                onClick = {
                    //추가 전에 장소 검색하는 페이지 추가 필요
                    // 새로운 DiaryEntry를 리스트에 추가
                    val newEntry = PlaceEntry(id = diaryUiState.placeEntries.size + 1)
                    diaryViewModel.addPlaceEntry(newEntry)  // 새로운 장소 추가
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("장소 추가")
            }
        }

        // Diary Entry Card
        items(diaryUiState.placeEntries.size) { index ->
            PlaceEntryCard(
                placeEntry = diaryUiState.placeEntries[index],
                onAddImage = {
                    selectedID = diaryUiState.placeEntries[index].id
                    pickMultipleImagesLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onDescriptionChange = {
                    diaryViewModel.updatePlaceDescription(diaryUiState.placeEntries[index].id, it)  // 장소 설명 업데이트
                },
                onDelete = {
                    diaryViewModel.deletePlaceEntry(diaryUiState.placeEntries[index].id)  // 장소 삭제
                }
            )
        }

        item {
            // 일기 전체 내용 입력 TextField (총평)
            OutlinedTextField(
                value = diaryUiState.totalReview,
                onValueChange = {
                    diaryViewModel.updateTotalReview(it)
                },
                label = { Text("오늘의 총평") },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                maxLines = 10,
                minLines = 5
            )
        }

        item {
            // 저장 버튼
            Button(
                onClick = { /* 저장 로직 구현 */
                    diaryViewModel.saveDiary()
                    navController.navigateUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("저장")
            }
        }
    }
}

@Composable
fun PlaceEntryCard(
    placeEntry: PlaceEntry,
    onAddImage: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDelete: () -> Unit
) {
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
                // 삭제 버튼
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }

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
                    LazyRow(modifier = Modifier.height(200.dp)) {
                        items(placeEntry.images) { uri ->
                            val imageBitmap = UriUtils().uriToBitmap(LocalContext.current, uri)?.asImageBitmap()
                            imageBitmap?.let {
                                Image(
                                    bitmap = it,
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

                Button(onClick = onAddImage ) {
                    Text("사진 추가")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = placeEntry.placeDescription,
                    onValueChange = { onDescriptionChange(it)},
                    label = { Text("장소 설명") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )

            }
        }
    }
}


class UriUtils {
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        val contentResolver: ContentResolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }
    }
}