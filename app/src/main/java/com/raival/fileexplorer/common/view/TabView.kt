package com.raival.fileexplorer.common.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.raival.fileexplorer.R
import com.raival.fileexplorer.util.Utils

class TabView : HorizontalScrollView {
    private var tabsArray = ArrayList<Tab>()
    private var textColor = 0
    private var indicatorColor = 0
    private var textSize = 0
    private lateinit var onUpdateListener: OnUpdateTabViewListener
    private lateinit var onBulkRemoveListener: OnBulkRemoveListener
    private var container: LinearLayout = LinearLayout(context)

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        addView(
            container,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        isFillViewport = true
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        textColor = Utils.getColorAttribute(R.attr.colorPrimary, context)
        indicatorColor = Utils.getColorAttribute(R.attr.colorPrimary, context)
        textSize = 14
    }

    @JvmOverloads
    fun addNewTab(tag: String, select: Boolean = true): Tab {
        return insertNewTabAt(tabsArray.size, tag, select)
    }

    fun removeTabs(ids: Array<String>) {
        val selectedTabPos = selectedTabPosition
        for (id in ids) {
            val pos = getTabPositionByTag(id)
            if (!valid(pos)) continue
            tabsArray.removeAt(pos)
            container.removeView(container.getChildAt(pos))
        }
        if (tabsArray.size == 0 && this::onUpdateListener.isInitialized) {
            onUpdateListener.onUpdate(null, ON_EMPTY)
            return
        }
        val tab = tabsArray[validatePosition(selectedTabPos)]
        tab.select(true)
        if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(tab, ON_SELECT)
    }

    private fun validatePosition(i: Int): Int {
        return if (i >= tabsArray.size) tabsArray.size - 1 else i.coerceAtLeast(0)
    }

    @JvmOverloads
    fun insertNewTabAt(pos: Int, tag: String, select: Boolean = true): Tab {
        if (!isUnique(tag)) {
            val oldSelectedTab = selectedTab
            oldSelectedTab!!.select(false)
            if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
                oldSelectedTab,
                ON_UNSELECT
            )
            val newTab = getTabByTag(tag)
            newTab!!.select(true)
            if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(newTab, ON_SELECT)
            return newTab
        }
        val oldSelectedTab = selectedTab
        val tab = Tab(tag, createTabView(), select)
        tabsArray.add(pos, tab)
        if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(tab, ON_CREATE)
        if (select) {
            if (oldSelectedTab != null) {
                oldSelectedTab.select(false)
                if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
                    oldSelectedTab,
                    ON_UNSELECT
                )
            }
            if (this::onUpdateListener.isInitialized) {
                onUpdateListener.onUpdate(tab, ON_SELECT)
            }
        }
        container.addView(tab.view, pos)
        scrollToTab(tab)
        return tab
    }

    private fun removeTabAt(pos: Int) {
        if (!valid(pos)) return
        if (tabsArray.size == 1 || selectedTabPosition != pos) {
            container.removeViewAt(pos)
            val tempTab = tabsArray[pos]
            tabsArray.removeAt(pos)
            if (this::onUpdateListener.isInitialized) {
                onUpdateListener.onUpdate(tempTab, ON_REMOVE)
                onUpdateListener.onUpdate(null, ON_EMPTY)
            }
            return
        }
        container.removeViewAt(pos)
        val tempTab = tabsArray[pos]
        tabsArray.removeAt(pos)
        if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(tempTab, ON_REMOVE)
        val limit = tabsArray.size - 1
        val newTabToSelect: Tab = if (pos > limit) {
            tabsArray[pos - 1]
        } else {
            tabsArray[pos]
        }
        newTabToSelect.select(true)
        if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
            newTabToSelect,
            ON_SELECT
        )
    }

    fun removeTab(id: String) {
        for ((i, tab) in tabsArray.withIndex()) {
            if (tab.tag == id) {
                removeTabAt(i)
                return
            }
        }
    }

    fun removeSelectedTab() {
        removeTabAt(selectedTabPosition)
    }

    fun removeAllTabs() {
        if (this::onBulkRemoveListener.isInitialized) onBulkRemoveListener.onBulkRemove(
            tabsArray,
            true
        )
        tabsArray.clear()
        container.removeAllViews()
        if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(null, ON_EMPTY)
    }

    private fun removeAllTabsExcept(pos: Int) {
        if (!valid(pos)) return
        val exception = tabsArray[pos]
        tabsArray.removeAt(pos)
        if (this::onBulkRemoveListener.isInitialized) onBulkRemoveListener.onBulkRemove(
            tabsArray,
            false
        )
        tabsArray.clear()
        container.removeAllViews()
        tabsArray.add(exception)
        container.addView(exception.view)
        if (!exception.isSelected) {
            exception.select(true)
            if (this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
                exception,
                ON_SELECT
            )
        }
        if (tabsArray.size == 0 && this::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
            null,
            ON_EMPTY
        )
    }

    fun removeAllTabsExceptSelectedTab() {
        removeAllTabsExcept(selectedTabPosition)
    }

    private fun valid(pos: Int): Boolean {
        return if (tabsArray.isEmpty()) false else pos > 0 && pos < tabsArray.size
    }

    private fun isUnique(s: String): Boolean {
        for (tab in tabsArray) {
            if (tab.tag == s) return false
        }
        return true
    }

    fun setTextSize(s: Int) {
        textSize = s
    }

    fun setOnUpdateTabViewListener(l: OnUpdateTabViewListener) {
        onUpdateListener = l
    }

    fun setOnBulkRemoveListener(l: OnBulkRemoveListener) {
        onBulkRemoveListener = l
    }

    fun getTabByTag(tag: String?): Tab? {
        for (tab in tabsArray) {
            if (tab.tag == tag) return tab
        }
        return null
    }

    private fun getTabPositionByTag(tag: String): Int {
        for ((i, tab) in tabsArray.withIndex()) {
            if (tab.tag == tag) return i
        }
        return -1
    }

    val tabCount: Int get() = tabsArray.size

    private val selectedTabPosition: Int
        get() {
            for ((i, tab) in tabsArray.withIndex()) {
                if (tab.isSelected) return i
            }
            return -1
        }

    val selectedTabId: String?
        get() {
            for (tab in tabsArray) {
                if (tab.isSelected) return tab.tag
            }
            return null
        }
    val selectedTab: Tab?
        get() {
            for (tab in tabsArray) {
                if (tab.isSelected) return tab
            }
            return null
        }

    fun tabAnimation(v: View?, event: String?) {
        when (event) {
            "onSelect" -> {
                val indicator = v!!.findViewWithTag<View>("tab_indicator")
                ObjectAnimator.ofFloat(
                    indicator,
                    "translationY",
                    Utils.pxToDp(2f),
                    -Utils.pxToDp(3f),
                    0f
                ).setDuration(500).start()
            }
            "onReselect" -> {
                val indicator = v!!.findViewWithTag<View>("tab_indicator")
                ObjectAnimator.ofFloat(indicator, "translationY", 0f, Utils.pxToDp(1.5f), 0f)
                    .setDuration(400).start()
            }
            "onUnselect" -> {
                val indicator = v!!.findViewWithTag<View>("tab_indicator")
                ObjectAnimator.ofFloat(indicator, "translationY", 0f, Utils.pxToDp(2f))
                    .setDuration(400).start()
            }
        }
    }

    private fun createTabView(): View {
        val padding = Utils.pxToDp(8f).toInt()
        val bg = LinearLayout(context)
        bg.orientation = LinearLayout.VERTICAL
        bg.tag = "tab_background"
        bg.setPadding(0, 0, padding, 0)
        bg.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        bg.minimumWidth = Utils.pxToDp(80f).toInt()
        bg.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val out = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, out, true)
        bg.setBackgroundResource(out.resourceId)
        val text = TextView(context)
        text.text = ""
        text.setTextColor(textColor)
        text.textSize = textSize.toFloat()
        text.tag = "tab_text"
        text.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        val line = LinearLayout(context)
        val gd = GradientDrawable()
        gd.setColor(indicatorColor)
        gd.cornerRadii = floatArrayOf(
            Utils.pxToDp(8f),
            Utils.pxToDp(8f),
            Utils.pxToDp(8f),
            Utils.pxToDp(8f),
            0f,
            0f,
            0f,
            0f
        )
        line.background = gd
        line.tag = "tab_indicator"
        bg.addView(text, LinearLayout.LayoutParams(-2, 0, 1f))
        bg.addView(line, LinearLayout.LayoutParams(-1, Utils.pxToDp(3f).toInt()))
        return bg
    }

    fun scrollToTab(tab: Tab?) {
        if (tab?.view == null) return
        post { smoothScrollTo(tab.view.x.toInt() - Utils.pxToDp(50f).toInt(), 0) }
    }

    val tags: ArrayList<String>
        get() {
            val tags = ArrayList<String>()
            for (tab in tabsArray) {
                tags.add(tab.tag)
            }
            return tags
        }

    /**
     * Interface
     *
     *
     * Events called when create/insert new tab:
     * onCreate > onUnselect? > onSelect?
     *
     *
     * Events called when remove a single tab new tab:
     * onRemove > onSelect?
     *
     *
     * Events called when remove moe than one tab:
     * onBulkRemove > onSelect?
     *
     *
     * Events called when remove all tabs:
     * onRemoveAll
     *
     *
     * Event called when update a tab:
     * onUpdate
     */
    interface OnUpdateTabViewListener {
        fun onUpdate(tab: Tab?, event: Int)
    }

    interface OnBulkRemoveListener {
        fun onBulkRemove(array: ArrayList<Tab>?, all: Boolean)
    }

    inner class Tab(val tag: String, val view: View, var isSelected: Boolean) {
        private var name: String = ""

        init {
            select(isSelected)
            view.setOnClickListener {
                if (selectedTab == null) {
                    select(true)
                    if (this@TabView::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
                        this,
                        ON_SELECT
                    )
                    return@setOnClickListener
                }
                if (this.isSelected) {
                    if (this@TabView::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
                        this,
                        ON_RESELECT
                    )
                    tabAnimation(this.view, "onReselect")
                    return@setOnClickListener
                }
                val oldSelectedTab = selectedTab
                oldSelectedTab!!.select(false)
                if (this@TabView::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
                    oldSelectedTab,
                    ON_UNSELECT
                )
                select(true)
                if (this@TabView::onUpdateListener.isInitialized) onUpdateListener.onUpdate(
                    this,
                    ON_SELECT
                )
            }
            view.setOnLongClickListener {
                if (this@TabView::onUpdateListener.isInitialized) {
                    onUpdateListener.onUpdate(this, ON_LONG_CLICK)
                    return@setOnLongClickListener true
                }
                false
            }
        }

        fun setName(s: String) {
            name = s
            (view.findViewWithTag<View>("tab_text") as TextView).text = s
        }

        fun getName(): String = name

        fun select(s: Boolean) {
            isSelected = s
            if (s) {
                view.alpha = 1f
                tabAnimation(view, "onSelect")
                scrollToTab(getTabByTag(tag))
            } else {
                view.alpha = 0.7f
                tabAnimation(view, "onUnselect")
            }
        }
    }

    companion object {
        const val ON_CREATE = 1
        const val ON_SELECT = 2
        const val ON_LONG_CLICK = 3
        const val ON_RESELECT = 4
        const val ON_UNSELECT = -1
        const val ON_REMOVE = -2
        const val ON_EMPTY = 0
    }
}