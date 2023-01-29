package com.example.mychat.data.mapping

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

class EncodedImageMapper : Mapper<Bitmap, String> {
    override fun transform(data: Bitmap): String {
        return encodeImage(data)
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
}