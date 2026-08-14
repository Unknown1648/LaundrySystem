package com.sparrow.laundrysys.utilities

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.sparrow.laundrysys.classes.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun changePassword(
    newPassword: String,
    authViewModel: AuthViewModel,
    context: Context,
    onComplete: (Boolean) -> Unit
) {
    try {
        val currentUser = authViewModel.currentUser

        withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(currentUser.value!!.username)
            val updates = hashMapOf(
                "password" to authViewModel.hashPassword(newPassword)
            )

            userRef.update(updates as Map<String, Any>).await()
        }

        Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
        onComplete(true)
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }
}