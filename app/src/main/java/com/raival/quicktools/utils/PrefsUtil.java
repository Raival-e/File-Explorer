package com.raival.quicktools.utils;

import com.pixplicity.easyprefs.library.Prefs;

public class PrefsUtil {
    //public final static int SORT_SYSTEM = 0;
    public final static int SORT_NAME_A2Z = 1;
    public final static int SORT_NAME_Z2A = 2;
    public final static int SORT_SIZE_SMALLER = 3;
    public final static int SORT_SIZE_BIGGER = 4;
    public final static int SORT_DATE_OLDER = 5;
    public final static int SORT_DATE_NEWER = 6;

    public static int getSortingMethod(){
        return Prefs.getInt("sorting_method", SORT_NAME_A2Z);
    }

    public static void setSortingMethod(int method){
        Prefs.putInt("sorting_method", method);
    }

    public static boolean listFoldersFirst(){
        return Prefs.getBoolean("list_folders_first", true);
    }

    public static void setListFoldersFirst(boolean b){
        Prefs.putBoolean("list_folders_first", b);
    }
}
