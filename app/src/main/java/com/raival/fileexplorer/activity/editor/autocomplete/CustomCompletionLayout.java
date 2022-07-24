package com.raival.fileexplorer.activity.editor.autocomplete;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.raival.fileexplorer.App;
import com.raival.fileexplorer.util.Utils;

import io.github.rosemoe.sora.widget.component.CompletionLayout;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class CustomCompletionLayout implements CompletionLayout {

    private ListView mListView;
    private ProgressBar mProgressBar;
    private GradientDrawable mBackground;
    private EditorAutoCompletion mEditorAutoCompletion;

    @Override
    public void onApplyColorScheme(EditorColorScheme colorScheme) {
        mBackground.setStroke(1, colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND));
        mBackground.setColor(colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND));
    }

    @Override
    public void setEditorCompletion(EditorAutoCompletion completion) {
        mEditorAutoCompletion = completion;
    }

    @Override
    public View inflate(Context context) {
        RelativeLayout layout = new RelativeLayout(context);

        mProgressBar = new ProgressBar(context);
        layout.addView(mProgressBar);
        var params = ((RelativeLayout.LayoutParams) mProgressBar.getLayoutParams());
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.width = params.height = (int) Utils.pxToDp(30);

        mBackground = new GradientDrawable();
        mBackground.setCornerRadius(Utils.pxToDp(8));
        layout.setBackground(mBackground);

        mListView = new ListView(context);
        mListView.setDividerHeight(0);
        layout.addView(mListView, new LinearLayout.LayoutParams(-1, -1));
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                mEditorAutoCompletion.select(position);
            } catch (Exception e) {
                App.showMsg(e.toString());
            }
        });

        setLoading(true);
        return layout;
    }

    @Override
    public AdapterView getCompletionList() {
        return mListView;
    }

    @Override
    public void setLoading(boolean loading) {
        mProgressBar.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void ensureListPositionVisible(int position, int incrementPixels) {
        mListView.post(() -> {
            while (mListView.getFirstVisiblePosition() + 1 > position && mListView.canScrollList(-1)) {
                performScrollList(incrementPixels / 2);
            }
            while (mListView.getLastVisiblePosition() - 1 < position && mListView.canScrollList(1)) {
                performScrollList(-incrementPixels / 2);
            }
        });
    }

    private void performScrollList(int offset) {
        var adpView = getCompletionList();

        long down = SystemClock.uptimeMillis();
        var ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_DOWN, 0, 0, 0);
        adpView.onTouchEvent(ev);
        ev.recycle();

        ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_MOVE, 0, offset, 0);
        adpView.onTouchEvent(ev);
        ev.recycle();

        ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_CANCEL, 0, offset, 0);
        adpView.onTouchEvent(ev);
        ev.recycle();
    }

}
