package eu.corvus.essentials.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by Vlad Cazacu on 7/13/2016.
 */
object Dispatcher : BroadcastReceiver() {
    private lateinit var context: Context
    private val listeners = ConcurrentHashMap<String, ConcurrentLinkedQueue<EventListener>>()

    private lateinit var handler: Handler
    internal fun initialize(context: Context) {
        this.context = context

        handler = Handler(Looper.getMainLooper())
    }

    fun registerEvents(listener: EventListener, action: String, vararg actions: String) {
        synchronized(listeners) {
            val registerForEvent: (String) -> Unit = { key ->
                val list = listeners[key] ?: ConcurrentLinkedQueue<EventListener>().apply { listeners.put(key, this) }
                list.add(listener)
            }

            registerForEvent(action)

            actions.forEach { registerForEvent(it) }
        }
    }

    fun unregister(listener: EventListener) {
        synchronized(listeners) {
            listeners.values.forEach{ it.remove(listener) }
        }
    }

    fun unregisterFrom(listener: EventListener, action: String, vararg actions: String) {
        synchronized(listeners) {
            val removeFrom : (String) -> Unit = { key ->
                listeners[key]?.removeAll { it == listener }
            }

            removeFrom(action)
            actions.forEach { removeFrom(it) }
        }
    }

    fun sendBroadcast(intent: Intent) {
        when(intent.action) {
            AppForeground -> { }
            AppBackground -> { }
        }

        handler.post { onReceive(context, intent) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val list = listeners[intent.action] ?: return
        list.forEach { it.onReceive(context, intent) }
    }

    interface EventListener {
        fun onReceive(context: Context, intent: Intent)
    }
}