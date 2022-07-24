package com.raival.fileexplorer.activity.editor.formatter.xml

import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.TextRange
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class XmlFormatter : Formatter {
    private var receiver: Formatter.FormatResultReceiver? = null
    private var isRunning = false

    override fun format(text: Content, cursorRange: TextRange) {
        isRunning = true
        receiver?.onFormatSucceed(format(text.toString()), cursorRange)
        isRunning = false
    }

    override fun formatRegion(text: Content, rangeToFormat: TextRange, cursorRange: TextRange) {

    }

    override fun setReceiver(receiver: Formatter.FormatResultReceiver?) {
        this.receiver = receiver
    }

    override fun isRunning(): Boolean {
        return isRunning
    }

    override fun destroy() {
        receiver = null
    }

    @Throws(Exception::class)
    private fun format(xml: String): String {
        val xpp = XmlPullParserFactory.newInstance().newPullParser()
        val reader = StringReader(xml)
        xpp.setInput(reader)
        var currentTag = xpp.eventType
        while (currentTag != XmlPullParser.START_DOCUMENT) {
            currentTag = xpp.next()
        }
        val tags = ArrayList<String>()
        val sb = StringBuilder()
        var indent = ""
        var isEmpty = false
        currentTag = xpp.next()
        while (currentTag != XmlPullParser.END_DOCUMENT) {
            if (currentTag == XmlPullParser.END_TAG) {
                indent = indent.substring(1)
                var lastItem = tags.size - 1
                if (lastItem < 0) lastItem = 0
                if (!isEmpty) sb.append(indent).append("</").append(tags[lastItem]).append(">")
                    .append("\n")
                tags.removeAt(lastItem)
                isEmpty = false
            } else if (currentTag == XmlPullParser.START_TAG) {
                val tagName = xpp.name
                tags.add(tagName)
                sb.append(indent).append("<").append(tagName)
                indent = "$indent	"
                val attrCount = xpp.attributeCount
                if (attrCount > 0) sb.append("\n")
                for (i in 0 until attrCount) {
                    val attrName = xpp.getAttributeName(i)
                    val attrValue = xpp.getAttributeValue(i)
                    sb.append(indent).append(attrName).append("=\"").append(attrValue).append("\"")
                        .append(if (i == attrCount - 1) "" else "\n")
                }
                isEmpty = if (xpp.isEmptyElementTag) {
                    sb.append("/>\n".trimIndent())
                    true
                } else {
                    sb.append(">\n".trimIndent())
                    false
                }
            }
            currentTag = xpp.next()
        }
        reader.close()
        return sb.toString()
    }
}