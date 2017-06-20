package eu.corvus.essentials.core

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import org.jetbrains.anko.AnkoLogger
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * Created by Vlad Cazacu on 10/4/2016.
 */
abstract class BaseApplication : Application(), Application.ActivityLifecycleCallbacks, AnkoLogger {

    val activityStack = mutableMapOf<Class<out Activity>, ActivityInfo>()

    var topActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()

        appContext = this
        threads = provideThreads()

        registerActivityLifecycleCallbacks(this)

        Dispatcher.initialize(this)
        Preferences.initialize(this)


        configureTimber()
        initialize()
    }

    private fun provideThreads(): Threads {
        val execService = Executors.newFixedThreadPool(getNumberOfCores())
        val scheduleService = Executors.newScheduledThreadPool(getNumberOfCores())

        return Threads(execService, scheduleService, Handler(Looper.getMainLooper()))
    }

    /**
     * Override this in production!
     */
    fun configureTimber() {
        Timber.plant(Timber.DebugTree())
    }

    abstract fun initialize() // initialize

    override fun onActivityStarted(activity: Activity) { }

    override fun onActivityPaused(activity: Activity) {
        changeState(activity, ActivityState.Paused)
    }

    override fun onActivityResumed(activity: Activity) {
        changeState(activity, ActivityState.Active)
    }

    override fun onActivityStopped(activity: Activity) {
        changeState(activity, ActivityState.Stopped)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        changeState(activity, ActivityState.Created)
    }

    override fun onActivityDestroyed(activity: Activity) {
        changeState(activity, ActivityState.Destroyed)

        if(activityStack.isEmpty()) {
            MemoryStorage.onNoActivity()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) = Unit

    private fun changeState(activity: Activity, activityState: ActivityState) {
        val activityInfo = when(activityState) {
            ActivityState.Created -> {
                val activityInfo = ActivityInfo(activity.javaClass, activity, activityState)
                activityStack.put(activity.javaClass, activityInfo)
                topActivity = activity
                activityInfo
            }
            ActivityState.Destroyed -> activityStack.remove(activity.javaClass)
            else -> activityStack[activity.javaClass]
        }

        activityInfo ?: return

        activityInfo.activityState = activityState

        if(activityState == ActivityState.Active)
            topActivity = activity

        if(currentActivity() == null)
            Dispatcher.sendBroadcast(Intent(AppBackground))
        else
            Dispatcher.sendBroadcast(Intent(AppForeground))
    }

    fun currentActivity(): Activity? {
        var currentActivity: Activity? = null

        activityStack.forEach {
            val value = it.value

            if (value.activityState == ActivityState.Active)
                currentActivity = value.activity
        }
        return currentActivity
    }

    fun clearTop() {
        activityStack.clear()
    }

    enum class ActivityState { Active, Paused, Stopped, Created, Destroyed }
    data class ActivityInfo(val type: Class<out Activity>, var activity: Activity, var activityState: ActivityState)

    override fun onTerminate() {
        super.onTerminate()
        threads.terminate()
    }
}