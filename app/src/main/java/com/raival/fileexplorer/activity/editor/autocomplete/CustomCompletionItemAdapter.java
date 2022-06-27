package com.raival.fileexplorer.activity.editor.autocomplete;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.util.Utils;

import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter;

public class CustomCompletionItemAdapter extends EditorCompletionAdapter {

    @Override
    public int getItemHeight() {
        return (int) Utils.pxToDp(45);
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent, boolean isCurrentCursorPosition) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.text_editor_completion_item, parent, false);
        }
        CompletionItem item = getItem(pos);
        TextView tv = view.findViewById(R.id.result_item_label);
        tv.setText(item.label);
        tv = view.findViewById(R.id.result_item_desc);
        tv.setText(item.desc);
        view.setTag(pos);
        TextView iv = view.findViewById(R.id.result_item_image);
        iv.setText(item.desc.subSequence(0, 1));
        return view;
    }

}