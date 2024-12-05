package com.example.pastfinder.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreenWithRoute(
    navController: NavController,
    diaryViewModel: DiaryViewModel
) {
    val locationList = diaryViewModel.uiState.value.placeEntries.map { LatLng(it.latitude, it.longitude) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            locationList.firstOrNull() ?: LatLng(37.504548, 126.956949), // 첫 번째 위치를 중심으로 설정
            15f, // 줌 레벨
            0f, // 기울기
            0f // 방향
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "지금까지의 경로",
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // 각 위치에 마커 추가 및 한줄평 표시
                diaryViewModel.uiState.value.placeEntries.forEach { placeEntry ->
                    val location = LatLng(placeEntry.latitude, placeEntry.longitude)
                    Marker(
                        state = MarkerState(position = location),
                        title = "${placeEntry.id}. ${placeEntry.placeName}",
                        snippet = placeEntry.simpleReview
                    )
                }

                // 위치 리스트를 연결하는 Polyline 추가
                if (locationList.size > 1) {
                    Polyline(
                        points = locationList,
                        color = Color.Blue,
                        width = 15f,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )
                }
            }
        }
    }
}
