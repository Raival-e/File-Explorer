package com.raival.fileexplorer.tab.file.d8

import android.view.View
import java.io.File
import java.util.*
import javax.tools.Diagnostic

class DiagnosticWrapper : Diagnostic<File?> {
    private var code: String? = null
    private var source: File? = null
    private var kind: Diagnostic.Kind? = null
    private var position: Long = 0
    private var startPosition: Long = 0
    private var endPosition: Long = 0
    private var lineNumber: Long = 0
    private var columnNumber: Long = 0
    var onClickListener: View.OnClickListener? = null
    private var message: String? = null

    /**
     * Extra information for this diagnostic
     */
    private var extra: Any? = null
    private var startLine = 0
    private var endLine = 0
    private var startColumn = 0
    private var endColumn = 0

    override fun getKind(): Diagnostic.Kind {
        return kind!!
    }

    fun setKind(kind: Diagnostic.Kind) {
        this.kind = kind
    }

    override fun getSource(): File? {
        return source
    }

    fun setSource(source: File?) {
        this.source = source
    }

    override fun getPosition(): Long {
        return position
    }

    fun setPosition(position: Long) {
        this.position = position
    }

    override fun getStartPosition(): Long {
        return startPosition
    }

    fun setStartPosition(startPosition: Long) {
        this.startPosition = startPosition
    }

    override fun getEndPosition(): Long {
        return endPosition
    }

    fun setEndPosition(endPosition: Long) {
        this.endPosition = endPosition
    }

    override fun getLineNumber(): Long {
        return lineNumber
    }

    fun setLineNumber(lineNumber: Long) {
        this.lineNumber = lineNumber
    }

    override fun getColumnNumber(): Long {
        return columnNumber
    }

    fun setColumnNumber(columnNumber: Long) {
        this.columnNumber = columnNumber
    }

    override fun getCode(): String {
        return code!!
    }

    fun setCode(code: String?) {
        this.code = code
    }

    override fun getMessage(locale: Locale): String {
        return message!!
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    override fun toString(): String {
        return """
               startOffset: $startPosition
               endOffset: $endPosition
               position: $position
               startLine: $startLine
               startColumn: $startColumn
               endLine: $endLine
               endColumn: $endColumn
               message: $message
               """.trimIndent()
    }

    override fun hashCode(): Int {
        return Objects.hash(
            code, source, kind, position, startPosition, endPosition, lineNumber,
            columnNumber, message, extra
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other is DiagnosticWrapper) {
            if (other.message != null && message == null) {
                return false
            }
            if (other.message == null && message != null) {
                return false
            }
            if (other.message != message) {
                return false
            }
            if (other.source != source) {
                return false
            }
            return if (other.lineNumber != lineNumber) {
                false
            } else other.columnNumber == columnNumber
        }
        return super.equals(other)
    }

    companion object {
        const val USE_LINE_POS = -31
    }
}