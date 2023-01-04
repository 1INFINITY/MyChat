package com.example.mychat.data.storage.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_USERS
import com.example.mychat.data.storage.StorageConstants.KEY_EMAIL
import com.example.mychat.data.storage.StorageConstants.KEY_FCM_TOKEN
import com.example.mychat.data.storage.StorageConstants.KEY_IMAGE
import com.example.mychat.data.storage.StorageConstants.KEY_NAME
import com.example.mychat.data.storage.StorageConstants.KEY_PASSWORD
import com.example.mychat.data.storage.StorageConstants.KEY_USER_ID
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FireBaseStorageImpl(private val firestoreDb: FirebaseFirestore) : FireBaseUserStorage {
    val TAG = "FIREBASE_STORAGE"

    override suspend fun userRegistration(user: User): User? {
        return suspendCoroutine {
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
                    val newUser = User(
                        id = documentReference.id,
                        name = user.name,
                        image = user.image,
                        email = user.email,
                        password = user.password
                    )
                    it.resume(newUser)
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "User add failure: ${e.message}")
                    it.resume(null)
                }
        }
    }

    override suspend fun checkUserExistAuthorization(authData: AuthData): Boolean {
        return suspendCoroutine {
            firestoreDb.collection(KEY_COLLECTION_USERS)
                .whereEqualTo(KEY_EMAIL, authData.email)
                .whereEqualTo(KEY_PASSWORD, authData.password)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
                        it.resume(true)
                    } else {
                        it.resume(false)
                    }
                }
        }

    }

    override suspend fun findUser(authData: AuthData): User? {
        return suspendCoroutine {
            firestoreDb.collection(KEY_COLLECTION_USERS)
                .whereEqualTo(KEY_EMAIL, authData.email)
                .whereEqualTo(KEY_PASSWORD, authData.password)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
                        val id = task.result.documents[0].id as String
                        val name = task.result.documents[0].get(KEY_NAME) as String
                        val imageStr = task.result.documents[0].get(KEY_IMAGE) as String
                        val email = task.result.documents[0].get(KEY_EMAIL) as String
                        val password = task.result.documents[0].get(KEY_PASSWORD) as String
                        val bytes = Base64.decode(imageStr, Base64.DEFAULT)
                        val image: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                        val user = User(
                            id = id,
                            image = image,
                            name = name,
                            email = email,
                            password = password
                        )
                        it.resume(user)
                    } else {
                        it.resume(null)
                    }
                }
        }
    }

    override suspend fun findUserById(userId: String): User? {
        return suspendCoroutine {
            firestoreDb.collection(KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result.exists()) {
                        val user = getUserFromSnapShot(task.result)
                        it.resume(user)
                    } else {
                        it.resume(null)
                    }
                }
        }
    }

    override suspend fun updateToken(userId: String) {
        val documentReference = firestoreDb.collection(KEY_COLLECTION_USERS).document(userId)
        val token = FirebaseMessaging.getInstance().token
        documentReference.update(KEY_FCM_TOKEN, token).addOnFailureListener() {
            task -> task.message
        }
    }

    override suspend fun deleteUserFieldById(userId: String, fieldName: String): Boolean {
        return suspendCoroutine {
            val documentReference = firestoreDb.collection(KEY_COLLECTION_USERS).document(userId)
            documentReference.update(fieldName, FieldValue.delete())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        it.resume(true)
                    } else {
                        it.resume(false)
                    }
                }
        }
    }

    override suspend fun getAllUsers(): List<User>? {
        return suspendCoroutine {
            val list: MutableList<User> = mutableListOf()
            val documentReference = firestoreDb.collection(KEY_COLLECTION_USERS)

            documentReference.get()
                .addOnSuccessListener { task ->
                    for (userSnapShot in task.documents) {
                        val user = getUserFromSnapShot(userSnapShot)
                        list.add(user)
                    }
                    it.resume(list.toList())
                }
                .addOnFailureListener { _ ->
                    it.resume(null)
                }
        }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth: Int = 150
        val previewHeight: Int = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val bao = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao)
        val bytes = bao.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun getUserFromSnapShot(snapshot: DocumentSnapshot): User {
        val id = snapshot.id as String
        val name = snapshot.get(KEY_NAME) as String
        val imageStr = snapshot.get(KEY_IMAGE) as String
        val email = snapshot.get(KEY_EMAIL) as String
        val password = snapshot.get(KEY_PASSWORD) as String
        val bytes = Base64.decode(imageStr, Base64.DEFAULT)
        val image: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        return User(
            id = id,
            image = image,
            name = name,
            email = email,
            password = password
        )
    }
}