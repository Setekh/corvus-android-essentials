package eu.corvus.essentials.core.mvc

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import eu.corvus.essentials.core.ActivityResult
import eu.corvus.essentials.core.Dispatcher
import eu.corvus.essentials.core.RequestPermissionResult
import eu.corvus.essentials.core.debug

/**
 * Created by Vlad Cazacu on 10/4/2016.
 */
abstract class BaseActivity : AppCompatActivity(), ModelView {

    open val controller: BaseController<out BaseActivity>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialize(savedInstanceState)
        controller?.attach(this)
    }

    abstract fun initialize(savedInstanceState: Bundle?)

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        intent.putExtra("requestCode", requestCode)

        super.startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val intent = data?.apply { action = ActivityResult } ?: Intent(ActivityResult)
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("resultCode", resultCode)

        if (data != null) {
//            intent.putExtra("intent-data", data.data) // its like this since the uri stops the local broadcasting, for some reason TODO investigate
//            intent.putExtra("intent-type", data.type)

            intent.data = data.data
            intent.type = data.type

            if (data.extras != null && !data.extras.isEmpty)
                intent.putExtras(data.extras)
        }

        Dispatcher.sendBroadcast(intent)
        debug("Activity Result $intent")
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val intent = Intent(RequestPermissionResult)
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("permissions", permissions)
        intent.putExtra("grantResults", grantResults)

        Dispatcher.sendBroadcast(intent)
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)

        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        controller?.onRestore()
    }

    override fun onPause() {
        super.onPause()
        onStart()
    }

    override fun onDestroy() {
        super.onDestroy()

        controller?.dettach()
    }

}
