package com.raival.fileexplorer.common.dialog

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.raival.fileexplorer.R

class CustomDialog : BottomSheetDialogFragment() {
    private val views = ArrayList<View>()
    private lateinit var icon: Drawable
    private lateinit var title: String
    private lateinit var msg: String
    private lateinit var positiveButton: String
    private lateinit var positiveListener: Listener
    private lateinit var negativeButton: String
    private lateinit var negativeListener: Listener
    private lateinit var neutralButton: String
    private lateinit var neutralListener: Listener
    private lateinit var msgView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.common_custom_dialog, container, true)
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleView = view.findViewById<TextView>(R.id.dialog_title)
        msgView = view.findViewById(R.id.dialog_msg)
        val imageView = view.findViewById<ShapeableImageView>(R.id.dialog_icon)
        val containerView = view.findViewById<LinearLayout>(R.id.dialog_container)
        val positiveButtonView = view.findViewById<MaterialButton>(R.id.dialog_positive_button)
        val negativeButtonView = view.findViewById<MaterialButton>(R.id.dialog_negative_button)
        val neutralButtonView = view.findViewById<MaterialButton>(R.id.dialog_neutral_button)
        if (this::icon.isInitialized) {
            imageView.visibility = View.VISIBLE
            imageView.setImageDrawable(icon)
        }
        if (this::title.isInitialized) {
            titleView.visibility = View.VISIBLE
            titleView.text = title
        }
        if (this::msg.isInitialized) {
            msgView.visibility = View.VISIBLE
            msgView.text = msg
        }
        if (views.size > 0) {
            containerView.visibility = View.VISIBLE
            for (view1 in views) {
                containerView.addView(view1)
            }
        }
        if (this::positiveButton.isInitialized) {
            positiveButtonView.visibility = View.VISIBLE
            positiveButtonView.text = positiveButton
            positiveButtonView.setOnClickListener { view1: View? ->
                positiveListener.listener.onClick(view1)
                if (positiveListener.dismiss) dismiss()
            }
        }
        if (this::negativeButton.isInitialized) {
            negativeButtonView.visibility = View.VISIBLE
            negativeButtonView.text = negativeButton
            negativeButtonView.setOnClickListener { view1: View? ->
                negativeListener.listener.onClick(view1)
                if (negativeListener.dismiss) dismiss()
            }
        }
        if (this::neutralButton.isInitialized) {
            neutralButtonView.visibility = View.VISIBLE
            neutralButtonView.text = neutralButton
            neutralButtonView.setOnClickListener { view1: View? ->
                neutralListener.listener.onClick(view1)
                if (neutralListener.dismiss) dismiss()
            }
        }
    }

    fun setTitle(title: String): CustomDialog {
        this.title = title
        return this
    }

    fun setMsg(msg: String): CustomDialog {
        this.msg = msg
        if (this::msgView.isInitialized) msgView.text = msg
        return this
    }

    fun setIcon(resId: Int): CustomDialog {
        icon = AppCompatResources.getDrawable(requireActivity(), resId)!!
        return this
    }

    fun setIconDrawable(drawable: Drawable): CustomDialog {
        this.icon = drawable
        return this
    }

    fun addView(view: View): CustomDialog {
        views.add(view)
        return this
    }

    fun setPositiveButton(
        label: String,
        listener: View.OnClickListener?,
        dismiss: Boolean
    ): CustomDialog {
        positiveButton = label
        positiveListener = Listener(listener ?: emptyListener(), dismiss)
        return this
    }

    fun setNegativeButton(
        label: String,
        listener: View.OnClickListener?,
        dismiss: Boolean
    ): CustomDialog {
        negativeButton = label
        negativeListener = Listener(listener ?: emptyListener(), dismiss)
        return this
    }

    fun setNeutralButton(
        label: String,
        listener: View.OnClickListener?,
        dismiss: Boolean
    ): CustomDialog {
        neutralButton = label
        neutralListener = Listener(listener ?: emptyListener(), dismiss)
        return this
    }

    private fun emptyListener(): View.OnClickListener = View.OnClickListener { }

    private class Listener(var listener: View.OnClickListener, var dismiss: Boolean)
}