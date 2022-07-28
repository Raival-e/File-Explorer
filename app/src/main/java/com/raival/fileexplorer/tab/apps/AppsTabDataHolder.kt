package com.raival.fileexplorer.tab.apps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.raival.fileexplorer.App
import com.raival.fileexplorer.tab.BaseDataHolder
import com.raival.fileexplorer.tab.apps.model.Apk
import com.raival.fileexplorer.tab.apps.resolver.ApkResolver

class AppsTabDataHolder(override val tag: String) : BaseDataHolder() {
    private val apps = MutableLiveData<ArrayList<Apk>>()

    fun getApps(showSystemApps: Boolean, sortNewerFirst: Boolean): LiveData<ArrayList<Apk>> {
        if (apps.value == null) {
            loadApps(showSystemApps, sortNewerFirst)
        }
        return apps
    }

    fun updateAppsList(showSystemApps: Boolean, sortNewerFirst: Boolean) {
        loadApps(showSystemApps, sortNewerFirst)
    }

    private fun loadApps(showSystemApps: Boolean, sortNewerFirst: Boolean) {
        Thread {
            val apks = ApkResolver().load(showSystemApps, sortNewerFirst).get()
            App.appHandler.post { apps.setValue(apks) }
        }.start()
    }
}