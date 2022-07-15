package com.raival.fileexplorer.activity.editor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.util.Utils;

import io.github.rosemoe.sora.widget.CodeEditor;

public class SymbolInputView extends LinearLayout {

    private int textColor;
    private CodeEditor editor;

    public SymbolInputView(Context context) {
        super(context);
        init();
    }

    public SymbolInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SymbolInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Utils.getColorAttribute(R.attr.backgroundColor, getContext()));
        setOrientation(HORIZONTAL);
        setTextColor(Utils.getColorAttribute(R.attr.colorOnSurface, getContext()));
    }

    public SymbolInputView bindEditor(CodeEditor editor) {
        this.editor = editor;
        return this;
    }

    public SymbolInputView setTextColor(int color) {
        for (int i = 0; i < getChildCount(); i++) {
            ((Button) getChildAt(i)).setTextColor(color);
        }
        textColor = color;
        return this;
    }

    public SymbolInputView addSymbols(String[] symbols) {
        for (String symbol : symbols) addSymbol(symbol);
        return this;
    }

    public SymbolInputView addSymbol(String display) {
        return addSymbol(display, display, display.length());
    }

    public SymbolInputView addSymbol(String display, String content) {
        return addSymbol(display, content, content.length());
    }

    public SymbolInputView addSymbol(String display, String content, int cursorPos) {
        Button btn = new Button(getContext(), null, android.R.attr.buttonStyleSmall);
        btn.setText(display);

        TypedValue out = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, out, true);
        btn.setBackgroundResource(out.resourceId);

        btn.setTextColor(textColor);
        addView(btn, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        btn.setOnClickListener((view) -> {
            if (editor != null)
                editor.insertText(content, cursorPos);
        });
        return this;
    }
}