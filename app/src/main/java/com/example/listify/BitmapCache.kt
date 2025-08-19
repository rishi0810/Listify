package com.example.listify

import android.graphics.Bitmap
import android.util.LruCache

/**
 * Simple LRU cache for Bitmaps keyed by string (e.g., URI string).
 * Size is measured in KB.
 */
object BitmapCache {
    private val maxSizeKb = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt() // use 1/8th of available memory

    private val cache = object : LruCache<String, Bitmap>(maxSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return try {
                value.byteCount / 1024
            } catch (e: Exception) {
                super.sizeOf(key, value)
            }
        }
    }

    fun get(key: String?): Bitmap? {
        if (key == null) return null
        return cache.get(key)
    }

    fun put(key: String?, bitmap: Bitmap?) {
        if (key == null || bitmap == null) return
        cache.put(key, bitmap)
    }
}
