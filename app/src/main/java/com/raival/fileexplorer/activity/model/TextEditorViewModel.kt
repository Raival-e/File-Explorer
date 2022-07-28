package com.raival.fileexplorer.activity.model

import androidx.lifecycle.ViewModel
import io.github.rosemoe.sora.text.Content
import java.io.File

class TextEditorViewModel : ViewModel() {
    var file: File? = null
    var content: Content? = null
}