package com.raival.fileexplorer.tab.file.misc.md5

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest

object HashUtils {

    private const val STREAM_BUFFER_LENGTH = 1024

    fun getCheckSumFromFile(digest: MessageDigest, filePath: String): String {
        val file = File(filePath)
        return getCheckSumFromFile(digest, file)
    }

    fun getCheckSumFromFile(digest: MessageDigest, file: File): String {
        val fis = FileInputStream(file)
        val byteArray = updateDigest(digest, fis).digest()
        fis.close()
        val hexCode = StringUtils.encodeHex(byteArray, true)
        return String(hexCode)
    }

    private fun updateDigest(digest: MessageDigest, data: InputStream): MessageDigest {
        val buffer = ByteArray(STREAM_BUFFER_LENGTH)
        var read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
        while (read > -1) {
            digest.update(buffer, 0, read)
            read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
        }
        return digest
    }
}
