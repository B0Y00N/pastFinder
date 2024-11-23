package com.example.pastfinder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pastfinder.ui.theme.PastFinderTheme
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapSearchActivity : ComponentActivity() {

    // 권한 요청 결과를 처리할 콜백 함수
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 권한이 허용된 경우 API 호출 시작
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // 권한이 거부된 경우 사용자에게 알림
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 항상 Places API를 초기화합니다.
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "")
        }

        // 위치 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // 위치 권한이 없다면 권한 요청
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setContent {
            val navController = rememberNavController()
            PlaceFinderScreen(navController)
        }
    }
}

@Composable
fun PlaceFinderScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search Places") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (query.isNotBlank()) {
                    loading = true
                    coroutineScope.launch {
                        searchPlaces(context, query) { result ->
                            places = result
                            loading = false
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please enter a search query",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(places) { place ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .clickable {
                                if (place.latLng != null) {
                                    val intent = Intent(context, MapsActivity::class.java).apply {
                                        putExtra("PLACE_LAT", place.latLng!!.latitude)
                                        putExtra("PLACE_LNG", place.latLng!!.longitude)
                                        putExtra("PLACE_NAME", place.name)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "위치 아이콘"
                        )
                        Text(
                            text = place.name ?: "Unknown Place",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 10.dp)
                                .fillMaxWidth()
                        )
                    }

                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun searchPlaces(context: Context, query: String, onResult: (List<Place>) -> Unit) {
    withContext(Dispatchers.IO) {
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyAYBaY6MNaZyAlVUOcADqOZC9jBnSC1T2E")
        }
        val placesClient = Places.createClient(context)

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions

                val places = mutableListOf<Place>()

                predictions.forEach { prediction ->
                    val placeId = prediction.placeId
                    val placeRequest = FetchPlaceRequest.builder(placeId, listOf(Place.Field.NAME, Place.Field.LAT_LNG)).build()

                    placesClient.fetchPlace(placeRequest)
                        .addOnSuccessListener { fetchResponse ->
                            val place = fetchResponse.place
                            places.add(place)
                            if (places.size == predictions.size) {
                                onResult(places)
                            }
                        }
                        .addOnFailureListener {
                            if (places.size == predictions.size) {
                                onResult(places)
                            }
                        }
                }
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreenWithMarker(navController: NavController, latitude: Double, longitude: Double, name: String) {
    val location = LatLng(latitude, longitude)

    // 카메라 설정
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            location, // 전달받은 위치를 중심으로
            15f,      // 줌 레벨
            0f,       // 기울기
            0f        // 방향
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(), // Box가 전체 너비를 차지하도록
                        contentAlignment = Alignment.Center // 제목을 중앙 정렬
                    ) {
                        Text(
                            text = "지도 위치: $name",  // 타이틀로 이름 사용
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD8B0A1) // 옅은 갈색
                ),
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // 전달받은 위치에 마커 추가
                Marker(
                    state = MarkerState(position = location),
                    title = name,
                    snippet = "위도: $latitude, 경도: $longitude"
                )
            }
        }
    }
}

class MapsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val latitude = intent.getDoubleExtra("PLACE_LAT", 0.0)
        val longitude = intent.getDoubleExtra("PLACE_LNG", 0.0)
        val placeName = intent.getStringExtra("PLACE_NAME") ?: "Unknown Place"

        setContent {
            MapsScreenWithMarker(
                navController = rememberNavController(),
                latitude = latitude,
                longitude = longitude,
                name = placeName
            )
        }
    }
}
