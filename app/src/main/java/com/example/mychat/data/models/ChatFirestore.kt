package com.example.mychat.data.models

import com.example.mychat.data.storage.StorageConstants.KEY_LAST_MESSAGE
import com.example.mychat.data.storage.StorageConstants.KEY_USERS_ID_ARRAY
import com.example.mychat.domain.models.User
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName
import java.util.ArrayList

data class ChatFirestore (
    @DocumentId
    val id: String? = null,
    @PropertyName(KEY_USERS_ID_ARRAY)
    val usersIdArray: ArrayList<DocumentReference>?,
    @PropertyName(KEY_LAST_MESSAGE)
    val lastMessage: String?,
) {
    constructor(): this(null, null, null)
}
