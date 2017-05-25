package eu.corvus.essentials.core.mvc

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by Vlad Cazacu on 17.03.2017.
 */
abstract class BaseFragment : Fragment(), ModelView {

    private lateinit var rootView: View
    open val controller: BaseController<out ModelView>? = null

    abstract val viewId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(viewId == 0)
            rootView = TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                gravity = Gravity.CENTER
                text = "View not setup"
            }
        else
            rootView = inflater.inflate(viewId, container, false)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        controller?.attach(this)
    }

    override fun onResume() {
        super.onResume()

        controller?.onRestore()
    }

    override fun onPause() {
        super.onPause()
        onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller?.dettach()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.dettach()
    }


}