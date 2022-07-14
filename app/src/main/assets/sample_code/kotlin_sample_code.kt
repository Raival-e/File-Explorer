package com.main

import android.content.Context
import android.widget.Toast
import java.io.File

class Main(context: Context, directory: File) {
    init {
        Toast.makeText(context, "Hello World!", Toast.LENGTH_SHORT).show()
    }
}
