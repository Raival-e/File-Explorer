package com.raival.fileexplorer.activity.editor.autocomplete

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.util.Utils
import io.github.rosemoe.sora.widget.component.CompletionLayout
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class CustomCompletionLayout : CompletionLayout {
    private lateinit var mListView: ListView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mBackground: GradientDrawable
    private lateinit var mEditorAutoCompletion: EditorAutoCompletion

    override fun onApplyColorScheme(colorScheme: EditorColorScheme) {
        mBackground.setStroke(
            1,
            colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND)
        )
        mBackground.setColor(colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND))
    }

    override fun setEditorCompletion(completion: EditorAutoCompletion) {
        mEditorAutoCompletion = completion
    }

    override fun inflate(context: Context): View {
        val layout = RelativeLayout(context)
        mProgressBar = ProgressBar(context)
        layout.addView(mProgressBar)

        val params = mProgressBar.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        params.height = Utils.pxToDp(30f).toInt()
        params.width = params.height

        mBackground = GradientDrawable()
        mBackground.cornerRadius = Utils.pxToDp(8f)
        layout.background = mBackground

        mListView = ListView(context)
        mListView.dividerHeight = 0
        layout.addView(mListView, LinearLayout.LayoutParams(-1, -1))
        mListView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                try {
                    mEditorAutoCompletion.select(position)
                } catch (e: Exception) {
                    showMsg(e.toString())
                }
            }
        setLoading(true)
        return layout
    }

    override fun getCompletionList(): AdapterView<*> {
        return mListView
    }

    override fun setLoading(loading: Boolean) {
        mProgressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    override fun ensureListPositionVisible(position: Int, incrementPixels: Int) {
        mListView.post {
            while (mListView.firstVisiblePosition + 1 > position && mListView.canScrollList(-1)) {
                performScrollList(incrementPixels / 2)
            }
            while (mListView.lastVisiblePosition - 1 < position && mListView.canScrollList(1)) {
                performScrollList(-incrementPixels / 2)
            }
        }
    }

    private fun performScrollList(offset: Int) {
        val adpView = completionList
        val down = SystemClock.uptimeMillis()
        var ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_DOWN, 0f, 0f, 0)

        adpView.onTouchEvent(ev)
        ev.recycle()

        ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_MOVE, 0f, offset.toFloat(), 0)
        adpView.onTouchEvent(ev)
        ev.recycle()

        ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_CANCEL, 0f, offset.toFloat(), 0)
        adpView.onTouchEvent(ev)
        ev.recycle()
    }
}