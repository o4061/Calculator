package com.userfaltakas.calculator.data

data class CurrencyResponse(
    val base: String,
    val date: String,
    val rates: HashMap<String, Double>,
    val success: Boolean,
    val timestamp: Int
)