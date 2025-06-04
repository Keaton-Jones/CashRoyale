package com.example.cashroyale.Services

import android.util.Log
import com.example.cashroyale.Models.Category
import com.example.cashroyale.Models.MonthlyGoals
import com.example.cashroyale.Models.Transactions // Correct model name and path
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // Needed for update/merge operations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await // For await() on suspend functions
import kotlinx.coroutines.withContext

class FireStore(private val db: FirebaseFirestore) {

    // --- Monthly Goals Functions ---

    // This function is generally not needed if you have getMonthlyGoalsFlow,
    // as Flow provides real-time updates. Keeping it for now if there's a specific one-time fetch need.
    suspend fun getMonthlyGoalByUserId(userId: String): MonthlyGoals? {
        return try {
            val querySnapshot = db.collection("monthlyGoals")
                .document(userId) // Directly get by document ID if userId is the document ID
                .get()
                .await()
            if (querySnapshot.exists()) {
                querySnapshot.toObject(MonthlyGoals::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FireStore", "Error getting monthly goal for user $userId: ${e.message}", e)
            null
        }
    }

    fun getMonthlyGoalsFlow(userId: String): Flow<MonthlyGoals?> = callbackFlow {
        val docRef = db.collection("monthlyGoals").document(userId)

        val subscription = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FireStore", "Listen failed for monthly goals for user $userId: ${e.message}", e)
                close(e) // Close the flow with the exception
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val goals = snapshot.toObject(MonthlyGoals::class.java)
                trySend(goals).isSuccess // Emit the updated goals
            } else {
                trySend(null).isSuccess // Emit null if document doesn't exist
            }
        }

        awaitClose { subscription.remove() } // Remove listener when flow is cancelled/closed
    }

    suspend fun saveMonthlyGoal(monthlyGoal: MonthlyGoals) {
        try {
            // Use userId as document ID. This is common for a single user's goals.
            db.collection("monthlyGoals").document(monthlyGoal.userId)
                .set(monthlyGoal, SetOptions.merge()) // Use SetOptions.merge() for partial updates
                .await()
            Log.d("FireStore", "Monthly goal saved successfully for user: ${monthlyGoal.userId}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving monthly goal: ${e.message}", e)
            throw e // Re-throw to allow ViewModel to handle it
        }
    }

    // --- Transactions Functions ---

    suspend fun saveTransaction(transaction: Transactions) {
        try {
            val docRef = if (transaction.id.isEmpty()) {
                db.collection("transactions").document() // Let Firestore generate new ID
            } else {
                db.collection("transactions").document(transaction.id) // Use existing ID for update
            }
            // Ensure the ID within the object matches the Firestore document ID
            docRef.set(transaction.copy(id = docRef.id)).await()
            Log.d("FireStore", "Transaction saved successfully: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving transaction: ${e.message}", e)
            throw e // Re-throw to allow ViewModel to handle
        }
    }

    fun getAllTransactions(userId: String): Flow<List<Transactions>> = callbackFlow {
        val transactionsCollection = db.collection("transactions")

        val listenerRegistration = transactionsCollection
            .whereEqualTo("userId", userId) // Filter by the specific user
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Listen failed for transactions: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transactions = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Transactions::class.java)?.apply {
                                this.id = doc.id // Crucially set the Firestore document ID
                            }
                        } catch (ex: Exception) {
                            Log.e("FireStore", "Error converting document ${doc.id} to Transactions: ${ex.message}", ex)
                            null
                        }
                    }
                    trySend(transactions).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }

        awaitClose { listenerRegistration.remove() } // Remove listener when flow is cancelled
    }

    // --- Category Functions (Corrected and New) ---

    /**
     * Gets all categories for a specific user as a real-time Flow.
     */
    fun getAllCategoriesFlow(userId: String): Flow<List<Category>> = callbackFlow {
        val categoriesCollection = db.collection("categories")

        val listenerRegistration = categoriesCollection
            .whereEqualTo("userId", userId) // Filter by user ID
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Listen failed for all categories for user $userId: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Category::class.java)?.apply {
                                this.id = doc.id // Crucially set the Firestore document ID
                            }
                        } catch (ex: Exception) {
                            Log.e("FireStore", "Error converting document ${doc.id} to Category: ${ex.message}", ex)
                            null
                        }
                    }
                    trySend(categories).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Gets categories by type (e.g., "income", "expense") for a specific user as a real-time Flow.
     */
    fun getCategoriesByTypeFlow(userId: String, type: String): Flow<List<Category>> = callbackFlow {
        val categoriesCollection = db.collection("categories")

        val listenerRegistration = categoriesCollection
            .whereEqualTo("userId", userId) // Filter by user ID
            .whereEqualTo("type", type) // Filter by category type
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Listen failed for categories by type ($type) for user $userId: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Category::class.java)?.apply {
                                this.id = doc.id
                            }
                        } catch (ex: Exception) {
                            Log.e("FireStore", "Error converting document ${doc.id} to Category: ${ex.message}", ex)
                            null
                        }
                    }
                    trySend(categories).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Gets a single category by its Firestore document ID and user ID as a real-time Flow.
     */
    suspend fun getCategoryById(categoryId: String, userId: String): Category? =
        withContext(Dispatchers.IO) {
            try {
                val documentSnapshot = db.collection("users")
                    .document(userId)
                    .collection("categories")
                    .document(categoryId)
                    .get()
                    .await() // Await the result of the Task

                documentSnapshot.toObject(Category::class.java) // Convert to Category object
            } catch (e: Exception) {
                Log.e("FireStore", "Error fetching category by ID: $categoryId for user $userId - ${e.message}", e)
                null
            }
        }


    /**
     * Updates an existing category in Firestore.
     * The Category object MUST have its 'id' field set to the Firestore document ID.
     */
    suspend fun updateCategory(category: Category) {
        try {
            db.collection("categories")
                .document(category.id) // Use the existing document ID
                .set(category, SetOptions.merge()) // Use merge to update only specified fields
                .await()
            Log.d("FireStore", "Category updated successfully: ${category.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error updating category ${category.id}: ${e.message}", e)
            throw e // Re-throw to allow ViewModel to handle
        }
    }

    /**
     * Checks if a category with the given name exists for a specific user.
     * This is a one-time check, not a real-time flow.
     * Returns true if a category with the name exists for the user, false otherwise.
     */
    suspend fun doesCategoryExist(categoryName: String, userId: String): Boolean {
        return try {
            val querySnapshot = db.collection("categories")
                .whereEqualTo("name", categoryName)
                .whereEqualTo("userId", userId)
                .limit(1) // Limit to 1 to check for existence efficiently
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.e("FireStore", "Error checking if category exists for user $userId, name $categoryName: ${e.message}", e)
            false // Assume it doesn't exist on error, or handle error specifically
        }
    }


    /**
     * Saves a new category to Firestore.
     * If category.id is empty, Firestore will generate a new document ID and set it back into the object.
     * If category.id is provided, it will overwrite the document with that ID.
     * This is a suspend function.
     */
    suspend fun saveCategory(category: Category) {
        try {
            val docRef = if (category.id.isEmpty()) {
                db.collection("categories").document() // Generate new ID
            } else {
                db.collection("categories").document(category.id) // Use provided ID
            }

            // Set the Firestore document ID back into the Category object's 'id' field
            // before saving. This ensures the object in your app always has the correct ID.
            docRef.set(category).await()
            Log.d("FireStore", "Category saved/added with ID: ${docRef.id} for user ${category.userId}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving category: ${e.message}", e)
            throw e // Re-throw to allow ViewModel to handle
        }
    }

    /**
     * Deletes a category from Firestore by its document ID.
     */
    suspend fun deleteCategory(categoryId: String) {
        try {
            db.collection("categories").document(categoryId).delete().await()
            Log.d("FireStore", "Category deleted: $categoryId")
        } catch (e: Exception) {
            Log.e("FireStore", "Error deleting category $categoryId: ${e.message}", e)
            throw e // Re-throw to allow ViewModel to handle
        }
    }
}