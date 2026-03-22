package com.example.spendwiseai.data.repository

import com.example.spendwiseai.domain.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid
    private fun txCollection() = uid?.let {
        db.collection("users").document(it).collection("transactions")
    }

    suspend fun upsertTransaction(id: Long, amount: Double, currency: String,
        categoryName: String, description: String, dateMillis: Long, type: TransactionType) {
        val col = txCollection() ?: return
        val data = mapOf("id" to id, "amount" to amount, "currency" to currency,
            "categoryName" to categoryName, "description" to description,
            "dateMillis" to dateMillis, "type" to type.name)
        col.document(id.toString()).set(data, SetOptions.merge()).await()
    }

    suspend fun deleteTransaction(id: Long) {
        txCollection()?.document(id.toString())?.delete()?.await()
    }

    suspend fun fetchAllTransactions(): List<FirestoreTransaction> {
        val col = txCollection() ?: return emptyList()
        return col.get().await().documents.mapNotNull { doc ->
            try {
                FirestoreTransaction(
                    id = doc.getLong("id") ?: return@mapNotNull null,
                    amount = doc.getDouble("amount") ?: 0.0,
                    currency = doc.getString("currency") ?: "TL",
                    categoryName = doc.getString("categoryName") ?: "Other",
                    description = doc.getString("description") ?: "",
                    dateMillis = doc.getLong("dateMillis") ?: 0L,
                    type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE")
                )
            } catch (e: Exception) { null }
        }
    }
}

data class FirestoreTransaction(
    val id: Long, val amount: Double, val currency: String,
    val categoryName: String, val description: String,
    val dateMillis: Long, val type: TransactionType
)
