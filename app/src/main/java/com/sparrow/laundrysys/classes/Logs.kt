package com.sparrow.laundrysys.classes

import com.google.firebase.Timestamp

data class Logs(
    val id: String = "",
    val actionType: String = "",
    val changedBy: String = "",
    val orderId: String = "",
    val oldValues: List<Map<String, Any>>? = null,
    val newValues: List<Map<String, Any>>? = null,
    val timestamp: Timestamp? = null
)