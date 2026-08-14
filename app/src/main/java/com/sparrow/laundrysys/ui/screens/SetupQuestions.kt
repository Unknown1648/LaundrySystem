package com.sparrow.laundrysys.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sparrow.laundrysys.classes.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupQuestions(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser
    val isLoading by authViewModel.isLoading
    val errorMessage by authViewModel.errorMessage

    // Predefined security questions
    val predefinedQuestions = listOf(
        "What was your childhood nickname?",
        "What is the name of your first pet?",
        "What was your first car?",
        "What elementary school did you attend?",
        "What is your mother's maiden name?",
        "In which city were you born?",
        "What was your favorite food as a child?",
        "What is the name of your favorite teacher?",
        "What is your favorite movie?"
    )

    // Track expanded state for each dropdown
    var expandedStates by remember { mutableStateOf(List(3) { false }) }

    // Track selected questions and answers
    var selectedQuestions by remember { mutableStateOf(List(3) { predefinedQuestions[0] }) }
    var answers by remember { mutableStateOf(List(3) { "" }) }

    // Track if we have duplicate questions
    val hasDuplicateQuestions = selectedQuestions.toSet().size != 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Recovery Questions") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose 3 different security questions and provide answers",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            repeat(3) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Question ${index + 1}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedStates[index],
                            onExpandedChange = {
                                val newExpandedStates = expandedStates.toMutableList()
                                newExpandedStates[index] = !expandedStates[index]
                                expandedStates = newExpandedStates
                            },
                        ) {
                            TextField(
                                readOnly = true,
                                value = selectedQuestions[index],
                                onValueChange = { },
                                label = { Text("Select Question") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expandedStates[index]
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedStates[index],
                                onDismissRequest = {
                                    val newExpandedStates = expandedStates.toMutableList()
                                    newExpandedStates[index] = false
                                    expandedStates = newExpandedStates
                                },
                            ) {
                                predefinedQuestions.forEach { question ->
                                    DropdownMenuItem(
                                        text = { Text(question) },
                                        onClick = {
                                            val newQuestions = selectedQuestions.toMutableList()
                                            newQuestions[index] = question
                                            selectedQuestions = newQuestions

                                            // Close the dropdown after selection
                                            val newExpandedStates = expandedStates.toMutableList()
                                            newExpandedStates[index] = false
                                            expandedStates = newExpandedStates
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = answers[index],
                            onValueChange = {
                                val newAnswers = answers.toMutableList()
                                newAnswers[index] = it
                                answers = newAnswers
                            },
                            label = { Text("Answer") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (hasDuplicateQuestions) {
                Text(
                    text = "Please select three different security questions",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val recoveryQuestions = mutableListOf<Map<String, String>>()
                    for (i in 0 until 3) {
                        recoveryQuestions.add(mapOf(
                            "question" to selectedQuestions[i],
                            "answer" to answers[i]
                        ))
                    }

                    authViewModel.setRecoveryQuestions(
                        currentUser!!.username,
                        recoveryQuestions
                    ) { success, _ ->
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                },
                enabled = !isLoading &&
                        answers.all { it.isNotBlank() } &&
                        !hasDuplicateQuestions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Questions")
                }
            }

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancel")
            }
        }
    }
}