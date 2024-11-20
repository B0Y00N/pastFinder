package com.example.pastfinder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState


@Composable
fun MapsScreen(
    navController: NavController
) {
    // 초기 위치 설정 (예: 서울역)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            LatLng(37.5563, 126.9698), // 서울역 (위도, 경도)
            15f, // 줌 레벨
            0f,  // 기울기, 0은 평평한 상태
            0f // 방향, 0은 기본 (북쪽)
        )
    }

    // 지도 화면 표시
    // GoogleMap 컴포저블을 사용하여 지도 표시
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // 서울역에 마커 추가
        Marker(
            state = MarkerState(LatLng(37.5563, 126.9698)),
            title = "서울역",
            snippet = "서울의 주요 기차역"
        )
    }
}
