package eu.corvus.essentials.core

import android.graphics.Bitmap
import android.util.LruCache

/**
 * Created by Vlad Cazacu on 13.03.2017.
 *
 * TODO bind it to application and activities
 */
object MemoryStorage {

    /**
     * Short term storage, the size of 5 elements
     */
    private val shortStorage = LruCache<String, Any>(5)

    /**
     * Stores in a short term storage, with max 5 elements <br>
     *     Dos not allow Bitmaps
     */
    fun storeShort(key: String, any: Any) {
        if (any is Bitmap)
            throw IllegalArgumentException("Bitmaps are not supported!")

        shortStorage.put(key, any)
    }

    fun shortStorage(): MutableMap<String, Any> {
        return shortStorage.snapshot()
    }

    fun <T> fetchShort(key: String, def: T): T {
        return shortStorage[key] as T? ?: def
    }

    fun <T> fetchOrNullShort(key: String, def: T? = null): T? {
        return shortStorage[key] as T? ?: def
    }

    fun delete(key: String) {
        shortStorage.remove(key)
    }

    fun onNoActivity() {
        clear()
    }

    fun clear() {
        shortStorage.evictAll()
    }

}