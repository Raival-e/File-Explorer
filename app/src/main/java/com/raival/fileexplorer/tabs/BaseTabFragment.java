package com.raival.fileexplorer.tabs;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionInflater;

import com.google.android.material.appbar.MaterialToolbar;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.common.view.BottomBarView;
import com.raival.fileexplorer.common.view.TabView;

public abstract class BaseTabFragment extends Fragment {
    public abstract boolean onBackPressed();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public BaseTabFragment() {
        super();
    }
}
