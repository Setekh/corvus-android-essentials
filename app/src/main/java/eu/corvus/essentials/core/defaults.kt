@file:JvmName("Defaults")
package eu.corvus.essentials.core

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.stream.JsonWriter
import eu.corvus.essentials.core.mvc.BaseActivity
import java.io.Serializable
import java.io.StringWriter
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import kotlin.properties.Delegates

/**
 * Created by Vlad Cazacu on 10/4/2016.
 */

var appContext: BaseApplication by Delegates.notNull<BaseApplication>()

val gson by lazy { Gson() }

var threads: Threads by Delegates.notNull<Threads>()
open class Threads(val executionService: ExecutorService, val scheduledExecutorService: ScheduledExecutorService, val uiHandler: Handler) {
    fun terminate() {
        try {
            executionService.shutdownNow()
        } catch (e: Exception) {
            error("Failed shutting down exec service!", e)
        }
        try {
            scheduledExecutorService.shutdownNow()
        } catch (e: Exception) {
            error("Failed shutting down scheduled exec service!", e)
        }
    }
}

fun JsonArray.addAll(filter: Iterable<JsonElement>) = filter.forEach { add(it) }
fun Any.toJson(): JsonElement = gson.toJsonTree(this)

inline fun <reified T: Any> JsonObject.fromJson(): T = gson.fromJson(this, T::class.java)

fun JsonElement.prettyPrint(stringWriter: StringWriter = StringWriter()): String {
    val jsonWriter = JsonWriter(stringWriter)
    jsonWriter.setIndent("  ")
    gson.toJson(this, jsonWriter)
    return stringWriter.toString()
}

fun Serializable.storeInPreferences() = Preferences.store(this)

fun Activity.contentView() = findViewById(android.R.id.content) as ViewGroup
fun Activity.contentViewChild() = contentView().getChildAt(0) as ViewGroup
fun Activity.isOnTop() = appContext.topActivity == this
fun Activity.isActive() = appContext.currentActivity() == this

inline fun <reified T : Any> typeOf(): Class<T> = T::class.java

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun Activity.hideSoftKeyboard(view: View? = null) {
    val curs = view ?: currentFocus ?: return

    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(curs.windowToken, 0)
}

fun BaseActivity.changeStatusColorChecked(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        this.changeStatusColor(color)
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private fun BaseActivity.changeStatusColor(color: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.statusBarColor = color
}

inline fun <reified T : Any> List<Any>.fetch(predicate: (T) -> Boolean) : T? {
    return find { it is T && predicate(it) } as T?
}

inline fun <reified T : Any> List<Any>.exists(predicate: (T) -> Boolean) : Boolean {
    return any { it is T && predicate(it) }
}

fun isEmailValid(email: String?): Boolean {
    return !(email == null || TextUtils.isEmpty(email)) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun getNumberOfCores(): Int {
    if(Build.VERSION.SDK_INT >= 17) {
        return Runtime.getRuntime().availableProcessors()
    }
    else {
        return 2 // FIXME assume 2
    }
}

// Default Intent Actions
const val RequestPermissionResult = "_PERMIS_REQ"
const val ActivityResult = "_ACT_REZ_"
const val AppBackground = "_GO_BAK"
const val AppForeground = "_GO_UP"

