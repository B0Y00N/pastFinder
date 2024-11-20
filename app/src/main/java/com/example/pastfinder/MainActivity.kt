package com.example.pastfinder

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pastfinder.ui.DiaryViewModel
import com.example.pastfinder.ui.LoginPage
import com.example.pastfinder.ui.MainScreen
import com.example.pastfinder.ui.MapsScreen
import com.example.pastfinder.ui.ReadDiaryPage
import com.example.pastfinder.ui.RegisterPage
import com.example.pastfinder.ui.ReminderPage
import com.example.pastfinder.ui.ReminderViewModel
import com.example.pastfinder.ui.WriteDiaryPage
import com.example.pastfinder.ui.theme.PastFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PastFinderTheme {


                val diaryViewModel = viewModel<DiaryViewModel>()
                val reminderViewModel = viewModel<ReminderViewModel>()

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "loginPage"
                ) {
                    composable(route = "loginPage") {
                        LoginPage(navController = navController) {
                            navController.navigate("registerPage")
                        }
                    }
                    composable(route = "registerPage"){
                        RegisterPage(
                            backToLoginPage = {
                                navController.navigateUp()
                            }
                        )
                    }
                    composable(route = "mainScreen") {
                        MainScreen(
                            navController = navController,
                            diaryViewModel = diaryViewModel
                        )
                    }
                    composable(
                        route = "writeDiaryPage/{year}/{month}/{day}",
                        arguments = listOf(
                            navArgument("year") { type = NavType.StringType},
                            navArgument("month") { type = NavType.StringType },
                            navArgument("day") { type = NavType.StringType }
                        )
                    ) {
                        val year = it.arguments?.getString("year") ?: ""
                        val month = it.arguments?.getString("month") ?: ""
                        val day = it.arguments?.getString("day") ?: ""
                        WriteDiaryPage(
                            navController = navController,
                            diaryViewModel = diaryViewModel,
                            year = year,
                            month = month,
                            day = day
                        )
                    }
                    composable(
                        route = "readDiaryPage/{year}/{month}/{day}",
                        arguments = listOf(
                            navArgument("year") { type = NavType.StringType},
                            navArgument("month") { type = NavType.StringType },
                            navArgument("day") { type = NavType.StringType }
                        )
                    ) {
                        //추후 구현
                        val year = it.arguments?.getString("year") ?: ""
                        val month = it.arguments?.getString("month") ?: ""
                        val day = it.arguments?.getString("day") ?: ""
                        ReadDiaryPage(
                            navController = navController,
                            diaryViewModel = diaryViewModel,
                            year = year,
                            month = month,
                            day = day
                        )
                    }
                    composable(route = "reminderPage") {
                        ReminderPage(navController = navController, reminderViewModel = reminderViewModel)
                    }

                    composable(route = "map"){
                        MapsScreen(navController = navController)
                    }
                    /* 장소 검색 페이지와
                    *  지도 마킹 페이지 컴포저블
                    *  각각 추가 */
                }
            }
        }
    }
}

