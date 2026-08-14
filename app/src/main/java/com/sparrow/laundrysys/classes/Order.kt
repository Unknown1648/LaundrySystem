package com.sparrow.laundrysys.classes

import com.google.firebase.Timestamp

data class Order(
    val id: String = "",
    val customerPhone: String = "",
    val customerName: String = "",
    val customerLocation: String = "",
    val status: String = "",
    val paid: Boolean = false,
    val totalPrice: Double = 0.0,
    val serviceItems: List<Map<String, Double>>? = null,
    val dateCreated: Timestamp? = null
)