package eu.corvus.essentials.core.mvc

import android.content.Context
import android.content.Intent
import eu.corvus.essentials.core.Dispatcher

/**
 * Created by Vlad Cazacu on 17.03.2017.
 */
abstract class BaseController<MV: ModelView> : Dispatcher.EventListener {

    protected var modelView: MV? = null

    fun attach(modelView: ModelView) {
        this.modelView = modelView as MV
        onAttached()
    }

    fun dettach() {
        onDetached()
        Dispatcher.unregister(this)
        modelView = null
    }

    protected abstract fun onAttached()
    protected abstract fun onDetached()

    open fun onRestore() = Unit
    open fun onSuspend() = Unit

    open fun onEventUi(intent: Intent) = Unit

    override final fun onReceive(context: Context, intent: Intent) {
        onEventUi(intent)
    }

    fun sendBroadcast(intent: Intent) = Dispatcher.sendBroadcast(intent)

    fun registerEvents(action: String, vararg actions: String) {
        Dispatcher.registerEvents(this, action, *actions)
    }
}