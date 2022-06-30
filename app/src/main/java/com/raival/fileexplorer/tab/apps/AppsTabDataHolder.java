package com.raival.fileexplorer.tab.apps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.raival.fileexplorer.App;
import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.apps.model.Apk;
import com.raival.fileexplorer.tab.apps.resolver.ApkResolver;

import java.util.ArrayList;

public class AppsTabDataHolder extends BaseDataHolder {
    public final String tag;
    private MutableLiveData<ArrayList<Apk>> apps = new MutableLiveData<>();

    public AppsTabDataHolder(String tag) {
        this.tag = tag;
    }

    public LiveData<ArrayList<Apk>> getApps(boolean showSystemApps, boolean sortNewerFirst) {
        if (apps.getValue() == null) {
            loadApps(showSystemApps, sortNewerFirst);
        }
        return apps;
    }

    public void updateAppsList(boolean showSystemApps, boolean sortNewerFirst) {
        loadApps(showSystemApps, sortNewerFirst);
    }

    private void loadApps(boolean showSystemApps, boolean sortNewerFirst) {
        new Thread(() -> {
            ArrayList<Apk> apks = new ApkResolver().load(showSystemApps, sortNewerFirst).get();
            App.appHandler.post(() -> apps.setValue(apks));
        }).start();
    }

    @Override
    public String getTag() {
        return tag;
    }
}
