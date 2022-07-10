package com.main

import android.content.Context
import android.widget.Toast
import java.io.File

object Main {
	@JvmStatic fun main(context: Context, directory: File) {
		Toast.makeText(context, "Hello World!", Toast.LENGTH_SHORT).show();
	}
}