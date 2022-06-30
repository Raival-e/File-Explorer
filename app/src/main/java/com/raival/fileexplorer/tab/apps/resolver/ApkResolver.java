package com.raival.fileexplorer.tab.apps.resolver;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.raival.fileexplorer.App;
import com.raival.fileexplorer.tab.apps.model.Apk;
import com.raival.fileexplorer.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApkResolver {
    private final ArrayList<Apk> list = new ArrayList<>();
    private boolean sortApps;

    public ApkResolver() {
    }

    public ApkResolver load(boolean showSystemApps, boolean sortNewerFirst) {
        list.clear();
        sortApps = sortNewerFirst;

        final PackageManager pm = App.appContext.getPackageManager();

        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES
                | PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS);

        PackageInfo androidInfo = null;
        try {
            androidInfo = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        for (ApplicationInfo info : apps) {
            if (info.sourceDir == null) {
                continue;
            }

            PackageInfo pkgInfo;
            try {
                pkgInfo = pm.getPackageInfo(info.packageName, PackageManager.GET_SIGNATURES);
            } catch (PackageManager.NameNotFoundException e) {
                pkgInfo = null;
            }
            boolean isSystemApp = isAppInSystemPartition(info) || isSignedBySystem(pkgInfo, androidInfo);
            if (isSystemApp && !showSystemApps) continue;

            Apk apk = new Apk();
            apk.pkg = info.packageName;
            apk.name = pm.getApplicationLabel(info).toString();
            apk.source = new File(info.publicSourceDir);
            apk.lastModified = apk.source.lastModified();
            apk.icon = info.loadIcon(pm);
            apk.size = FileUtils.getFormattedFileSize(new File(info.publicSourceDir));
            list.add(apk);
        }
        return this;
    }

    public ArrayList<Apk> get() {
        if (sortApps) list.sort((apk1, apk2) -> Long.compare(apk2.lastModified, apk1.lastModified));
        return list;
    }

    public static boolean isAppInSystemPartition(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags
                & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))
                != 0);
    }

    public boolean isSignedBySystem(PackageInfo piApp, PackageInfo piSys) {
        return (piApp != null
                && piSys != null
                && piApp.signatures != null
                && piSys.signatures[0].equals(piApp.signatures[0]));
    }
}