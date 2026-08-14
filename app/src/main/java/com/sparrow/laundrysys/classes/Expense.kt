package com.sparrow.laundrysys.classes

import com.google.firebase.Timestamp

data class Expense(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Timestamp? = Timestamp.now(),
    val notes: String = ""
)