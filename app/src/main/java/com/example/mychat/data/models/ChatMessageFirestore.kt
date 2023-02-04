package com.example.mychat.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import java.util.*

data class ChatMessageFirestore(
    @DocumentId
    val id: String?,
    val index: Long?,
    val senderId: DocumentReference?,
    val message: String?,
    val timestamp: Date?,
    val deleted: Boolean?,
) {
    constructor() : this(null, null, null, null, null, null)
}