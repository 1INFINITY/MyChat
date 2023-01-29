package com.example.mychat.data.storage.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.mychat.data.mapping.EncodedImageMapper
import com.example.mychat.data.mapping.ImageMapper
import com.example.mychat.data.mapping.UserMapper
import com.example.mychat.data.mapping.UserRegisteredMapper
import com.example.mychat.data.models.UserFirestore
import com.example.mychat.data.storage.StorageConstants.KEY_CHAT_ID
import com.example.mychat.data.storage.StorageConstants.KEY_CHAT_NAME
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_CHAT
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_CHATS
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_USERS
import com.example.mychat.data.storage.StorageConstants.KEY_DELETED
import com.example.mychat.data.storage.StorageConstants.KEY_EMAIL
import com.example.mychat.data.storage.StorageConstants.KEY_FCM_TOKEN
import com.example.mychat.data.storage.StorageConstants.KEY_IMAGE
import com.example.mychat.data.storage.StorageConstants.KEY_LAST_MESSAGE
import com.example.mychat.data.storage.StorageConstants.KEY_MESSAGE
import com.example.mychat.data.storage.StorageConstants.KEY_NAME
import com.example.mychat.data.storage.StorageConstants.KEY_PASSWORD
import com.example.mychat.data.storage.StorageConstants.KEY_SENDER_ID
import com.example.mychat.data.storage.StorageConstants.KEY_TIMESTAMP
import com.example.mychat.data.storage.StorageConstants.KEY_USERS_ID_ARRAY
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
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

    override suspend fun userRegistration(
        user: User,
        flow: FlowCollector<ResultData<User>>,
    ): User? {
        val authData = AuthData(email = user.email, password = user.password)
        val isUserExist = checkUserExistAuthorization(authData = authData)

        if (isUserExist) {
            flow.emit(ResultData.failure("User already exist"))
            return null
        }
        try {
            val userRegisteredFirestore = UserRegisteredMapper(EncodedImageMapper()).transform(user)
            val userRef: DocumentReference =
                firestoreDb.collection(KEY_COLLECTION_USERS).add(userRegisteredFirestore).await()
            val newUser = user.copy(id = userRef.id)
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

    override suspend fun userAuthorization(
        authData: AuthData,
        flow: FlowCollector<ResultData<User>>,
    ): User? {
        val user: User? = findUser(authData)

        if (user != null) {
            updateToken(userId = user.id)
            flow.emit(ResultData.success(user))
            return user
        } else {
            flow.emit(ResultData.failure("Something goes wrong"))
            return null
        }
    }

    override suspend fun updateToken(userId: String) {
        val documentReference = firestoreDb.collection(KEY_COLLECTION_USERS).document(userId)
        val token: String = FirebaseMessaging.getInstance().token.await()
        documentReference.update(KEY_FCM_TOKEN, token)
    }

    override suspend fun signOutUser(
        user: User,
        flow: FlowCollector<ResultData<Boolean>>,
    ): Boolean {
        val usersRef = firestoreDb.collection(KEY_COLLECTION_USERS)
        try {
            usersRef.document(user.id).update(KEY_FCM_TOKEN, FieldValue.delete()).await()
            flow.emit(ResultData.success(true))
            return true
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
            return false
        }
    }

    override suspend fun getAllUsers(flow: FlowCollector<ResultData<List<User>>>): List<User>? {
        val usersRef = firestoreDb.collection(KEY_COLLECTION_USERS)

        try {
            val users: List<User> = usersRef.get().await().map {
                val userFirestore: UserFirestore = it.toObject(UserFirestore::class.java)!!
                return@map UserMapper(ImageMapper()).transform(userFirestore)
            }
            flow.emit(ResultData.success(users))
            return users
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
            return null
        }
    }

    override suspend fun sendMessage(
        chatMessage: ChatMessage,
        flow: FlowCollector<ResultData<Boolean>>,
    ): Boolean {
        val documentReference = firestoreDb.collection(KEY_COLLECTION_CHAT)
        val chatRef = firestoreDb.collection(KEY_COLLECTION_CHATS).document(chatMessage.chat.id)
        val senderRef =
            firestoreDb.collection(KEY_COLLECTION_USERS).document(chatMessage.sender.id)
        val messageHashMap = hashMapOf(
            KEY_CHAT_ID to chatRef,
            KEY_SENDER_ID to senderRef,
            KEY_MESSAGE to chatMessage.message,
            KEY_TIMESTAMP to chatMessage.date,
        )

        try {
            documentReference.add(messageHashMap).await()
            chatRef.update(KEY_LAST_MESSAGE, chatMessage.message)
            flow.emit(ResultData.success(true))
            return true
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
            return false
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
        var isAdded: Boolean = false
        var isUpdate: Boolean = false
        var isRemoved: Boolean = false
        var chatRegistration: ListenerRegistration? = null
        try {
            chatRegistration = query.addSnapshotListener { value, error ->
                flow.launch {
                    if (error == null) {

                        val messageList: MutableList<ChatMessage> = mutableListOf()
                        value?.documentChanges?.map { doc ->
                            isAdded = doc.type == DocumentChange.Type.ADDED
                            isUpdate = doc.type == DocumentChange.Type.MODIFIED
                            isRemoved = doc.document.getBoolean(KEY_DELETED) ?: false

                            if (isAdded && !isRemoved || isUpdate) {
                                val message: String = doc.document.getString(KEY_MESSAGE)!!
                                val date: Date = doc.document.getDate(KEY_TIMESTAMP)!!
                                val senderId: String =
                                    doc.document.getDocumentReference(KEY_SENDER_ID)!!.id
                                val senderSnapshot =
                                    firestoreDb.collection(KEY_COLLECTION_USERS).document(senderId)
                                        .get().await()
                                val userFirestore: UserFirestore =
                                    senderSnapshot.toObject(UserFirestore::class.java)!!
                                val sender: User =
                                    UserMapper(ImageMapper()).transform(userFirestore)

                                val chatMessage = ChatMessage(
                                    id = doc.document.id,
                                    chat = chat,
                                    sender = sender,
                                    message = message,
                                    date = date)
                                messageList.add(chatMessage)
                            }
                        }
                        if (isAdded)
                            flow.trySendBlocking(ResultData.success(messageList.toList()))
                        else
                            if (isRemoved)
                                flow.trySendBlocking(ResultData.removed(messageList.toList()))
                            else
                                flow.trySendBlocking(ResultData.update(messageList.toList()))
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

    override suspend fun createNewChat(
        userSender: User,
        users: List<User>,
        flow: FlowCollector<ResultData<Chat>>,
    ) {
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
                userReceiver = users.find { it.id != userSender.id }!!,
                lastMessage = lastMessage)
            flow.emit(ResultData.success(newChat))
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
        }
    }

    override suspend fun openChat(
        user: User,
        chatId: String,
        flow: FlowCollector<ResultData<Chat>>,
    ) {
        try {
            val savedChatSnapshot: DocumentSnapshot = firestoreDb
                .collection(KEY_COLLECTION_CHATS)
                .document(chatId)
                .get().await()

            val userReceiverRef =
                (savedChatSnapshot.get(KEY_USERS_ID_ARRAY) as ArrayList<DocumentReference>).find { it.id != user.id }
            val receiverSnapshot = userReceiverRef!!.get().await()
            val userFirestore: UserFirestore =
                receiverSnapshot.toObject(UserFirestore::class.java)!!
            val userReceiver: User = UserMapper(ImageMapper()).transform(userFirestore)

            val savedChat = Chat(
                id = savedChatSnapshot.id,
                name = savedChatSnapshot.getString(KEY_CHAT_NAME),
                userReceiver = userReceiver,
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

                            val userReceiverRef =
                                (doc.document.get(KEY_USERS_ID_ARRAY) as ArrayList<DocumentReference>).find { it.id != user.id }
                            val receiverSnapshot = userReceiverRef!!.get().await()
                            val userFirestore: UserFirestore =
                                receiverSnapshot.toObject(UserFirestore::class.java)!!
                            val userReceiver: User =
                                UserMapper(ImageMapper()).transform(userFirestore)

                            val chat = Chat(id = chatId,
                                name = chatName,
                                userReceiver = userReceiver,
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

    override suspend fun changeMessage(
        chatMessage: ChatMessage,
        flow: FlowCollector<ResultData<ChatMessage>>,
    ) {
        val messageRef = firestoreDb.collection(KEY_COLLECTION_CHAT).document(chatMessage.id)
        try {
            messageRef.update(KEY_MESSAGE, chatMessage.message).await()
            flow.emit(ResultData.success(chatMessage))
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
        }
    }

    override suspend fun deleteMessage(
        chatMessage: ChatMessage,
        flow: FlowCollector<ResultData<ChatMessage>>,
    ) {
        val messageRef = firestoreDb.collection(KEY_COLLECTION_CHAT).document(chatMessage.id)
        try {
            messageRef.update(KEY_DELETED, true).await()
            flow.emit(ResultData.success(chatMessage))
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
        }
    }
}