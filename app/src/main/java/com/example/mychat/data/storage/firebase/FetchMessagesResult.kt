package com.example.mychat.data.storage.firebase

import com.example.mychat.data.models.ChatMessageFirestore
import com.google.firebase.firestore.DocumentSnapshot

data class FetchMessagesResult(
    val list: List<ChatMessageFirestore>,
    val prevOffset: DocumentSnapshot?,
    val nextOffset: DocumentSnapshot?,
)