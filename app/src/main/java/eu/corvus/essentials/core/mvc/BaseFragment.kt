package eu.corvus.essentials.core.mvc

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.flirtsy.app.core.mvc.BaseController
import com.flirtsy.app.core.mvc.ModelView

/**
 * Created by Vlad Cazacu on 17.03.2017.
 */
abstract class BaseFragment : Fragment(), ModelView {

    private lateinit var rootView: View
    open val controller: BaseController<out ModelView>? = null

    abstract val viewId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(viewId == 0)
            rootView = FrameLayout(context)
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