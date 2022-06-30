package com.raival.fileexplorer.tab.apps;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.BaseTabFragment;
import com.raival.fileexplorer.tab.apps.adapter.AppListAdapter;

public class AppsTabFragment extends BaseTabFragment {
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;

    private MaterialCheckBox showSystemApps;
    private MaterialCheckBox sortApps;

    @Override
    public boolean onBackPressed() {
        super.closeTab();
        return true;
    }

    @Override
    public BaseDataHolder createNewDataHolder() {
        return new AppsTabDataHolder(getTag());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.apps_tab_fragment, container, false);
        recyclerView = view.findViewById(R.id.rv);
        progressIndicator = view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prepareBottomBarView();
        ((AppsTabDataHolder) getDataHolder()).getApps(false, true).observe(getViewLifecycleOwner(), (list) -> {
            recyclerView.setAdapter(new AppListAdapter(list, this));
            progressIndicator.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            showSystemApps.setEnabled(true);
            sortApps.setEnabled(true);
        });
        getTabView().setName("Apps");
    }

    @SuppressLint("SetTextI18n")
    private void prepareBottomBarView() {
        getBottomBarView().clear();

        getBottomBarView().addView(new Space(requireActivity()), new LinearLayout.LayoutParams(0, 0, 1));
        showSystemApps = new MaterialCheckBox(requireActivity());
        showSystemApps.setLayoutParams(new LinearLayout.LayoutParams(-2, -1, 1));
        showSystemApps.setText("System Apps");
        showSystemApps.setGravity(Gravity.CENTER_VERTICAL);
        showSystemApps.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            ((AppsTabDataHolder) getDataHolder()).updateAppsList(showSystemApps.isChecked(), sortApps.isChecked());
            progressIndicator.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            showSystemApps.setEnabled(false);
            sortApps.setEnabled(false);
        }));
        getBottomBarView().addItem("Show System Apps", showSystemApps);

        getBottomBarView().addView(new Space(requireActivity()), new LinearLayout.LayoutParams(0, 0, 1));

        sortApps = new MaterialCheckBox(requireActivity());
        sortApps.setLayoutParams(new LinearLayout.LayoutParams(-2, -1, 1));
        sortApps.setText("Newer First");
        sortApps.setGravity(Gravity.CENTER_VERTICAL);
        sortApps.setChecked(true);
        sortApps.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            ((AppsTabDataHolder) getDataHolder()).updateAppsList(showSystemApps.isChecked(), sortApps.isChecked());
            progressIndicator.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            showSystemApps.setEnabled(false);
            sortApps.setEnabled(false);
        }));
        getBottomBarView().addItem("Sort Apps", sortApps);
        getBottomBarView().addView(new Space(requireActivity()), new LinearLayout.LayoutParams(0, 0, 1));
    }
}
