package com.raival.fileexplorer.tab.apps.resolver

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.raival.fileexplorer.App
import com.raival.fileexplorer.tab.apps.model.Apk
import com.raival.fileexplorer.tab.file.extension.getFormattedFileSize
import java.io.File

class ApkResolver {
    private val list = ArrayList<Apk>()
    private var sortApps = false

    @SuppressLint("PackageManagerGetSignatures")
    fun load(showSystemApps: Boolean, sortNewerFirst: Boolean): ApkResolver {
        list.clear()
        sortApps = sortNewerFirst
        val pm = App.appContext.packageManager
        val apps = pm.getInstalledApplications(
            PackageManager.MATCH_UNINSTALLED_PACKAGES
                    or PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS
        )

        var androidInfo: PackageInfo? = null
        try {
            androidInfo = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES)
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        for (info in apps) {
            if (info.sourceDir == null) {
                continue
            }
            val pkgInfo: PackageInfo? = try {
                pm.getPackageInfo(info.packageName, PackageManager.GET_SIGNATURES)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

            val isSystemApp = isAppInSystemPartition(info) || isSignedBySystem(pkgInfo, androidInfo)
            if (isSystemApp && !showSystemApps) continue

            val apk = Apk(
                pm.getApplicationLabel(info).toString(),
                info.packageName,
                File(info.publicSourceDir).getFormattedFileSize(),
                info.loadIcon(pm),
                File(info.publicSourceDir).lastModified(),
                File(info.publicSourceDir)
            )
            list.add(apk)
        }
        return this
    }

    fun get(): ArrayList<Apk> {
        if (sortApps) list.sortWith { apk1: Apk, apk2: Apk ->
            apk2.lastModified.compareTo(apk1.lastModified)
        }
        return list
    }

    private fun isSignedBySystem(piApp: PackageInfo?, piSys: PackageInfo?): Boolean {
        return piApp != null && piSys != null && piApp.signatures != null && piSys.signatures[0] == piApp.signatures[0]
    }

    companion object {
        fun isAppInSystemPartition(applicationInfo: ApplicationInfo): Boolean {
            return ((applicationInfo.flags
                    and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))
                    != 0)
        }
    }
}