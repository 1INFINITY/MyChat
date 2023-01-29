package com.example.mychat.data.models

import com.google.firebase.firestore.DocumentId

class UserRegisteredFirestore(
    @DocumentId
    val id: String? = null,
    val email: String? = null,
    val image: String? = null,
    val name: String? = null,
    val password: String? = null,
) {
    constructor(): this(null, null, null, null, null)
}