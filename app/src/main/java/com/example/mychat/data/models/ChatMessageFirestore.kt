package com.example.mychat.data.models

import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.models.User
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class ChatMessageFirestore(
    @DocumentId
    val id: String?,
    val chatId: DocumentReference?,
    val senderId: DocumentReference?,
    val message: String?,
    val timestamp: Date?,
    val deleted: Boolean?
) {
    constructor(): this(null, null, null, null, null, null)
}