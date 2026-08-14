package com.sparrow.laundrysys.classes

import java.util.Date

// User data class
data class User(
    val username: String = "",
    val name: String = "",
    val role: String = "",
    val salary: Double = 0.0,
    val dateJoined: Date = Date(),
    val profilePictureUrl: String = "",
    val recoveryQuestions: List<Map<String, String>> = emptyList(),
    val verified: Boolean = false,
    val biometricsEnabled: Boolean = false
)