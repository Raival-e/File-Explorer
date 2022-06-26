package com.raival.fileexplorer.activities.editor.schemes;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class DarkScheme extends EditorColorScheme {

    @Override
    public void applyDefault() {
        super.applyDefault();
        setColor(ANNOTATION, 0xffbbb529);
        setColor(FUNCTION_NAME, 0xFF67A6F4);
        setColor(IDENTIFIER_NAME, 0xFF86A1DF);
        setColor(IDENTIFIER_VAR, 0xFFE8BE7F);
        setColor(LITERAL, 0xFF45CB40);
        setColor(OPERATOR, 0xFFCDD9E5);
        setColor(COMMENT, 0xff898888);
        setColor(KEYWORD, 0xFFB496DE);
        setColor(WHOLE_BACKGROUND, 0xFF2A2D3E);
        setColor(TEXT_NORMAL, 0xFFCDD9E5);
        setColor(LINE_NUMBER_BACKGROUND, 0xFF2A2D3E);
        setColor(LINE_NUMBER, 0xFF5C7084);
        setColor(LINE_DIVIDER, 0xFF5C7084);
        setColor(SCROLL_BAR_THUMB, 0x77a6a6a6);
        setColor(SCROLL_BAR_THUMB_PRESSED, 0x77565656);
        setColor(SELECTED_TEXT_BACKGROUND, 0xff3676b8);
        setColor(MATCHED_TEXT_BACKGROUND, 0xff32593d);
        setColor(CURRENT_LINE, 0xFF303844);
        setColor(SELECTION_INSERT, 0xffffffff);
        setColor(SELECTION_HANDLE, 0xffffffff);
        setColor(BLOCK_LINE, 0xff454F59);
        setColor(BLOCK_LINE_CURRENT, 0xdd454F59);
        setColor(NON_PRINTABLE_CHAR, 0xFF5C7084);
        setColor(TEXT_SELECTED, 0xffffffff);
    }

}