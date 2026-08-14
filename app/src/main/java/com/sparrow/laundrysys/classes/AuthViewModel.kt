package com.sparrow.laundrysys.classes

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Date

class AuthViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    val currentUser = mutableStateOf<User?>(null)
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState
    private val _dialogState = MutableStateFlow<DialogState?>(null)

    // Update username and validate
    fun updateUsername(username: String) {
        _forgotPasswordState.update {
            it.copy(
                username = username,
                usernameError = if (username.isBlank()) "Username cannot be empty" else null
            )
        }
    }

    // Update new password and validate
    fun updateNewPassword(password: String) {
        _forgotPasswordState.update { state ->
            state.copy(
                newPassword = password,
                passwordError = validatePassword(password),
                confirmPasswordError = if (state.confirmPassword.isNotBlank() &&
                    password != state.confirmPassword) {
                    "Passwords do not match"
                } else null,
                isFormValid = validateForm(
                    username = state.username,
                    newPassword = password,
                    confirmPassword = state.confirmPassword,
                    answers = state.answers
                )
            )
        }
    }

    // Update confirm password and validate
    fun updateConfirmPassword(password: String) {
        _forgotPasswordState.update { state ->
            state.copy(
                confirmPassword = password,
                confirmPasswordError = if (password != state.newPassword) {
                    "Passwords do not match"
                } else null,
                isFormValid = validateForm(
                    username = state.username,
                    newPassword = state.newPassword,
                    confirmPassword = password,
                    answers = state.answers
                )
            )
        }
    }

    // Update security question answer
    fun updateAnswer(index: Int, answer: String) {
        _forgotPasswordState.update { state ->
            val newAnswers = state.answers.toMutableList()
            // Ensure the list has enough elements
            while (newAnswers.size <= index) {
                newAnswers.add("")
            }
            newAnswers[index] = answer

            state.copy(
                answers = newAnswers,
                isFormValid = validateForm(
                    username = state.username,
                    newPassword = state.newPassword,
                    confirmPassword = state.confirmPassword,
                    answers = newAnswers
                )
            )
        }
    }

    // Load security questions from Firestore
    fun loadSecurityQuestions() {
        viewModelScope.launch {
            try {
                _forgotPasswordState.update { it.copy(isLoading = true, errorMessage = null) }

                val db = FirebaseFirestore.getInstance()
                val username = _forgotPasswordState.value.username

                val userDoc = db.collection("users").document(username).get().await()

                if (userDoc.exists()) {
                    val userData = userDoc.data
                    val storedQuestions = userData?.get("recoveryQuestions") as? List<Map<String, String>> ?: emptyList()

                    if (storedQuestions.isEmpty()) {
                        _forgotPasswordState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "No recovery questions set for this user"
                            )
                        }
                    } else {
                        _forgotPasswordState.update {
                            it.copy(
                                isLoading = false,
                                questions = storedQuestions,
                                questionsLoaded = true
                            )
                        }
                    }
                } else {
                    _forgotPasswordState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _forgotPasswordState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    // Reset password with security questions
    fun resetPassword() {
        val state = _forgotPasswordState.value

        if (!state.isFormValid) {
            return
        }

        viewModelScope.launch {
            try {
                _forgotPasswordState.update { it.copy(isLoading = true, errorMessage = null) }

                val recoveryAnswers = mutableListOf<Map<String, String>>()
                for (i in state.questions.indices) {
                    if (i < 3 && state.answers.getOrNull(i)?.isNotBlank() == true) {
                        recoveryAnswers.add(mapOf(
                            "question" to (state.questions[i]["question"] ?: ""),
                            "answer" to state.answers[i]
                        ))
                    }
                }

                // Verify answers against stored questions and reset password
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("users").document(state.username).get().await()

                if (!userDoc.exists()) {
                    _forgotPasswordState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User not found"
                        )
                    }
                    return@launch
                }

                val userData = userDoc.data
                val storedQuestions = userData?.get("recoveryQuestions") as? List<Map<String, Any>> ?: emptyList()

                // Check if at least 2 answers match
                var correctAnswers = 0
                recoveryAnswers.forEach { recoveryAnswer ->
                    val question = recoveryAnswer["question"] ?: ""
                    val answer = recoveryAnswer["answer"] ?: ""

                    storedQuestions.forEach { storedQuestion ->
                        if ((storedQuestion["question"] as? String)?.equals(question, ignoreCase = true) == true &&
                            (storedQuestion["answer"] as? String)?.equals(answer, ignoreCase = true) == true) {
                            correctAnswers++
                        }
                    }
                }

                if (correctAnswers >= 2) {
                    // Update password in the database
                    val passwordHash = hashPassword(state.newPassword)
                    db.collection("users").document(state.username)
                        .update("password", passwordHash).await()

                    _forgotPasswordState.update {
                        it.copy(
                            isLoading = false,
                            resetSuccess = true
                        )
                    }
                } else {
                    _forgotPasswordState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Not enough correct answers. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                _forgotPasswordState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An error occurred while resetting password"
                    )
                }
            }
        }
    }

    // Validate password strength
    private fun validatePassword(password: String): String? {
        return when {
            password.length < 8 -> "Password must be at least 8 characters"
            !password.matches(Regex(".*[A-Z].*")) -> "Password must contain at least one uppercase letter"
            !password.matches(Regex(".*[a-z].*")) -> "Password must contain at least one lowercase letter"
            !password.matches(Regex(".*[0-9].*")) -> "Password must contain at least one number"
            else -> null
        }
    }

    // Validate entire form
    private fun validateForm(
        username: String,
        newPassword: String,
        confirmPassword: String,
        answers: List<String>
    ): Boolean {
        return username.isNotBlank() &&
                newPassword.isNotBlank() &&
                newPassword == confirmPassword &&
                validatePassword(newPassword) == null &&
                answers.count { it.isNotBlank() } >= 2
    }

    // Show confirmation dialog
    fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        _dialogState.value = DialogState(
            title = title,
            message = message,
            onConfirm = onConfirm,
            onDismiss = { dismissDialog() }
        )
    }

    // Dismiss dialog
    private fun dismissDialog() {
        _dialogState.value = null
    }

    // Clear the forgot password state
    fun clearForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }

    // Hash password using SHA-256
    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    // Save user data to SharedPreferences
    private fun saveUserToPrefs(context: Context, user: User) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("username", user.username)
            putString("name", user.name)
            putString("role", user.role)
            putFloat("salary", user.salary.toFloat())
            putLong("dateJoined", user.dateJoined.time)
            putString("profilePictureUrl", user.profilePictureUrl)
            putBoolean("verified", user.verified)
            putBoolean("biometricsEnabled", user.biometricsEnabled)
            apply()
        }
    }

    // Load user data from SharedPreferences
    fun loadUserFromPrefs(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val username = sharedPrefs.getString("username", null)

        if (username != null) {
            val user = User(
                username = username,
                name = sharedPrefs.getString("name", "") ?: "",
                role = sharedPrefs.getString("role", "") ?: "",
                salary = sharedPrefs.getFloat("salary", 0f).toDouble(),
                dateJoined = Date(sharedPrefs.getLong("dateJoined", 0)),
                profilePictureUrl = sharedPrefs.getString("profilePictureUrl", "") ?: "",
                verified = sharedPrefs.getBoolean("verified", false),
                biometricsEnabled = sharedPrefs.getBoolean("biometricsEnabled", false)
            )
            currentUser.value = user
        }
    }

    fun checkIfAdminExists(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val result = usersCollection.limit(1).get().await()
                onResult(!result.isEmpty)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error checking admin: ${e.message}")
                onResult(false)
            } finally {
                isLoading.value = false
            }
        }
    }


    // Clear user data from SharedPreferences
    fun logout(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            clear()
            apply()
        }
        currentUser.value = null
    }

    // Register a new user
    fun register(
        context: Context,
        username: String,
        password: String,
        name: String,
        role: String,
        salary: Double,
        profilePictureUrl: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                // Check if username already exists
                val existingUser = usersCollection.whereEqualTo("username", username).get().await()
                if (!existingUser.isEmpty) {
                    onComplete(false, "Username already exists")
                    return@launch
                }

                // Create new user
                val hashedPassword = hashPassword(password)
                val user = hashMapOf(
                    "username" to username,
                    "password" to hashedPassword,
                    "name" to name,
                    "role" to role,
                    "salary" to salary,
                    "dateJoined" to Date(),
                    "profilePictureUrl" to profilePictureUrl,
                    "recoveryQuestions" to emptyList<Map<String, String>>(),
                    "verified" to false,
                    "biometricsEnabled" to false
                )

                // Save to Firestore
                usersCollection.document(username).set(user).await()

                // Create user object
                val newUser = User(
                    username = username,
                    name = name,
                    role = role,
                    salary = salary,
                    dateJoined = Date(),
                    profilePictureUrl = profilePictureUrl
                )

                // Save to SharedPreferences
                saveUserToPrefs(context, newUser)
                currentUser.value = newUser

                onComplete(true, null)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error: ${e.message}")
                onComplete(false, e.message)
            } finally {
                isLoading.value = false
            }
        }
    }

    // Login user
    fun login(
        context: Context,
        username: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val hashedPassword = hashPassword(password)
                val userDoc = usersCollection.document(username).get().await()

                if (!userDoc.exists()) {
                    onComplete(false, "User not found")
                    return@launch
                }

                val userData = userDoc.data
                val storedPassword = userData?.get("password") as? String

                if (storedPassword != hashedPassword) {
                    onComplete(false, "Invalid password")
                    return@launch
                }

                // Create user object
                val user = User(
                    username = username,
                    name = userData["name"] as? String ?: "",
                    role = userData["role"] as? String ?: "",
                    salary = (userData["salary"] as? Number)?.toDouble() ?: 0.0,
                    dateJoined = (userData["dateJoined"] as? Timestamp)?.toDate() ?: Date(),
                    profilePictureUrl = userData["profilePictureUrl"] as? String ?: "",
                    recoveryQuestions = userData["recoveryQuestions"] as? List<Map<String, String>> ?: emptyList(),
                    verified = userData["verified"] as? Boolean ?: false,
                    biometricsEnabled = userData["biometricsEnabled"] as? Boolean ?: false
                )

                // Save to SharedPreferences
                saveUserToPrefs(context, user)
                currentUser.value = user

                onComplete(true, null)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error: ${e.message}")
                onComplete(false, e.message)
            } finally {
                isLoading.value = false
            }
        }
    }

    // Set recovery questions
    fun setRecoveryQuestions(
        username: String,
        questions: List<Map<String, String>>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                // Update Firestore
                usersCollection.document(username).update(
                    mapOf(
                        "recoveryQuestions" to questions,
                        "verified" to true
                    )
                ).await()

                // Update current user
                currentUser.value = currentUser.value?.copy(
                    recoveryQuestions = questions,
                    verified = true
                )

                onComplete(true, null)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Set recovery questions error: ${e.message}")
                onComplete(false, e.message)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(
        imageUri: Uri,
        context: Context,
        callback: (String, Boolean) -> Unit
    ) {
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val imageUrl = CloudinaryManager.uploadImage(imageUri, context)
                if (imageUrl != null) {
                    isLoading.value = false
                    callback(imageUrl, true)
                } else {
                    throw Exception("Failed to upload image")
                }
            } catch (e: Exception) {
                isLoading.value = false
                errorMessage.value = "Image upload failed: ${e.message}"
                callback("", false)
            }
        }
    }

    // Toggle biometrics
    fun toggleBiometrics(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val username = currentUser.value?.username ?: return@launch

                // Update Firestore
                usersCollection.document(username).update("biometricsEnabled", enabled).await()

                // Update current user
                currentUser.value = currentUser.value?.copy(biometricsEnabled = enabled)

                // Update SharedPreferences
                val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putBoolean("biometricsEnabled", enabled)
                    apply()
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Toggle biometrics error: ${e.message}")
                errorMessage.value = e.message
            }
        }
    }
}
