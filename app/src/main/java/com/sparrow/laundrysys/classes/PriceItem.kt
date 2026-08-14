package com.sparrow.laundrysys.classes

data class PriceItem(
    val id: String = "",
    val item: String = "",
    val uom: String = "",
    val price: Double = 0.0
)