package com.raival.fileexplorer.activity.editor.autocomplete

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.raival.fileexplorer.R
import com.raival.fileexplorer.extension.toDp
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter

class CustomCompletionItemAdapter : EditorCompletionAdapter() {
    override fun getItemHeight(): Int = 45.toDp()

    public override fun getView(
        pos: Int,
        view: View?,
        parent: ViewGroup,
        isCurrentCursorPosition: Boolean
    ): View {
        val v: View = view ?: LayoutInflater.from(context)
            .inflate(R.layout.text_editor_completion_item, parent, false)

        val item = getItem(pos)
        var tv = v.findViewById<TextView>(R.id.result_item_label)
        val iv = v.findViewById<TextView>(R.id.result_item_image)

        tv.text = item.label
        tv = v.findViewById(R.id.result_item_desc)
        tv.text = item.desc
        v.tag = pos
        iv.text = item.desc.subSequence(0, 1)

        return v
    }
}