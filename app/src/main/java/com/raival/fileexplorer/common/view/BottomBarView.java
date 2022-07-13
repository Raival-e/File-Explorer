package com.raival.fileexplorer.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.util.PrefsUtils;

public class BottomBarView extends LinearLayout {
    public BottomBarView(Context context) {
        super(context);
    }

    public BottomBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addItem(String tag, int icon, OnClickListener clickListener) {
        View view = ((LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.bottom_bar_menu_item, this, false);
        view.setOnClickListener(clickListener);
        view.setTooltipText(tag);
        TextView label = view.findViewById(R.id.label);
        if (PrefsUtils.Settings.getShowBottomToolbarLabels()) {
            label.setText(tag);
        } else {
            label.setVisibility(GONE);
        }
        ImageView image = view.findViewById(R.id.icon);
        image.setImageResource(icon);
        addView(view);
    }

    public void addItem(String tag, View view) {
        view.setTooltipText(tag);
        addView(view);
    }

    public void clear() {
        removeAllViews();
    }

    public void onUpdatePrefs() {
        for (int i = 0; i < getChildCount(); i++) {
            final View view = getChildAt(i);
            final View label = view.findViewById(R.id.label);
            if (label != null) {
                label.setVisibility(PrefsUtils.Settings.getShowBottomToolbarLabels() ? VISIBLE : GONE);
            }
        }
    }
}
