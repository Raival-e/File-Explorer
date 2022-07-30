package com.raival.fileexplorer.extension

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun String.getStringList(): ArrayList<String> {
    return Gson().fromJson(this, object : TypeToken<ArrayList<String>>() {}.type)
}

fun String.surroundWithBrackets(): String {
    return "[$this]"
}