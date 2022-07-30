package com.raival.fileexplorer.extension

import android.util.TypedValue
import com.raival.fileexplorer.App

fun Int.toDp(): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    App.appContext.resources.displayMetrics
).toInt()

fun Float.toDp(): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    App.appContext.resources.displayMetrics
)