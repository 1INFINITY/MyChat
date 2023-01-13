package com.example.mychat.data.storage.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.nsd.NsdManager
import android.util.Base64
import android.util.Log
import com.example.mychat.data.storage.StorageConstants.KEY_CHAT_ID
import com.example.mychat.data.storage.StorageConstants.KEY_CHAT_NAME
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_CHAT
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_CHATS
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_USERS
import com.example.mychat.data.storage.StorageConstants.KEY_EMAIL
import com.example.mychat.data.storage.StorageConstants.KEY_FCM_TOKEN
import com.example.mychat.data.storage.StorageConstants.KEY_IMAGE
import com.example.mychat.data.storage.StorageConstants.KEY_LAST_MESSAGE
import com.example.mychat.data.storage.StorageConstants.KEY_MESSAGE
import com.example.mychat.data.storage.StorageConstants.KEY_NAME
import com.example.mychat.data.storage.StorageConstants.KEY_PASSWORD
import com.example.mychat.data.storage.StorageConstants.KEY_RECEIVER_ID
import com.example.mychat.data.storage.StorageConstants.KEY_SENDER_ID
import com.example.mychat.data.storage.StorageConstants.KEY_TIMESTAMP
import com.example.mychat.data.storage.StorageConstants.KEY_USERS_ID_ARRAY
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FireBaseStorageImpl(private val firestoreDb: FirebaseFirestore) : FireBaseUserStorage {
    val TAG = "FIREBASE_STORAGE"

    override suspend fun userRegistration(user: User, flow: FlowCollector<ResultData<User>>): User? {
        val authData = AuthData(email = user.email, password = user.password)
        val isUserExist = checkUserExistAuthorization(authData = authData)

        if (isUserExist) {
            flow.emit(ResultData.failure("User already exist"))
            return null
        }
        try {
            val userImageEncoded: String = encodeImage(user.image)
            val userHashMap = hashMapOf(
                KEY_IMAGE to userImageEncoded,
                KEY_NAME to user.name,
                KEY_EMAIL to user.email,
                KEY_PASSWORD to user.password,
            )
            val userRef: DocumentReference = firestoreDb.collection(KEY_COLLECTION_USERS).add(userHashMap).await()
            val newUser = User(
                id = userRef.id,
                name = user.name,
                image = user.image,
                email = user.email,
                password = user.password
            )
            flow.emit(ResultData.success(newUser))
            return newUser
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
            return null
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
        documentReference.update(KEY_FCM_TOKEN, token).addOnFailureListener() { task ->
            task.message
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

    override suspend fun sendMessage(chatMessage: ChatMessage): Boolean {
        return suspendCoroutine {
            val documentReference = firestoreDb.collection(KEY_COLLECTION_CHAT)
            val chatRef = firestoreDb.collection(KEY_COLLECTION_CHATS).document(chatMessage.chat.id)
            val senderRef = firestoreDb.collection(KEY_COLLECTION_USERS).document(chatMessage.sender.id)
            val messageHashMap = hashMapOf(
                KEY_CHAT_ID to chatRef,
                KEY_SENDER_ID to senderRef,
                KEY_MESSAGE to chatMessage.message,
                KEY_TIMESTAMP to chatMessage.date,
            )
            documentReference.add(messageHashMap)
                .addOnSuccessListener { doc ->
                    // Обновление последнего сообщения чата
                    chatRef.update(KEY_LAST_MESSAGE, chatMessage.message)

                    Log.d(TAG, "Message added with ID: ${doc.id}")
                    it.resume(true)
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Message add failure: ${e.message}")
                    it.resume(false)
                }
        }
    }

    override suspend fun fetchNewMessages(
        chat: Chat,
        flow: ProducerScope<ResultData<List<ChatMessage>>>,
    ) {
        val chatRef: DocumentReference =
            firestoreDb.collection(KEY_COLLECTION_CHATS).document(chat.id)
        val query: Query = firestoreDb
            .collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_CHAT_ID, chatRef)
        var chatRegistration: ListenerRegistration? = null
        try {
            chatRegistration = query.addSnapshotListener { value, error ->
                flow.launch {
                    if (error == null) {

                        val messageList: MutableList<ChatMessage> = mutableListOf()
                        value?.documentChanges?.map { doc ->
                            val message: String = doc.document.getString(KEY_MESSAGE)!!
                            val date: Date = doc.document.getDate(KEY_TIMESTAMP)!!

                            // Find User
                            val senderId: String = doc.document.getDocumentReference(KEY_SENDER_ID)!!.id
                            val senderSnapshot = firestoreDb.collection(KEY_COLLECTION_USERS).document(senderId).get().await()
                            val sender: User = getUserFromSnapShot(senderSnapshot)

                            val chatMessage = ChatMessage(
                                chat = chat,
                                sender = sender,
                                message = message,
                                date = date)
                            messageList.add(chatMessage)
                        }
                        flow.trySendBlocking(ResultData.success(messageList.toList()))
                    } else {
                        flow.trySendBlocking(ResultData.failure(error.localizedMessage))
                    }
                }
            }

        } catch (error: FirebaseFirestoreException) {
            flow.trySendBlocking(ResultData.failure(error.localizedMessage))
        } finally {
            flow.awaitClose {
                chatRegistration?.remove()
            }
        }

    }

    override suspend fun createNewChat(users: List<User>, flow: FlowCollector<ResultData<Chat>>) {
        try {
            val usersIds = users.map {
                firestoreDb.collection(KEY_COLLECTION_USERS).document(it.id)
            }
            val lastMessage = "У вас еще нет сообщений"
            val chatHashMap = hashMapOf(
                KEY_USERS_ID_ARRAY to usersIds,
                KEY_LAST_MESSAGE to lastMessage
            )
            val newChatId: String =
                firestoreDb.collection(KEY_COLLECTION_CHATS).add(chatHashMap).await().id
            val newChat = Chat(
                id = newChatId,
                name = null,
                users = users,
                lastMessage = lastMessage)
            flow.emit(ResultData.success(newChat))
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
        }
    }

    override suspend fun openChat(chat: Chat, flow: FlowCollector<ResultData<Chat>>) {
        try {
            val savedChatSnapshot: DocumentSnapshot = firestoreDb
                .collection(KEY_COLLECTION_CHATS)
                .document(chat.id)
                .get().await()

            val savedChat = Chat(
                id = savedChatSnapshot.id,
                name = savedChatSnapshot.getString(KEY_CHAT_NAME),
                users = chat.users,
                lastMessage = savedChatSnapshot.getString(KEY_LAST_MESSAGE)!!
            )
            flow.emit(ResultData.success(savedChat))
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
        }
    }

    override suspend fun fetchChats(
        user: User,
        flow: ProducerScope<ResultData<List<Chat>>>,
    ) {
        val docRef = firestoreDb.collection(KEY_COLLECTION_USERS).document(user.id)

        val registrationChat = firestoreDb.collection(KEY_COLLECTION_CHATS)
            .whereArrayContains(KEY_USERS_ID_ARRAY, docRef)
            .addSnapshotListener { value, error ->
                GlobalScope.launch {
                    if (error == null && !value!!.isEmpty) {
                        val chatList: MutableList<Chat> = mutableListOf()
                        value?.documentChanges?.map { doc ->
                            val chatId = doc.document.id as String
                            val chatName: String? = doc.document.getString(KEY_CHAT_NAME)
                            val lastMessage: String = doc.document.getString(KEY_LAST_MESSAGE)!!

                            val usersRefs =
                                (doc.document.get(KEY_USERS_ID_ARRAY) as ArrayList<DocumentReference>)
                            val tasks: List<Task<DocumentSnapshot>> = usersRefs.map { it.get() }
                            val allTask: Task<List<DocumentSnapshot>> = Tasks.whenAllSuccess(tasks)
                            val users = mutableListOf<User>()
                            val job = allTask.addOnSuccessListener {
                                for (documentSnapshot in it) {
                                    if (documentSnapshot != null) {
                                        val id = documentSnapshot.id as String
                                        val name = documentSnapshot.get(KEY_NAME) as String
                                        val imageStr = documentSnapshot.get(KEY_IMAGE) as String
                                        val email = documentSnapshot.get(KEY_EMAIL) as String
                                        val password =
                                            documentSnapshot.get(KEY_PASSWORD) as String
                                        val bytes = Base64.decode(imageStr, Base64.DEFAULT)
                                        val image: Bitmap =
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                        val user = User(
                                            id = id,
                                            image = image,
                                            name = name,
                                            email = email,
                                            password = password
                                        )
                                        users.add(user)
                                    }
                                }
                            }
                            job.await()
                            val chat = Chat(id = chatId,
                                name = chatName,
                                users = users,
                                lastMessage = lastMessage)
                            chatList.add(chat)
                        }
                        flow.trySendBlocking(ResultData.success(chatList.toList()))
                    } else {
                        flow.trySendBlocking(ResultData.failure(error.toString()))
                    }
                }
            }
        flow.awaitClose {
            registrationChat.remove()
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