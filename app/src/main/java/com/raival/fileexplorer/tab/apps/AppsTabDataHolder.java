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

    public LiveData<ArrayList<Apk>> getApps() {
        if (apps.getValue() == null) {
            loadApps();
        }
        return apps;
    }

    private void loadApps() {
        new Thread(() -> {
            ArrayList<Apk> apks = new ApkResolver().load(false, true).get();
            App.appHandler.post(() -> apps.setValue(apks));
        }).start();
    }

    @Override
    public String getTag() {
        return tag;
    }
}
