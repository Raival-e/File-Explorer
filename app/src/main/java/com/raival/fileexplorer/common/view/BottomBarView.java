package com.raival.fileexplorer.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.raival.fileexplorer.R;

import java.util.ArrayList;

public class BottomBarView extends LinearLayout {
    private final ArrayList<Item> items = new ArrayList<>();

    public BottomBarView(Context context) {
        super(context);
    }

    public BottomBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BottomBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addItem(@NonNull String tag, int icon, OnClickListener clickListener) {
        View view = ((LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.bottom_bar_menu_item, this, false);
        view.setOnClickListener(clickListener);
        ImageView image = view.findViewById(R.id.icon);
        image.setImageResource(icon);
        items.add(new Item(tag, view, image));
        addView(view);
    }

    public Item getItem(String tag) {
        for (Item item : items) {
            if (item.tag.equals(tag)) {
                return item;
            }
        }
        return null;
    }

    public void clear() {
        items.clear();
        removeAllViews();
    }

    public class Item {
        String tag;
        View view;
        ImageView icon;

        public Item(String tag, View view, ImageView icon) {
            this.tag = tag;
            this.view = view;
            this.icon = icon;
        }
    }
}
