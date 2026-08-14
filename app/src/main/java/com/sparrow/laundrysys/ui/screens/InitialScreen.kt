package com.sparrow.laundrysys.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.sparrow.laundrysys.classes.AuthViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box

@Composable
fun InitialScreen(navController: NavController, authViewModel: AuthViewModel) {
    val isLoading by authViewModel.isLoading
    var hasChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.checkIfAdminExists { exists ->
            navController.navigate(if (exists) "login" else "admin_registration") {
                popUpTo("initial") { inclusive = true }
            }
            hasChecked = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (!hasChecked || isLoading) {
            CircularProgressIndicator()
        }
    }
}
