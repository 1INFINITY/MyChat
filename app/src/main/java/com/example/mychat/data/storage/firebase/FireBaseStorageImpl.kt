package com.example.mychat.data.storage.firebase

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.mychat.data.storage.firebase.FirebaseConstants.KEY_COLLECTION_USERS
import com.example.mychat.data.storage.firebase.FirebaseConstants.KEY_EMAIL
import com.example.mychat.data.storage.firebase.FirebaseConstants.KEY_IMAGE
import com.example.mychat.data.storage.firebase.FirebaseConstants.KEY_NAME
import com.example.mychat.data.storage.firebase.FirebaseConstants.KEY_PASSWORD
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FireBaseStorageImpl(private val firestoreDb: FirebaseFirestore): FireBaseUserStorage {
    val TAG = "FIREBASE_STORAGE"

    override fun userRegistration(user: User) {
        val userImageEncoded: String = encodeImage(user.image)

        val userHashMap = hashMapOf(
            KEY_IMAGE to userImageEncoded,
            KEY_NAME to user.name,
            KEY_EMAIL to user.email,
            KEY_PASSWORD to user.password,
        )
        firestoreDb.collection(KEY_COLLECTION_USERS)
            .add(userHashMap)
            .addOnSuccessListener { documentReference ->
               Log.d(TAG, "User added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "User add failure: ${e.message}")
            }
    }

    override suspend fun checkUserExistAuthorization(authData: AuthData): Boolean {
        return suspendCoroutine {
            firestoreDb.collection(KEY_COLLECTION_USERS)
                .whereEqualTo(KEY_EMAIL, authData.email)
                .whereEqualTo(KEY_PASSWORD, authData.password)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null && task.result.documents.size > 0){
                        it.resume(true)
                    }
                    else {
                        it.resume(false)
                    }
                }
        }

    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth: Int = 150
        val previewHeight: Int = bitmap.height * previewWidth/ bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val bao = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao)
        val bytes = bao.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}