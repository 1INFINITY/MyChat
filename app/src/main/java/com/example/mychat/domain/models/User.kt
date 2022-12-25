package com.example.mychat.domain.models

import android.graphics.Bitmap

class User(
    val image: Bitmap,
    val name: String,
    val email: String,
    val password: String,
)