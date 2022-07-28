package com.raival.fileexplorer.activity.model

import androidx.lifecycle.ViewModel
import com.raival.fileexplorer.tab.BaseDataHolder
import com.raival.fileexplorer.tab.file.model.Task

class MainViewModel : ViewModel() {
    @JvmField
    val tasks = arrayListOf<Task>()
    private val dataHolders: MutableList<BaseDataHolder> = arrayListOf()

    fun addDataHolder(dataHolder: BaseDataHolder) {
        dataHolders.add(dataHolder)
    }

    fun getDataHolders(): ArrayList<BaseDataHolder> {
        return dataHolders as ArrayList<BaseDataHolder>
    }

    fun getDataHolder(tag: String): BaseDataHolder? {
        for (dataHolder in dataHolders) {
            if (dataHolder.tag == tag) return dataHolder
        }
        return null
    }
}