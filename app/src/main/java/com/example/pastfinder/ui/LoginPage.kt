package com.example.pastfinder.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pastfinder.api.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    navController: NavController,
    goToRegisterPage: () -> Unit,
    viewModel: LoginViewModel
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val loginStatus by viewModel.loginStatus

    val context = LocalContext.current

    LaunchedEffect(key1 = loginStatus) {
        when (loginStatus) {
            is LoginStatus.Success -> {
                Toast.makeText(context, (loginStatus as LoginStatus.Success).message, Toast.LENGTH_SHORT).show()
                // 일기랑 리마인더 리스트 받아오기 추가할 것
                navController.navigate(route = "mainScreen") {
                    popUpTo(route = "loginPage") { inclusive = true }
                }
            }
            is LoginStatus.Error -> {
                Toast.makeText(context, (loginStatus as LoginStatus.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {
                // 초기 상태
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text( text = "Login Page")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = { email = it },
                label = {
                    Text("E-mail")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = { password = it },
                label = {
                    Text("Password")
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { goToRegisterPage() }
            ) {
                Text(text = "Register your email")
            }
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    viewModel.loginUser(email, password)

                }
            ) {
                Text( text = "Login")
            }
            when (loginStatus) {
                is LoginStatus.Loading -> {
                    CircularProgressIndicator()
                }
                else -> {
                    // 초기 상태
                }
            }

        }
    }
}

