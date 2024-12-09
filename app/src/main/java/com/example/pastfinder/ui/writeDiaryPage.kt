package com.example.pastfinder.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
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
import com.google.android.libraries.places.api.model.kotlin.place
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Base64


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteDiaryPage(
    navController: NavController,
    diaryViewModel: DiaryViewModel = viewModel(),
    year: String,
    month: String,
    day: String
) {
    val operationStatus by diaryViewModel.operationStatus.collectAsState()

    // 상태 변수 선언
    val diaryUiState by diaryViewModel.uiState.collectAsState()

    var selectedID by rememberSaveable { mutableStateOf(-1) }

    val context = LocalContext.current

    val pickMultipleImagesLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty() && selectedID != -1) {
                val base64Images = uris.map { uri ->
                    uriToBase64(context = context, uri = uri)
                }.filterNotNull()
                diaryViewModel.addImages(entryId = selectedID, images = base64Images)
            }
        }

    when (operationStatus) {
        is OperationStatus.Loading -> {
            // 로딩 상태 표시
            CircularProgressIndicator()
        }
        is OperationStatus.Success -> {
            // 성공 시 동작 처리
            navController.navigateUp()
            diaryViewModel.resetOperationStatus()
        }
        is OperationStatus.Error -> {
            // 에러 메시지 표시
            Toast.makeText(context, "작업에 실패했습니다.", Toast.LENGTH_SHORT).show()
            diaryViewModel.resetOperationStatus()
        }
        else -> { /* Idle 상태 처리 안함 */ }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TopAppBar(
                title = { Text("일기 쓰기") },
                navigationIcon = {
                    IconButton(onClick = {
                        //임시저장 할 것인지 고민 필요
                        navController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    // 지도 아이콘 버튼 추가
                    IconButton(onClick = {
                        // 위도와 경도 리스트
                        //val locationList = diaryUiState.placeEntries.map { it.latitude to it.longitude }

                        navController.navigate(route = "mapRoute")

                        //diaryViewModel.resetPlaceFinder()
                        //navController.navigate("mapSearch")
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
                    selectedID = diaryUiState.placeEntries.size + 1
                    /* 장소 검색하는 페이지 추가 필요 */
                    /* 예시: navController.navigate("searchPlacePage/@@@@") */
                    /* 해당 페이지에서 diaryViewModel.updatePlaceInfo() 함수 호출해야함! */
                    // 새로운 DiaryEntry를 리스트에 추가
                    val newEntry = PlaceEntry(id = diaryUiState.placeEntries.size + 1)
                    diaryViewModel.addPlaceEntry(newEntry)  // 새로운 장소 추가
                    diaryViewModel.resetPlaceFinder()
                    navController.navigate(route = "mapSearch/$selectedID")
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
                onAddSimpleReview = {
                    diaryViewModel.updateSimpleReview(diaryUiState.placeEntries[index].id, it) // 장소 한줄평 업데이트
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = diaryUiState.title.isNotEmpty() && diaryUiState.totalReview.isNotEmpty()
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
    onAddSimpleReview: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 16.dp, 10.dp, 16.dp)
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 삭제 버튼
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }
                Text(
                    text = if (placeEntry.placeName.isNotBlank()) placeEntry.placeName else "${placeEntry.id}번째 장소",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(15.dp))
            }


            // 확장된 카드 내용
            if (isExpanded) {
                // 이미지 표시
                if (placeEntry.images.isNotEmpty()) {
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

                OutlinedTextField(
                    value = placeEntry.simpleReview,
                    onValueChange = { onAddSimpleReview(it) },
                    label = { Text("장소 한줄평") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }
        }
    }
}


fun base64ToBitmap(base64String: String): Bitmap? {
    try {
        val decodedString = Base64.getDecoder().decode(base64String)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}


fun uriToBase64(context: Context, uri: Uri): String? {
    try {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

        // Bitmap을 바이트 배열로 변환
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Base64로 인코딩
        return Base64.getEncoder().encodeToString(byteArray)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}