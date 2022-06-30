package com.raival.fileexplorer.common.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.util.Utils;

import java.util.ArrayList;

public class TabView extends HorizontalScrollView {
    public final static int ON_CREATE = 1;
    public final static int ON_SELECT = 2;
    public final static int ON_LONG_CLICK = 3;
    public final static int ON_RESELECT = 4;
    public final static int ON_UNSELECT = -1;
    public final static int ON_REMOVE = -2;
    public final static int ON_EMPTY = 0;

    public ArrayList<Tab> tabsArray = new ArrayList<>();

    private int textColor;
    private int indicatorColor;
    private int textSize;

    private OnUpdateTabViewListener onUpdateListener;
    private OnBulkRemoveListener onBulkRemoveListener;
    private LinearLayout container;

    public TabView(Context context) {
        super(context);
        init(context);
    }

    public TabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context c) {
        container = new LinearLayout(c);
        addView(container, new ViewGroup.LayoutParams(-1, -1));
        setFillViewport(true);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        textColor = Utils.getColorAttribute(R.attr.colorPrimary, getContext());
        indicatorColor = Utils.getColorAttribute(R.attr.colorPrimary, getContext());
        textSize = 14;
    }

    public Tab addNewTab(String tag) {
        return addNewTab(tag, true);
    }

    public Tab addNewTab(String tag, boolean select) {
        return insertNewTabAt(tabsArray.size(), tag, select);
    }

    public Tab insertNewTabAt(int pos, String tag) {
        return insertNewTabAt(pos, tag, true);
    }

    public void removeTabs(String[] ids) {
        final int selectedTabPos = getSelectedTabPosition();

        for (String id : ids) {
            final int pos = getTabPositionByTag(id);
            if (!valid(pos)) continue;
            tabsArray.remove(pos);
            container.removeView(container.getChildAt(pos));
        }
        if (tabsArray.size() == 0 && onUpdateListener != null) {
            onUpdateListener.onUpdate(null, ON_EMPTY);
            return;
        }
        Tab tab = tabsArray.get(validatePosition(selectedTabPos));
        tab.select(true);
        if (onUpdateListener != null) onUpdateListener.onUpdate(tab, ON_SELECT);
    }

    public int validatePosition(final int i) {
        if (i >= tabsArray.size()) return tabsArray.size() - 1;
        return Math.max(i, 0);
    }

    public Tab insertNewTabAt(int pos, String tag, boolean select) {
        if (!isUnique(tag)) {
            Tab oldSelectedTab = getSelectedTab();
            oldSelectedTab.select(false);
            if (onUpdateListener != null) onUpdateListener.onUpdate(oldSelectedTab, ON_UNSELECT);
            Tab newTab = getTabByTag(tag);
            newTab.select(true);
            if (onUpdateListener != null) onUpdateListener.onUpdate(newTab, ON_SELECT);
            return newTab;
        }
        Tab oldSelectedTab = getSelectedTab();
        Tab tab = new Tab(tag, createTabView(), select);
        tabsArray.add(pos, tab);
        if (onUpdateListener != null) onUpdateListener.onUpdate(tab, ON_CREATE);
        if (select) {
            if (oldSelectedTab != null) {
                oldSelectedTab.select(false);
                if (onUpdateListener != null)
                    onUpdateListener.onUpdate(oldSelectedTab, ON_UNSELECT);
            }
            if (onUpdateListener != null) {
                onUpdateListener.onUpdate(tab, ON_SELECT);
            }
        }
        container.addView(tab.view, pos);
        scrollToTab(tab);
        return tab;
    }

    public void removeTabAt(int pos) {
        if (!valid(pos)) return;
        if (tabsArray.size() == 1 || getSelectedTabPosition() != pos) {
            container.removeViewAt(pos);
            Tab tempTab = tabsArray.get(pos);
            tabsArray.remove(pos);
            if (onUpdateListener != null) {
                onUpdateListener.onUpdate(tempTab, ON_REMOVE);
                onUpdateListener.onUpdate(null, ON_EMPTY);
            }
            return;
        }
        container.removeViewAt(pos);
        Tab tempTab = tabsArray.get(pos);
        tabsArray.remove(pos);
        if (onUpdateListener != null) onUpdateListener.onUpdate(tempTab, ON_REMOVE);
        int limit = tabsArray.size() - 1;
        Tab newTabToSelect;
        if (pos > limit) {
            newTabToSelect = tabsArray.get(pos - 1);
        } else {
            newTabToSelect = tabsArray.get(pos);
        }
        newTabToSelect.select(true);
        if (onUpdateListener != null) onUpdateListener.onUpdate(newTabToSelect, ON_SELECT);
    }

    public void removeTab(String id) {
        int i = 0;
        for (Tab tab : tabsArray) {
            if (tab.tag != null && tab.tag.equals(id)) {
                removeTabAt(i);
                return;
            }
            i++;
        }
    }

    public void removeSelectedTab() {
        removeTabAt(getSelectedTabPosition());
    }

    public void removeAllTabs() {
        if (onUpdateListener != null) onBulkRemoveListener.onBulkRemove(tabsArray, true);
        tabsArray.clear();
        container.removeAllViews();
        if (onUpdateListener != null) onUpdateListener.onUpdate(null, ON_EMPTY);
    }

    public void removeAllTabsExcept(int pos) {
        if (!valid(pos)) return;
        Tab exception = tabsArray.get(pos);
        tabsArray.remove(pos);
        if (onUpdateListener != null) onBulkRemoveListener.onBulkRemove(tabsArray, false);

        tabsArray.clear();
        container.removeAllViews();

        tabsArray.add(exception);
        container.addView(exception.view);
        if (!exception.isSelected) {
            exception.select(true);
            if (onUpdateListener != null) onUpdateListener.onUpdate(exception, ON_SELECT);
        }
        if (tabsArray.size() == 0 && onUpdateListener != null)
            onUpdateListener.onUpdate(null, ON_EMPTY);
    }

    public void removeAllTabsExceptSelectedTab() {
        removeAllTabsExcept(getSelectedTabPosition());
    }

    private boolean valid(int pos) {
        if (tabsArray.isEmpty()) return false;
        return pos > 0 && pos < tabsArray.size();
    }

    public boolean isUnique(String s) {
        for (Tab tab : tabsArray) {
            if (tab.tag.equals(s)) return false;
        }
        return true;
    }

    public void setTextSize(int s) {
        textSize = s;
    }

    public void setOnUpdateTabViewListener(OnUpdateTabViewListener l) {
        onUpdateListener = l;
    }

    public void setOnBulkRemoveListener(OnBulkRemoveListener l) {
        onBulkRemoveListener = l;
    }

    public Tab getTabByTag(String tag) {
        for (Tab tab : tabsArray) {
            if (tab.tag.equals(tag)) return tab;
        }
        return null;
    }

    public int getTabPositionByTag(String tag) {
        int i = 0;
        for (Tab tab : tabsArray) {
            if (tab.tag.equals(tag)) return i;
            i++;
        }
        return -1;
    }

    public int getTabCount() {
        return tabsArray.size();
    }

    public int getSelectedTabPosition() {
        int i = 0;
        for (Tab tab : tabsArray) {
            if (tab.isSelected) return i;
            i++;
        }
        return -1;
    }

    public String getSelectedTabId() {
        for (Tab tab : tabsArray) {
            if (tab.isSelected) return tab.tag;
        }
        return null;
    }

    public Tab getSelectedTab() {
        for (Tab tab : tabsArray) {
            if (tab.isSelected) return tab;
        }
        return null;
    }

    public void tabAnimation(View v, String event) {
        switch (event) {
            case "onSelect": {
                View indicator = v.findViewWithTag("tab_indicator");
                ObjectAnimator.ofFloat(indicator, "translationY", Utils.pxToDp(2), -Utils.pxToDp(2), 0).setDuration(250).start();
                break;
            }
            case "onReselect": {
                View indicator = v.findViewWithTag("tab_indicator");
                ObjectAnimator.ofFloat(indicator, "translationY", 0, -Utils.pxToDp(2), 0).setDuration(250).start();
                break;
            }
            case "onUnselect": {
                View indicator = v.findViewWithTag("tab_indicator");
                ObjectAnimator.ofFloat(indicator, "translationY", 0, Utils.pxToDp(2)).setDuration(250).start();
                break;
            }
        }
    }

    public ArrayList<Tab> getTabsArray() {
        return tabsArray;
    }

    private View createTabView() {
        int padding = (int) Utils.pxToDp(8);
        LinearLayout bg = new LinearLayout(getContext());
        bg.setOrientation(LinearLayout.VERTICAL);
        bg.setTag("tab_background");
        bg.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        bg.setLayoutParams(new ViewGroup.LayoutParams(-2, -1));

        TypedValue out = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, out, true);
        bg.setBackgroundResource(out.resourceId);

        TextView text = new TextView(getContext());
        text.setText("");
        text.setTextColor(textColor);
        text.setPadding(padding, 0, padding, 0);
        text.setTextSize(textSize);
        text.setTag("tab_text");
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        LinearLayout line = new LinearLayout(getContext());
        line.setBackgroundColor(indicatorColor);
        line.setTag("tab_indicator");

        bg.addView(text, new LinearLayout.LayoutParams(-2, 0, 1));
        bg.addView(line, new LinearLayout.LayoutParams(-1, (int) Utils.pxToDp(2), 0));

        return bg;
    }

    public void scrollToTab(Tab tab) {
        if (tab == null || tab.view == null) return;
        post(() -> {
            int[] array = {0, 0};
            tab.view.getLocationOnScreen(array);
            smoothScrollTo(array[0], 0);
        });
    }

    public ArrayList<String> getTags() {
        final ArrayList<String> tags = new ArrayList<>();
        for (Tab tab : tabsArray) {
            tags.add(tab.tag);
        }
        return tags;
    }


    /**
     * Interface
     * <p>
     * Events called when create/insert new tab:
     * onCreate > onUnselect? > onSelect?
     * <p>
     * Events called when remove a single tab new tab:
     * onRemove > onSelect?
     * <p>
     * Events called when remove moe than one tab:
     * onBulkRemove > onSelect?
     * <p>
     * Events called when remove all tabs:
     * onRemoveAll
     * <p>
     * Event called when update a tab:
     * onUpdate
     */

    public interface OnUpdateTabViewListener {
        void onUpdate(Tab tab, int event);
    }

    public interface OnBulkRemoveListener {
        void onBulkRemove(ArrayList<Tab> array, boolean all);
    }

    public class Tab {
        public String tag;
        public String name;
        public View view;
        public boolean isSelected = false;

        public Tab(String tag, View view, boolean isSelected) {
            this.tag = tag;
            this.view = view;
            select(isSelected);
            final boolean listenerDefined = onUpdateListener != null;
            view.setOnClickListener((unused) -> {
                if (getSelectedTab() == null) {
                    select(true);
                    if (listenerDefined) onUpdateListener.onUpdate(this, ON_SELECT);
                    return;
                }
                if (this.isSelected) {
                    if (listenerDefined) onUpdateListener.onUpdate(this, ON_RESELECT);
                    tabAnimation(this.view, "onReselect");
                    return;
                }
                Tab oldSelectedTab = getSelectedTab();
                oldSelectedTab.select(false);
                if (listenerDefined) onUpdateListener.onUpdate(oldSelectedTab, ON_UNSELECT);
                select(true);
                if (listenerDefined) onUpdateListener.onUpdate(this, ON_SELECT);
            });

            view.setOnLongClickListener((unused) -> {
                if (onUpdateListener != null) {
                    onUpdateListener.onUpdate(this, ON_LONG_CLICK);
                    return true;
                }
                return false;
            });

        }

        public void setName(String s) {
            name = s;
            ((TextView) view.findViewWithTag("tab_text")).setText(s);
        }

        public void select(boolean s) {
            isSelected = s;
            if (s) {
                view.setAlpha(1f);
                tabAnimation(view, "onSelect");
                scrollToTab(getTabByTag(tag));
            } else {
                view.setAlpha(0.7f);
                tabAnimation(view, "onUnselect");
            }
        }
    }
}