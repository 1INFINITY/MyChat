package com.example.mychat.data.storage.firebase

import com.example.mychat.data.mapping.EncodedImageMapper
import com.example.mychat.data.mapping.ImageMapper
import com.example.mychat.data.mapping.UserMapper
import com.example.mychat.data.mapping.UserRegisteredMapper
import com.example.mychat.data.models.ChatFirestore
import com.example.mychat.data.models.ChatMessageFirestore
import com.example.mychat.data.models.UserFirestore
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_CHATS
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_MESSAGES
import com.example.mychat.data.storage.StorageConstants.KEY_COLLECTION_USERS
import com.example.mychat.data.storage.StorageConstants.KEY_DELETED
import com.example.mychat.data.storage.StorageConstants.KEY_EMAIL
import com.example.mychat.data.storage.StorageConstants.KEY_FCM_TOKEN
import com.example.mychat.data.storage.StorageConstants.KEY_LAST_INDEX
import com.example.mychat.data.storage.StorageConstants.KEY_LAST_MESSAGE
import com.example.mychat.data.storage.StorageConstants.KEY_MESSAGE
import com.example.mychat.data.storage.StorageConstants.KEY_PASSWORD
import com.example.mychat.data.storage.StorageConstants.KEY_TIMESTAMP
import com.example.mychat.data.storage.StorageConstants.KEY_USERS_ID_ARRAY
import com.example.mychat.domain.models.AuthData
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.ChatMessage
import com.example.mychat.domain.models.User
import com.example.mychat.domain.repository.ResultData
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    override suspend fun findUserByRef(userRef: DocumentReference): User {
        val userFirestore = userRef.get().await().toObject(UserFirestore::class.java)!!
        return UserMapper(ImageMapper()).transform(userFirestore)
    }

    override suspend fun findUser(authData: AuthData): User? {
        return suspendCoroutine {
            firestoreDb.collection(KEY_COLLECTION_USERS)
                .whereEqualTo(KEY_EMAIL, authData.email)
                .whereEqualTo(KEY_PASSWORD, authData.password)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
                        val userFirestore =
                            task.result.documents[0].toObject(UserFirestore::class.java)!!
                        val user = UserMapper(ImageMapper()).transform(userFirestore)
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

    override fun setCallbackOnChatUpdates(
        scope: CoroutineScope,
        chat: Chat,
        callback: () -> (Unit)
    ) {
        scope.launch {
            var registration: ListenerRegistration? = null
            try {
                registration = firestoreDb
                    .collection(KEY_COLLECTION_CHATS)
                    .document(chat.id)
                    .collection(KEY_COLLECTION_MESSAGES)
                    .addSnapshotListener { q: QuerySnapshot?, e: FirebaseFirestoreException? ->
                        if (e == null && q != null && !q.isEmpty && !q.metadata.isFromCache)
                            callback()
                    }
                awaitCancellation()
            } finally {
                registration?.remove()
            }
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
        chat: Chat,
        flow: FlowCollector<ResultData<Boolean>>,
    ): Boolean {
        val chatRef = firestoreDb.collection(KEY_COLLECTION_CHATS).document(chat.id)
        val senderRef =
            firestoreDb.collection(KEY_COLLECTION_USERS).document(chatMessage.sender.id)

        try {
            var newIndex: Long? = null
            firestoreDb.runTransaction {
                newIndex = it.get(chatRef).getLong(KEY_LAST_INDEX)?.plus(1) ?: 0
                it.update(
                    chatRef,
                    mapOf(
                        Pair(KEY_LAST_MESSAGE, chatMessage.message),
                        Pair(KEY_LAST_INDEX, newIndex)
                    )
                )
            }.await()
            val messageFirestore = ChatMessageFirestore(
                null,
                newIndex,
                senderRef,
                chatMessage.message,
                chatMessage.date,
                false
            )
            chatRef.collection(KEY_COLLECTION_MESSAGES).add(messageFirestore).await()
            flow.emit(ResultData.success(true))
            return true
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
            return false
        }

    }

    override suspend fun fetchMessages(
        chat: Chat,
        limit: Int,
        offset: DocumentSnapshot?,
    ): FetchMessagesResult {
        var list: MutableList<DocumentSnapshot>? = null
        if (offset == null)
            list = firestoreDb
                .collection(KEY_COLLECTION_CHATS)
                .document(chat.id)
                .collection(KEY_COLLECTION_MESSAGES)
                .orderBy(KEY_TIMESTAMP)
                .limitToLast(limit.toLong())
                .get().await().documents
        else
            list = firestoreDb
                .collection(KEY_COLLECTION_CHATS)
                .document(chat.id)
                .collection(KEY_COLLECTION_MESSAGES)
                .orderBy(KEY_TIMESTAMP)
                .endBefore(offset)
                .limitToLast(limit.toLong())
                .get().await().documents
        val newOffset: DocumentSnapshot? = list.first()

        return FetchMessagesResult(
            list = list.map { it.toObject(ChatMessageFirestore::class.java)!! },
            nextOffset = newOffset,
            prevOffset = offset
        )
    }

    override suspend fun findChatByRef(chatRef: DocumentReference): ChatFirestore {
        return chatRef.get().await().toObject(ChatFirestore::class.java)!!
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
            val lastMessage = "?? ?????? ?????? ?????? ??????????????????"
            val chatHashMap = hashMapOf(
                KEY_USERS_ID_ARRAY to usersIds,
                KEY_LAST_MESSAGE to lastMessage
            )
            val newChatId: String =
                firestoreDb.collection(KEY_COLLECTION_CHATS).add(chatHashMap).await().id
            val newChat = Chat(
                id = newChatId,
                userSender = userSender,
                userReceiver = users.find { it.id != userSender.id }!!,
                lastMessage = lastMessage
            )
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
                userSender = user,
                userReceiver = userReceiver,
                lastMessage = savedChatSnapshot.getString(KEY_LAST_MESSAGE)!!
            )
            flow.emit(ResultData.success(savedChat))
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
        }
    }

    override suspend fun fetchChats(
        user: User
    ) = channelFlow<ResultData<ChatFirestore>> {
        val docRef = firestoreDb.collection(KEY_COLLECTION_USERS).document(user.id)

        val registrationChat = firestoreDb.collection(KEY_COLLECTION_CHATS)
            .whereArrayContains(KEY_USERS_ID_ARRAY, docRef)
            .addSnapshotListener { value, error ->
                GlobalScope.launch {
                    try {
                        if (error == null && !value!!.isEmpty) {
                            value.documentChanges.map { doc ->
                                val chatFirestore = doc.document.toObject(ChatFirestore::class.java)
                                when (doc.type) {
                                    DocumentChange.Type.ADDED -> {
                                        send(ResultData.success(chatFirestore))
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        val deleted: Boolean? = doc.document.getBoolean(KEY_DELETED)
                                        if (deleted != null && deleted == true)
                                            send(ResultData.removed(chatFirestore))
                                        else
                                            send(ResultData.update(chatFirestore))
                                    }
                                    else -> {}
                                }
                            }
                        }
                    } catch (error: FirebaseFirestoreException) {
                        send(ResultData.failure(error.localizedMessage))
                    }
                }
            }
        awaitClose {
            registrationChat.remove()
        }
    }

    override suspend fun changeMessage(
        chatMessage: ChatMessage,
        flow: FlowCollector<ResultData<ChatMessage>>,
    ) {
        val messageRef = firestoreDb.collection(KEY_COLLECTION_MESSAGES).document(chatMessage.id)
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
        val messageRef = firestoreDb.collection(KEY_COLLECTION_MESSAGES).document(chatMessage.id)
        try {
            messageRef.update(KEY_DELETED, true).await()
            flow.emit(ResultData.success(chatMessage))
        } catch (error: FirebaseFirestoreException) {
            flow.emit(ResultData.failure(error.localizedMessage))
        }
    }
}