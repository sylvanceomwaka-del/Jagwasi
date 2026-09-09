package com.jagwasi.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Formats a timestamp (milliseconds since epoch) as "dd/MM/yyyy HH:mm"
 * using the device's default locale. A new SimpleDateFormat instance is
 * created each call to avoid stale locale/timezone caches.
 */
fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(timestamp)
}

/**
 * Converts a Bitmap to a Base64‑encoded string using PNG compression.
 */
fun bitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val byteArray = stream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

/**
 * Decodes a Base64‑encoded string back to a Bitmap.
 * Returns `null` if the input is malformed or cannot be decoded.
 */
fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val byteArray = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } catch (e: IllegalArgumentException) {
        // Invalid Base64 input
        null
    } catch (e: Exception) {
        // Decoding failure (e.g., corrupted image data)
        null
    }
}
