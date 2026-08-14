package com.sparrow.laundrysys.classes

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CloudinaryManager {
    private const val CLOUD_NAME = "dhwqwvh7x"
    private const val API_KEY = "115482868459466"
    private const val API_SECRET = "hp7vvUgugzFROmejw4Pwej7SYFA"

    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", CLOUD_NAME,
            "api_key", API_KEY,
            "api_secret", API_SECRET
        )
    )

    suspend fun uploadImage(imageUri: Uri, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap())
                uploadResult["secure_url"] as String
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}