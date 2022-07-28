package com.raival.fileexplorer.tab.apps.model

import android.graphics.drawable.Drawable
import java.io.File

class Apk(
    val name: String,
    val pkg: String,
    val size: String,
    val icon: Drawable,
    val lastModified: Long,
    val source: File
)