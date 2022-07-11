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
        ((TextView) view.findViewById(R.id.label)).setText(tag);
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
}
