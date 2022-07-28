package com.raival.fileexplorer.tab.file.model

import java.io.File

abstract class Task {
    abstract val name: String?
    abstract val details: String?
    abstract val isValid: Boolean

    abstract fun setActiveDirectory(directory: File)
    abstract fun start(onUpdate: OnUpdateListener, onFinish: OnFinishListener)

    interface OnUpdateListener {
        fun onUpdate(progress: String)
    }

    interface OnFinishListener {
        fun onFinish(result: String)
    }
}