package com.example.mychat.data.storage.sharedPrefs

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.mychat.data.storage.StorageConstants
import com.example.mychat.data.storage.StorageConstants.KEY_EMAIL
import com.example.mychat.data.storage.StorageConstants.KEY_IMAGE
import com.example.mychat.data.storage.StorageConstants.KEY_NAME
import com.example.mychat.data.storage.StorageConstants.KEY_USER_ID
import com.example.mychat.domain.models.User
import java.io.ByteArrayOutputStream

class SharedPreferencesStorageImpl(appContext: Context): SharedPreferencesStorage {

    private val sharedPrefs = appContext.getSharedPreferences(StorageConstants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

    override fun saveUserDetails(user: User) {
        val editor = sharedPrefs.edit()
        editor.putString(KEY_USER_ID, user.id)
        editor.putString(KEY_IMAGE, encodeImage(user.image))
        editor.putString(KEY_NAME, user.name)
        editor.putString(KEY_EMAIL, user.email)
        editor.apply()
    }


    override fun getUserDetails(): User {
        val bytes = Base64.decode(sharedPrefs.getString(KEY_IMAGE, null), Base64.DEFAULT)
        val image: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val id = sharedPrefs.getString(KEY_USER_ID, "")
        val name = sharedPrefs.getString(KEY_NAME, "")
        val email = sharedPrefs.getString(KEY_EMAIL, "")

        return User (
            id = id!!,
            image = image,
            name = name!!,
            email = email!!,
            password = ""
                )
    }

    override fun clearUserDetails() {
        val editor = sharedPrefs.edit()
        editor.clear()
        editor.apply()
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth: Int = 150
        val previewHeight: Int = bitmap.height * previewWidth/ bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val bao = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao)
        val bytes = bao.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

}
