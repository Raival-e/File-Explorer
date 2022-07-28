package com.raival.fileexplorer.common.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raival.fileexplorer.R

class OptionsDialog(var title: String) : BottomSheetDialogFragment() {
    private val options = ArrayList<OptionHolder>()
    private var container: LinearLayout? = null

    fun addOption(
        label: String?,
        listener: View.OnClickListener?,
        dismissOnClick: Boolean
    ): OptionsDialog {
        return addOption(label, 0, listener, dismissOnClick)
    }

    fun addOption(
        label: String?,
        resId: Int,
        listener: View.OnClickListener?,
        dismissOnClick: Boolean
    ): OptionsDialog {
        val optionHolder = OptionHolder()
        optionHolder.dismissOnClick = dismissOnClick
        optionHolder.label = label
        optionHolder.listener = listener
        optionHolder.res = resId
        options.add(optionHolder)
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.common_options_dialog, container, false)
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view.findViewById(R.id.container)
        (view.findViewById<View>(R.id.title) as TextView).text = title
        view.findViewById<View>(R.id.msg).visibility = View.GONE
        addOptions()
    }

    private fun addOptions() {
        for (optionHolder in options) {
            val v = layoutInflater.inflate(R.layout.common_options_dialog_item, container, false)
            if (optionHolder.res != 0) {
                val icon = v.findViewById<ImageView>(R.id.icon)
                icon.visibility = View.VISIBLE
                icon.setImageResource(optionHolder.res)
            }
            (v.findViewById<View>(R.id.label) as TextView).text = optionHolder.label
            v.findViewById<View>(R.id.background).setOnClickListener { view: View? ->
                if (optionHolder.listener != null) optionHolder.listener!!.onClick(view)
                if (optionHolder.dismissOnClick) v.post { dismiss() }
            }
            container!!.addView(v)
        }
    }

    private class OptionHolder {
        var label: String? = null
        var res = 0
        var listener: View.OnClickListener? = null
        var dismissOnClick = false
    }
}