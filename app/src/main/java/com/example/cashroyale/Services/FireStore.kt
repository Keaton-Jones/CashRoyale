package com.example.cashroyale.Services

import android.util.Log
import com.example.cashroyale.Models.Category
import com.example.cashroyale.Models.Expense
import com.example.cashroyale.Models.Income
import com.example.cashroyale.Models.MonthlyGoals
import com.example.cashroyale.Models.Transactions
import com.example.cashroyale.Models.Transactions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query // Explicit import for Query.Direction
import java.util.Locale

class FireStore(private val db: FirebaseFirestore) {

    // Standardized CATEGORY_COLLECTION_NAME to "categories" (plural) based on your Firestore screenshot
    private val CATEGORY_COLLECTION_NAME = "categories" //

    // --- Category Management ---

    /**
     * Saves or updates a Category document in Firestore.
     * If category.id is empty, a new document ID is generated.
     * The Category object includes its limit.
     */
    suspend fun saveOrUpdateCategory(category: Category) {
        val documentId = category.id.ifEmpty { db.collection(CATEGORY_COLLECTION_NAME).document().id }
        val categoryToSave = category.copy(id = documentId)

        val documentRef = db.collection(CATEGORY_COLLECTION_NAME)
            .document(categoryToSave.id)

        Log.d("FireStore", "saveOrUpdateCategory: Attempting to save/update category: ${categoryToSave.name} (ID: ${categoryToSave.id}) to path: $CATEGORY_COLLECTION_NAME/${categoryToSave.id}")
        Log.d("FireStore", "saveOrUpdateCategory: Data being saved: $categoryToSave")

        try {
            // Using SetOptions.merge() is crucial for updates to only modify specified fields
            documentRef.set(categoryToSave, SetOptions.merge()).await()
            Log.d("FireStore", "saveOrUpdateCategory: Category '${categoryToSave.name}' saved/updated successfully.")
        } catch (e: Exception) {
            Log.e("FireStore", "saveOrUpdateCategory: ERROR saving/updating category '${categoryToSave.name}': ${e.message}", e)
            throw e
        }
    }

    /**
     * Provides a real-time flow of all categories for a specific user,
     * including their name, image, and limit.
     */
    fun getUserCategoriesFlow(userId: String): Flow<List<Category>> = callbackFlow {
        val listenerRegistration = db.collection(CATEGORY_COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Error fetching user categories: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                val categoryList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Category::class.java)?.apply { id = doc.id }
                    } catch (mappingError: Exception) {
                        Log.e("FireStore", "Error mapping document ${doc.id} to Category: ${mappingError.message}, Data: ${doc.data}", mappingError)
                        null
                    }
                } ?: emptyList()
                trySend(categoryList).isSuccess
                Log.d("FireStore", "User categories fetched: ${categoryList.size} documents. First: ${categoryList.firstOrNull()}")
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Original `saveCategory` function - kept as per request.
     * Note: `saveOrUpdateCategory` is generally preferred as it handles both new and existing categories.
     */
    suspend fun saveCategory(category: Category) {
        try {
            val docRef = if (category.id.isEmpty()) {
                db.collection(CATEGORY_COLLECTION_NAME).document()
            } else {
                db.collection(CATEGORY_COLLECTION_NAME).document(category.id)
            }
            docRef.set(category).await() // Use set to overwrite or create
            Log.d("FireStore", "Category saved (via saveCategory): ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving category (via saveCategory): ${e.message}", e)
            throw e
        }
    }

    /**
     * Original `getAllCategoriesFlow` function - kept as per request.
     * Note: `getUserCategoriesFlow` provides similar functionality for the current user.
     */
    fun getAllCategoriesFlow(userId: String): Flow<List<Category>> = callbackFlow {
        val listenerRegistration = db.collection(CATEGORY_COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(categories).isSuccess
                Log.d("FireStore", "All categories flow updated: ${categories.size} documents.")
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Original `getCategoriesByTypeFlow` function - kept as per request.
     */
    fun getCategoriesByTypeFlow(userId: String, type: String): Flow<List<Category>> = callbackFlow {
        val listenerRegistration = db.collection(CATEGORY_COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type) // Assumes 'type' field exists in your Category model
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(categories).isSuccess
                Log.d("FireStore", "Categories by type ($type) flow updated: ${categories.size} documents.")
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Original `getCategoryById` function - path corrected to match `CATEGORY_COLLECTION_NAME`.
     * This now fetches from the top-level 'categories' collection.
     */
    suspend fun getCategoryById(categoryId: String, userId: String): Category? = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection(CATEGORY_COLLECTION_NAME)
                .document(categoryId).get().await()
            // Ensure the fetched category actually belongs to the user requesting it
            if (snapshot.exists() && snapshot.getString("userId") == userId) {
                snapshot.toObject(Category::class.java)?.apply { id = snapshot.id }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FireStore", "Error fetching category by ID: ${e.message}", e)
            null
        }
    }

    /**
     * Original `updateCategory` function - collection name corrected.
     */
    suspend fun updateCategory(category: Category) {
        try {
            db.collection(CATEGORY_COLLECTION_NAME).document(category.id)
                .set(category, SetOptions.merge()).await()
            Log.d("FireStore", "Category updated: ${category.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error updating category: ${e.message}", e)
            throw e
        }
    }

    /**
     * Original `doesCategoryExist` function - collection name corrected.
     */
    suspend fun doesCategoryExist(categoryName: String, userId: String): Boolean {
        return try {
            val snapshot = db.collection(CATEGORY_COLLECTION_NAME)
                .whereEqualTo("name", categoryName)
                .whereEqualTo("userId", userId)
                .limit(1).get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e("FireStore", "Error checking category existence: ${e.message}", e)
            false
        }
    }

    /**
     * Original `deleteCategory` function - collection name corrected.
     */
    suspend fun deleteCategory(categoryId: String) {
        try {
            db.collection(CATEGORY_COLLECTION_NAME).document(categoryId).delete().await()
            Log.d("FireStore", "Category deleted: $categoryId")
        } catch (e: Exception) {
            Log.e("FireStore", "Error deleting category: ${e.message}", e)
            throw e
        }
    }


    // --- Monthly Goals ---

    /**
     * Original `getMonthlyGoalByUserId` function - kept as per request.
     * Note: `getMonthlyGoalsFlow` is preferred for real-time updates.
     */
    suspend fun getMonthlyGoalByUserId(userId: String): MonthlyGoals? {
        return try {
            val querySnapshot = db.collection("monthlyGoals")
                .document(userId)
                .document(userId)
                .get()
                .await()
            if (querySnapshot.exists()) {
                querySnapshot.toObject(MonthlyGoals::class.java)
            } else null
        } catch (e: Exception) {
            Log.e("FireStore", "Error getting monthly goal: ${e.message}", e)
            null
        }
    }

    fun getMonthlyGoalsFlow(userId: String): Flow<MonthlyGoals?> = callbackFlow {
        val docRef = db.collection("monthlyGoals").document(userId)
        val subscription = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(MonthlyGoals::class.java)).isSuccess
            Log.d("FireStore", "Monthly goals flow update: ${snapshot?.toObject(MonthlyGoals::class.java)}")
        }
        awaitClose { subscription.remove() }
    }

    suspend fun saveMonthlyGoal(monthlyGoal: MonthlyGoals) {
        try {
            db.collection("monthlyGoals").document(monthlyGoal.userId)
                .set(monthlyGoal, SetOptions.merge()).await()
            Log.d("FireStore", "Monthly goal saved for user ${monthlyGoal.userId}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving monthly goal: ${e.message}", e)
            throw e
        }
    }

    // --- Transactions ---

    // This function for expenses was already using string dates, no change needed.
    fun getMonthlyExpensesFlow(userId: String, startDate: String, endDate: String): Flow<List<Expense>> = callbackFlow {
        val listenerRegistration = db.collection("expenses")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Error fetching expenses: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                val expenseList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val expense = doc.toObject(Expense::class.java)?.apply { id = doc.id }
                        if (expense == null) {
                            Log.w("FireStore", "Failed to map document ${doc.id} to Expense object. Data: ${doc.data}")
                        }
                        expense
                    } catch (mappingError: Exception) {
                        Log.e("FireStore", "Error mapping document ${doc.id} to Expense: ${mappingError.message}, Data: ${doc.data}", mappingError)
                        null
                    }
                } ?: emptyList()
                trySend(expenseList).isSuccess
                Log.d("FireStore", "Monthly expenses fetched: ${expenseList.size} documents.")
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun saveTransaction(transaction: Transactions) {
        try {
            val docRef = if (transaction.id.isEmpty()) {
                db.collection("transactions").document()
                db.collection("transactions").document()
            } else {
                db.collection("transactions").document(transaction.id)
                db.collection("transactions").document(transaction.id)
            }
            docRef.set(transaction.copy(id = docRef.id)).await()
            Log.d("FireStore", "Transaction saved with ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving transaction: ${e.message}", e)
            throw e
            throw e
        }
    }

    fun getAllTransactions(userId: String): Flow<List<Transactions>> = callbackFlow {
        val listenerRegistration = db.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transactions::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(transactions).isSuccess
                Log.d("FireStore", "All transactions fetched: ${transactions.size} documents.")
            }

        awaitClose { listenerRegistration.remove() }
    }

    // --- Income specific functions ---

    suspend fun saveIncome(income: Income) {
        try {
            val docRef = if (income.id.isEmpty()) {
                db.collection("income").document()
            } else {
                db.collection("income").document(income.id)
            }

            docRef.set(income.copy(id = docRef.id)).await()
            Log.d("FireStore", "Income saved with ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving income: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateIncome(income: Income) {
        try {
            db.collection("income").document(income.id)
                .set(income, SetOptions.merge()).await()
            Log.d("FireStore", "Income updated: ${income.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error updating income: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteIncome(incomeId: String) {
        try {
            db.collection("income").document(incomeId).delete().await()
            Log.d("FireStore", "Income deleted: $incomeId")
        } catch (e: Exception) {
            Log.e("FireStore", "Error deleting income: ${e.message}", e)
            throw e
        }
    }

    fun getAllIncomeFlow(userId: String): Flow<List<Income>> = callbackFlow {
        val listenerRegistration = db.collection("income")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val incomeList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Income::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(incomeList).isSuccess
                Log.d("FireStore", "All income fetched: ${incomeList.size} documents.")
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getIncomeById(incomeId: String): Income? {
        return try {
            val doc = db.collection("income").document(incomeId).get().await()
            if (doc.exists()) {
                doc.toObject(Income::class.java)?.apply { id = doc.id }
            } else null
        } catch (e: Exception) {
            Log.e("FireStore", "Error getting income: ${e.message}", e)
            null
        }
    }

    // --- Expense specific functions ---

    suspend fun saveExpense(expense: Expense) {
        try {
            val docRef = if (expense.id.isEmpty()) {
                db.collection("expenses").document()
            } else {
                db.collection("expenses").document(expense.id)
            }

            docRef.set(expense.copy(id = docRef.id)).await()
            Log.d("FireStore", "Expense saved with ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateExpense(expense: Expense) {
        try {
            db.collection("expenses").document(expense.id)
                .set(expense, SetOptions.merge()).await()
            Log.d("FireStore", "Expense updated: ${expense.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error updating expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteExpense(expenseId: String) {
        try {
            db.collection("expenses").document(expenseId).delete().await()
            Log.d("FireStore", "Expense deleted: $expenseId")
        } catch (e: Exception) {
            Log.e("FireStore", "Error deleting expense: ${e.message}", e)
            throw e
        }
    }

    fun getAllExpensesFlow(userId: String): Flow<List<Expense>> = callbackFlow {
        val listenerRegistration = db.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val expenseList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(expenseList).isSuccess
                Log.d("FireStore", "All expenses fetched: ${expenseList.size} documents.")
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getExpenseById(expenseId: String): Expense? {
        return try {
            val doc = db.collection("expenses").document(expenseId).get().await()
            if (doc.exists()) {
                doc.toObject(Expense::class.java)?.apply { id = doc.id }
            } else null
        } catch (e: Exception) {
            Log.e("FireStore", "Error getting expense: ${e.message}", e)
            null
        }
    }
}