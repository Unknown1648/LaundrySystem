package com.sparrow.laundrysys.classes

// State class for the forgot password screen
data class ForgotPasswordState(
    val username: String = "",
    val usernameError: String? = null,
    val newPassword: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val questions: List<Map<String, String>> = emptyList(),
    val answers: List<String> = listOf("", "", ""),
    val questionsLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resetSuccess: Boolean = false,
    val isFormValid: Boolean = false
)