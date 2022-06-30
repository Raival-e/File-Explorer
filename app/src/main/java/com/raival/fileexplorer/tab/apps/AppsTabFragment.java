package com.raival.fileexplorer.tab.apps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.BaseTabFragment;
import com.raival.fileexplorer.tab.apps.adapter.AppListAdapter;

public class AppsTabFragment extends BaseTabFragment {
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;

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
        ((AppsTabDataHolder) getDataHolder()).getApps().observe(getViewLifecycleOwner(), (list) -> {
            recyclerView.setAdapter(new AppListAdapter(list, this));
            progressIndicator.setVisibility(View.GONE);
        });
        prepareBottomBarView();
        getTabView().setName("Apps");
    }

    private void prepareBottomBarView() {
        getBottomBarView().clear();
    }
}
