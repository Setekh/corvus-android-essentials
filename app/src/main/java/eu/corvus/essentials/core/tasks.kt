package eu.corvus.essentials.core

import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

/**
 * Created by Vlad Cazacu on 05.05.2017.
 */
class Promise<T> {
    var isSuccessful = false
    var isCanceled = false

    var future: Future<*>? = null
        set(value) {
            if(isCanceled)
                value?.cancel(true)

            field = value
        }

    internal val successHandlers = LinkedList<ResultCallbackHolder<T>>()
    internal val failHandlers = LinkedList<ResultCallbackHolder<Throwable>>()
    internal var alwaysHandler: ResultCallbackHolder<Unit>? = null

    fun cancel(mayInterruptIfRunning: Boolean) {
        isCanceled = true
        future?.cancel(mayInterruptIfRunning)
    }

    fun doSuccess(returns: T) {
        isSuccessful = true
        successHandlers.forEach {
            if(it.isUi)
                threads.uiHandler.post { it.resultHandler.invoke(true, returns) }
            else
                it.resultHandler.invoke(true, returns)
        }
    }

    fun doFail(returns: Throwable) {
        isSuccessful = false
        failHandlers.forEach {
            if(it.isUi)
                threads.uiHandler.post { it.resultHandler.invoke(false, returns) }
            else
                it.resultHandler.invoke(false, returns)
        }
    }

    class ResultCallbackHolder<in E>(val isUi: Boolean, val resultHandler: (Boolean, E) -> Unit)

    fun doFinally() {
        successHandlers.clear()
        failHandlers.clear()
        val alwaysHandler = alwaysHandler ?: return

        this.alwaysHandler = null

        if(alwaysHandler.isUi)
            threads.uiHandler.post { alwaysHandler.resultHandler.invoke(isSuccessful, Unit) }
        else
            alwaysHandler.resultHandler.invoke(isSuccessful, Unit)
    }

    fun get(): T  {

        if(isCanceled)
            throw InterruptedException("Task interrupted!")

        return future!!.get() as T
    }
}

fun <V> task(body: () -> V): Promise<V> {
    val promise = Promise<V>()

    val futureTask = FutureTask<V>(Callable {
        var value: V? = null
        try {
            val v = body.invoke()
            value = v
            promise.doSuccess(v)
        } catch (e: Exception) {
            promise.doFail(e)
        } finally {
            promise.doFinally()
        }
        value
    })

    promise.future = futureTask

    threads.uiHandler.post { // on the next loop
        threads.executionService.submit(futureTask)
    }

    return promise
}

infix fun <T> Promise<T>.success(callback: ((T) -> Unit)) : Promise<T> {
    successHandlers += Promise.ResultCallbackHolder(false, { _, returns -> callback.invoke(returns) })
    return this
}

infix fun <T> Promise<T>.fail(callback: ((Throwable) -> Unit)) : Promise<T> {
    failHandlers += Promise.ResultCallbackHolder(false, { _, returns -> callback.invoke(returns) })
    return this
}

infix fun <T> Promise<T>.successUi(callback: ((T) -> Unit)) : Promise<T> {
    successHandlers += Promise.ResultCallbackHolder(true, { _, returns -> callback.invoke(returns) })
    return this
}

infix fun <T> Promise<T>.failUi(callback: ((Throwable) -> Unit)) : Promise<T> {
    failHandlers += Promise.ResultCallbackHolder(true, { _, returns -> callback.invoke(returns) })
    return this
}

infix fun <T> Promise<T>.finally(callback: ((Boolean) -> Unit)) : Promise<T> {
    alwaysHandler = Promise.ResultCallbackHolder(false, { success, _ -> callback.invoke(success) })
    return this
}

infix fun <T> Promise<T>.finallyUi(callback: ((Boolean) -> Unit)) : Promise<T> {
    alwaysHandler = Promise.ResultCallbackHolder(true, { success, _ -> callback.invoke(success) })
    return this
}
