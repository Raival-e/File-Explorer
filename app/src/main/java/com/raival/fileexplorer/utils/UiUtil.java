package com.raival.fileexplorer.utils;

import android.util.TypedValue;

import com.raival.fileexplorer.App;

public class UiUtil {
    public static float pxToDp(float val){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, App.appContext.getResources().getDisplayMetrics());
    }
}
