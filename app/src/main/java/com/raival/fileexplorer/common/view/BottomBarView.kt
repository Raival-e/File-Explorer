package com.raival.fileexplorer.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.raival.fileexplorer.R
import com.raival.fileexplorer.util.PrefsUtils

class BottomBarView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun addItem(tag: String?, icon: Int, clickListener: OnClickListener?) {
        val view = (context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.bottom_bar_menu_item, this, false)
        view.setOnClickListener(clickListener)
        view.tooltipText = tag
        val label = view.findViewById<TextView>(R.id.label)
        if (PrefsUtils.Settings.showBottomToolbarLabels) {
            label.text = tag
        } else {
            label.visibility = GONE
        }
        val image = view.findViewById<ImageView>(R.id.icon)
        image.setImageResource(icon)
        addView(view)
    }

    fun addItem(tag: String?, view: View) {
        view.tooltipText = tag
        addView(view)
    }

    fun clear() {
        removeAllViews()
    }

    fun onUpdatePrefs() {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val label = view.findViewById<View>(R.id.label)
            if (label != null) {
                label.visibility =
                    if (PrefsUtils.Settings.showBottomToolbarLabels) VISIBLE else GONE
            }
        }
    }
}