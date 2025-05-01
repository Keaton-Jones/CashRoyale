package com.example.cashroyale

data class Expense(
    val description: String, //category or descrip
    val amount: Double,  // how muh it cost
    val date: String,    // date of purchase
    val imageUri: String? = null     // if there is a pic attached
)
