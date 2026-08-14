package com.sparrow.laundrysys.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sparrow.laundrysys.classes.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassword(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()
    val context = LocalContext.current
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    BackHandler(enabled = hasUnsavedChanges) {
        authViewModel.showConfirmationDialog(
            "Discard changes?",
            "You have unsaved changes. Are you sure you want to go back?",
            onConfirm = { navController.navigateUp() }
        )
    }

    LaunchedEffect(forgotPasswordState.errorMessage) {
        forgotPasswordState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(forgotPasswordState.resetSuccess) {
        if (forgotPasswordState.resetSuccess) {
            Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
            authViewModel.clearForgotPasswordState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = forgotPasswordState.username,
                onValueChange = {
                    authViewModel.updateUsername(it)
                    hasUnsavedChanges = true
                },
                label = { Text("Username") },
                isError = forgotPasswordState.usernameError != null,
                supportingText = {
                    forgotPasswordState.usernameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !forgotPasswordState.isLoading && !forgotPasswordState.questionsLoaded
            )

            Button(
                onClick = { authViewModel.loadSecurityQuestions() },
                enabled = !forgotPasswordState.isLoading && forgotPasswordState.username.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (forgotPasswordState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Load Security Questions")
            }

            AnimatedVisibility(visible = forgotPasswordState.questionsLoaded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = forgotPasswordState.newPassword,
                        onValueChange = {
                            authViewModel.updateNewPassword(it)
                            hasUnsavedChanges = true
                        },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        isError = forgotPasswordState.passwordError != null,
                        supportingText = {
                            forgotPasswordState.passwordError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    PasswordStrengthIndicator(password = forgotPasswordState.newPassword)

                    OutlinedTextField(
                        value = forgotPasswordState.confirmPassword,
                        onValueChange = {
                            authViewModel.updateConfirmPassword(it)
                            hasUnsavedChanges = true
                        },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        isError = forgotPasswordState.confirmPasswordError != null,
                        supportingText = {
                            forgotPasswordState.confirmPasswordError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    Text("Answer at least 2 security questions correctly:", style = MaterialTheme.typography.bodyMedium)

                    forgotPasswordState.questions.take(3).forEachIndexed { index, question ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(question["question"] ?: "", fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = forgotPasswordState.answers.getOrElse(index) { "" },
                                    onValueChange = {
                                        authViewModel.updateAnswer(index, it)
                                        hasUnsavedChanges = true
                                    },
                                    label = { Text("Answer") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { authViewModel.resetPassword() },
                        enabled = !forgotPasswordState.isLoading && forgotPasswordState.isFormValid,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        if (forgotPasswordState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Reset Password")
                    }
                }
            }

            TextButton(
                onClick = {
                    if (hasUnsavedChanges) {
                        authViewModel.showConfirmationDialog(
                            "Discard changes?",
                            "You have unsaved changes. Are you sure you want to go back?",
                            onConfirm = { navController.navigate("login") }
                        )
                    } else {
                        navController.navigate("login")
                    }
                }
            ) {
                Text("Back to Login")
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = when {
        password.length < 8 -> 0.25f
        password.matches(Regex(".*[A-Z].*")) &&
                password.matches(Regex(".*[a-z].*")) &&
                password.matches(Regex(".*[0-9].*")) -> 1f
        password.matches(Regex(".*[A-Z].*")) ||
                password.matches(Regex(".*[0-9].*")) -> 0.5f
        else -> 0.25f
    }

    val color = when {
        strength < 0.5f -> MaterialTheme.colorScheme.error
        strength < 1f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    val strengthText = when {
        strength < 0.5f -> "Weak"
        strength < 1f -> "Medium"
        else -> "Strong"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { strength },
            modifier = Modifier.fillMaxWidth(),
            color = color,
        )

        Text(
            text = strengthText,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

