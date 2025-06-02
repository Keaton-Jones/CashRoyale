package com.example.cashroyale.Services

import com.example.cashroyale.Models.Expense
import com.example.cashroyale.Models.Category
import com.example.cashroyale.Models.Income
import com.example.cashroyale.Models.User
import com.example.cashroyale.Models.MonthlyGoals
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.NoSuchElementException

class FireStore {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = Firebase.auth
    private val userCollection = db.collection("users")
    private val expenseCollection = db.collection("expenses")
    private val incomeCollection = db.collection("incomes")
    private val categoryCollection = db.collection("categories")
    private val monthlyGoalsCollection = db.collection("monthlyGoals")

    suspend fun saveUser(userName : String, password : String) : Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = User(userName, password)
            userCollection.document(userName).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}