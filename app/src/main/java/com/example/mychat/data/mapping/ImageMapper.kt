package com.example.mychat.data.mapping

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

class ImageMapper: Mapper<String, Bitmap> {
    override fun transform(data: String): Bitmap {
        val bytes = Base64.decode(data, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}