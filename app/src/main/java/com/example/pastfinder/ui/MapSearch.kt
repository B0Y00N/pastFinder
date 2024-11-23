package com.example.pastfinder

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.example.pastfinder.ui.theme.PastFinderTheme
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

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

        // 위치 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // 위치 권한이 없다면 권한 요청
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // 위치 권한이 이미 있는 경우
            Places.initialize(applicationContext, "YOUR_GOOGLE_MAPS_API_KEY")
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
                    Text(
                        text = place.name ?: "Unknown Place",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun searchPlaces(context: Context, query: String, onResult: (List<Place>) -> Unit) {
    withContext(Dispatchers.IO) {
        val placesClient = Places.createClient(context)

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions.map { prediction ->
                    Place.builder()
                        .setName(prediction.getFullText(null).toString())
                        .build()
                }
                onResult(predictions)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
