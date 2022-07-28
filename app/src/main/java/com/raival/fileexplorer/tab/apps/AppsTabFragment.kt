package com.raival.fileexplorer.tab.apps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.raival.fileexplorer.R
import com.raival.fileexplorer.tab.BaseDataHolder
import com.raival.fileexplorer.tab.BaseTabFragment
import com.raival.fileexplorer.tab.apps.adapter.AppListAdapter
import com.raival.fileexplorer.tab.apps.model.Apk

class AppsTabFragment : BaseTabFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var showSystemApps: MaterialCheckBox
    private lateinit var sortApps: MaterialCheckBox

    override fun onBackPressed(): Boolean {
        super.closeTab()
        return true
    }

    override fun createNewDataHolder(): BaseDataHolder {
        return AppsTabDataHolder(tag!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.apps_tab_fragment, container, false)
        recyclerView = view.findViewById(R.id.rv)
        progressIndicator = view.findViewById(R.id.progress)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareBottomBarView()
        (dataHolder as AppsTabDataHolder).getApps(showSystemApps = false, sortNewerFirst = true)
            .observe(viewLifecycleOwner) { list: ArrayList<Apk> ->
                recyclerView.adapter = AppListAdapter(list, this)
                progressIndicator.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                showSystemApps.isEnabled = true
                sortApps.isEnabled = true
            }
        getTabView()!!.setName("Apps")
    }

    @SuppressLint("SetTextI18n")
    private fun prepareBottomBarView() {
        bottomBarView!!.clear()
        bottomBarView!!.addView(Space(requireActivity()), LinearLayout.LayoutParams(0, 0, 1f))

        showSystemApps = MaterialCheckBox(requireActivity()).apply {
            layoutParams = LinearLayout.LayoutParams(-2, -1, 1f)
            text = "System Apps"
            gravity = Gravity.CENTER_VERTICAL
            setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
                (dataHolder as AppsTabDataHolder).updateAppsList(
                    showSystemApps.isChecked, sortApps.isChecked
                )
                progressIndicator.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                showSystemApps.isEnabled = false
                sortApps.isEnabled = false
            }
        }

        bottomBarView!!.addItem("Show System Apps", showSystemApps)
        bottomBarView!!.addView(Space(requireActivity()), LinearLayout.LayoutParams(0, 0, 1f))

        sortApps = MaterialCheckBox(requireActivity()).apply {
            layoutParams = LinearLayout.LayoutParams(-2, -1, 1f)
            text = "Newer First"
            gravity = Gravity.CENTER_VERTICAL
            isChecked = true
            setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
                (dataHolder as AppsTabDataHolder).updateAppsList(
                    showSystemApps.isChecked, sortApps.isChecked
                )
                progressIndicator.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                showSystemApps.isEnabled = false
                sortApps.isEnabled = false
            }
        }

        bottomBarView!!.addItem("Sort Apps", sortApps)
        bottomBarView!!.addView(Space(requireActivity()), LinearLayout.LayoutParams(0, 0, 1f))
    }
}