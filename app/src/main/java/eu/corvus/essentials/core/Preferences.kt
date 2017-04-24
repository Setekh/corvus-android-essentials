package eu.corvus.essentials.core

import android.content.Context
import android.preference.PreferenceManager
import android.util.Base64
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.net.URLDecoder
import kotlin.reflect.KClass

/**
 * Created by Vlad Cazacu on 5/2/2016.
 */
object Preferences : AnkoLogger {

    private lateinit var context: Context
    val defaultPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    internal fun initialize(context: Context) {
        this.context = context
    }

    fun <T : Any> store(obj: T) {
        val value = gson.toJson(obj)

        defaultPrefs.edit().putString("obj-${obj.javaClass.simpleName}", Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)).apply()
    }

    fun <T : Any> fetch(clazz: KClass<T>) : T? {
        return fetch(clazz.java)
    }

    fun clear(clazz: KClass<*>) {
        clear(clazz.java)
    }

    fun clear(clazz: KClass<*>, vararg clazzz: KClass<*>) {
        clear(clazz.java)

        for(i in clazzz.indices)
            clear(clazzz[i].java)
    }

    fun contains(clazz: KClass<*>): Boolean {
        return defaultPrefs.contains("obj-${clazz.java.simpleName}")
    }

    fun clear(clazz: Class<*>) {
        defaultPrefs.edit().remove("obj-${clazz.simpleName}").apply()

        debug("Cleared object[${clazz.simpleName}] not found.")
    }

    fun <T : Any> fetch(clazz: Class<T>) : T? {
        val value: String? = defaultPrefs.getString("obj-${clazz.simpleName}", null)

        try {
            if (value != null)
                return gson.fromJson(String(Base64.decode(value, Base64.NO_WRAP), Charsets.UTF_8), clazz)
        } catch (e: Exception) {
            val obj = gson.fromJson(URLDecoder.decode(value), clazz)
            store(obj)
            return obj
        }

        debug("Fetched object[${clazz.simpleName}] not found.")
        return null
    }

    /**
     * Used to wipe everything use #clear if you want to remove a specific thing
     */
    fun clearDefaults() {
        defaultPrefs.edit().clear().commit()
    }
}