package com.sparrow.laundrysys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.sparrow.laundrysys.classes.AuthViewModel
import com.sparrow.laundrysys.ui.theme.LaundryRoomTheme
import com.sparrow.laundrysys.utilities.AuthenticatedApp
import com.sparrow.laundrysys.utilities.UnauthenticatedApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaundryRoomTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.loadUserFromPrefs(context)
    }

    val currentUser by authViewModel.currentUser

    val navController = rememberNavController()

    if(currentUser != null)
        AuthenticatedApp(navController, authViewModel)
    else
        UnauthenticatedApp(navController, authViewModel)
}